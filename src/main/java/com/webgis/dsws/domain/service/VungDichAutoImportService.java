package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.model.*;
import com.webgis.dsws.domain.repository.*;

import jakarta.transaction.Transactional;

import com.webgis.dsws.domain.model.enums.MucDoVungDichEnum;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.*;
import java.util.stream.Collectors;

import java.text.SimpleDateFormat;

import com.webgis.dsws.domain.model.enums.TrangThaiVungDichEnum;
import org.springframework.data.jpa.domain.Specification;

@Service
@RequiredArgsConstructor
public class VungDichAutoImportService {

    private final TrangTraiRepository trangTraiRepository;
    private final CaBenhRepository caBenhRepository;
    private final BenhRepository benhRepository;
    private final DonViHanhChinhRepository donViHanhChinhRepository;
    private final VungDichRepository vungDichRepository;
    private final GeometryService geometryService;
    private final ClusterAnalysisService clusterAnalysisService;
    private final BienPhapPhongChongService bienPhapPhongChongService;
    private final VungDichBienPhapRepository vungDichBienPhapRepository;

    public List<VungDich> autoCreateFromData(
            Integer maTinhThanh,
            Integer minCases) {

        // 1. Lấy danh sách ca bệnh theo điều kiện
        List<CaBenh> caBenhs = getCaBenhsByFilters(maTinhThanh);

        // 2. Nhóm các ca bệnh theo loại bệnh
        Map<Benh, List<CaBenh>> diseasesGroups = caBenhs.stream()
                .filter(caBenh -> !caBenh.getDaKetThuc()) // Lọc các ca bệnh chưa kết thúc
                .collect(Collectors.groupingBy(CaBenh::getBenh));

        // 3. Với mỗi loại bệnh, tạo các cluster và vùng dịch tương ứng
        List<VungDich> newZones = new ArrayList<>();
        for (Map.Entry<Benh, List<CaBenh>> diseaseGroup : diseasesGroups.entrySet()) {
            Benh benh = diseaseGroup.getKey();
            List<CaBenh> caBenhsByDisease = diseaseGroup.getValue();

            // Tạo clusters cho từng loại bệnh
            Map<Geometry, List<CaBenh>> diseaseClusters = clusterAnalysisService
                    .clusterDiseasesByLocation(caBenhsByDisease);

            // Tạo vùng dịch cho từng cluster của loại bệnh này
            for (Map.Entry<Geometry, List<CaBenh>> cluster : diseaseClusters.entrySet()) {
                if (cluster.getValue().size() >= (minCases != null ? minCases : 1)) {
                    VungDich vungDich = createZoneFromCluster(cluster.getKey(), cluster.getValue(), benh);
                    vungDich = vungDichRepository.save(vungDich); // Ensure vungDich is saved before associating with
                                                                  // BienPhapPhongChong
                    newZones.add(vungDich);
                }
            }
        }

        return newZones;
    }

    private List<CaBenh> getCaBenhsByFilters(Integer maTinhThanh) {
        Specification<CaBenh> spec = Specification
                .where((root, query, builder) -> builder.equal(root.get("daKetThuc"), false));

        if (maTinhThanh != null && maTinhThanh > 0) {
            List<Integer> allDVHCIds = donViHanhChinhRepository.findAllChildrenIds(maTinhThanh);

            spec = spec.and(
                    (root, query, builder) -> root.get("trangTrai").get("donViHanhChinh").get("id").in(allDVHCIds));
        }

        return caBenhRepository.findAll(spec);
    }

    private Map<Geometry, List<CaBenh>> clusterDiseasesByLocation(List<CaBenh> caBenhs) {
        Map<Geometry, List<CaBenh>> clusters = new HashMap<>();
        List<CaBenh> unprocessed = new ArrayList<>(caBenhs);
        double clusterDistance = 1000.0; // 1km

        while (!unprocessed.isEmpty()) {
            CaBenh current = unprocessed.remove(0);
            List<CaBenh> cluster = new ArrayList<>();
            cluster.add(current);

            Iterator<CaBenh> iterator = unprocessed.iterator();
            while (iterator.hasNext()) {
                CaBenh other = iterator.next();
                double distance = geometryService.calculateDistance(
                        current.getTrangTrai().getPoint(),
                        other.getTrangTrai().getPoint());

                if (distance <= clusterDistance) {
                    cluster.add(other);
                    iterator.remove();
                }
            }

            // Tính centroid cho cluster
            Geometry centroid = geometryService.calculateCentroid(
                    cluster.stream()
                            .map(cb -> cb.getTrangTrai().getPoint())
                            .collect(Collectors.toList()));

            clusters.put(centroid, cluster);
        }

        return clusters;
    }

    @Transactional
    protected VungDich createZoneFromCluster(Geometry center, List<CaBenh> caBenhs, Benh benh) {
        // 1. Validate
        if (caBenhs.isEmpty() || caBenhs.stream().allMatch(CaBenh::getDaKetThuc)) {
            throw new IllegalArgumentException("Không thể tạo vùng dịch từ danh sách ca bệnh đã kết thúc");
        }

        // 2. Create VungDich
        VungDich vungDich = new VungDich();
        vungDich.setBenh(benh);
        vungDich.setGeom(center);
        vungDich.setBanKinh(calculateRadius(caBenhs));
        vungDich.setMucDo(calculateSeverity(caBenhs));
        vungDich.setTrangThai(TrangThaiVungDichEnum.DANG_GIAM_SAT);
        vungDich.setNgayBatDau(
                caBenhs.stream()
                        .map(CaBenh::getNgayPhatHien)
                        .min(Date::compareTo)
                        .orElseThrow());

        // 3. Save VungDich first
        vungDich = vungDichRepository.save(vungDich);

        // 4. Create default measures
        // List<BienPhapPhongChong> defaultMeasures = bienPhapPhongChongService
        // .getDefaultMeasuresForDisease(benh.getId());

        // // 5. Create associations
        // List<VungDichBienPhap> vungDichBienPhaps = defaultMeasures.stream()
        // .map(measure -> {
        // VungDichBienPhap vdbp = new VungDichBienPhap();
        // vdbp.setVungDich(vungDich);
        // vdbp.setBienPhap(measure);
        // vdbp.setNgayApDung(new Date(System.currentTimeMillis()));
        // return vdbp;
        // })
        // .collect(Collectors.toList());

        // vungDichBienPhapRepository.saveAll(vungDichBienPhaps);

        return vungDich;
    }

    /**
     * @param maBenhCode
     * @return
     */
    private String generateZoneCode(Long maBenhCode) {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return String.format("VD_%s_%s", maBenhCode, timestamp);
    }

    private String calculateFarmImpact(float distance, float radius) {
        double impactRatio = distance / radius;
        if (impactRatio <= 0.3)
            return "Rất cao";
        if (impactRatio <= 0.5)
            return "Cao";
        if (impactRatio <= 0.7)
            return "Trung bình";
        return "Thấp";
    }

    private BienPhapPhongChong createMeasure(String ten, String moTa, int mucDoUuTien) {
        BienPhapPhongChong measure = new BienPhapPhongChong();
        measure.setTenBienPhap(ten);
        measure.setMoTa(moTa);
        measure.setThuTuUuTien(mucDoUuTien);
        return measure;
    }

    /**
     * Xác định đơn vị hành chính cho vùng dịch dựa trên phân tích cluster
     */
    private DonViHanhChinh determineAdministrativeUnit(List<CaBenh> caBenhs, Geometry center) {
        // Thống kê số ca bệnh theo đơn vị hành chính
        Map<DonViHanhChinh, Long> dvhcCount = caBenhs.stream()
                .map(cb -> cb.getTrangTrai().getDonViHanhChinh())
                .collect(Collectors.groupingBy(
                        dvhc -> dvhc,
                        Collectors.counting()));

        // Tìm đơn vị hành chính có nhiều ca bệnh nhất
        return dvhcCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException("Không thể xác định đơn vị hành chính"));
    }

    private List<VungDichTrangTrai> calculateAffectedFarms(VungDich vungDich) {
        List<TrangTrai> trangTraisInRange = trangTraiRepository.findFarmsWithinDistance(
                vungDich.getGeom(),
                vungDich.getBanKinh());

        return trangTraisInRange.stream()
                .map(trangTrai -> {
                    VungDichTrangTrai vdt = new VungDichTrangTrai();
                    vdt.setVungDich(vungDich);
                    vdt.setTrangTrai(trangTrai);
                    vdt.setKhoangCach((float) geometryService.calculateDistance(
                            vungDich.getGeom(),
                            trangTrai.getPoint()));
                    return vdt;
                })
                .collect(Collectors.toList());
    }

    private float calculateRadius(List<CaBenh> caBenhs) {
        // Tính bán kính dựa trên khoảng cách xa nhất từ trung tâm đến các điểm
        double maxDistance = 0;
        Point centroid = geometryService.calculateCentroid(
                caBenhs.stream()
                        .map(cb -> cb.getTrangTrai().getPoint())
                        .collect(Collectors.toList()))
                .getCentroid();

        for (CaBenh caBenh : caBenhs) {
            double distance = geometryService.calculateDistance(
                    centroid,
                    caBenh.getTrangTrai().getPoint());
            maxDistance = Math.max(maxDistance, distance);
        }

        return (float) (maxDistance * 1.2); // Thêm 20% buffer
    }

    private MucDoVungDichEnum calculateSeverity(List<CaBenh> caBenhs) {
        int totalCases = caBenhs.stream()
                .mapToInt(CaBenh::getSoCaNhiemBanDau)
                .sum();

        if (totalCases >= 100)
            return MucDoVungDichEnum.CAP_DO_4;
        if (totalCases >= 50)
            return MucDoVungDichEnum.CAP_DO_3;
        if (totalCases >= 10)
            return MucDoVungDichEnum.CAP_DO_2;
        return MucDoVungDichEnum.CAP_DO_1;
    }

    private double calculateTimeSpan(List<CaBenh> caBenhs) {
        Date earliest = caBenhs.stream()
                .map(CaBenh::getNgayPhatHien)
                .min(Date::compareTo)
                .orElse(new Date());

        Date latest = caBenhs.stream()
                .map(CaBenh::getNgayPhatHien)
                .max(Date::compareTo)
                .orElse(new Date());

        return (latest.getTime() - earliest.getTime()) / (1000.0 * 60 * 60 * 24); // Convert to days
    }

    private double calculateSpreadRate(List<CaBenh> caBenhs) {
        double timeSpan = calculateTimeSpan(caBenhs);
        if (timeSpan == 0)
            return 0;

        return caBenhs.size() / timeSpan; // Cases per day
    }
}
