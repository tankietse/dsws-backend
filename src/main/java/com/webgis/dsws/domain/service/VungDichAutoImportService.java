package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.model.*;
import com.webgis.dsws.domain.repository.*;
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

    public List<VungDich> autoCreateFromData(
            Date fromDate,
            Date toDate,
            String maTinhThanh,
            String loaiBenh,
            Integer minCases) {

        // 1. Lấy danh sách ca bệnh theo điều kiện
        List<CaBenh> caBenhs = getCaBenhsByFilters(fromDate, toDate, maTinhThanh, loaiBenh);

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
                    newZones.add(vungDichRepository.save(vungDich));
                }
            }
        }

        return newZones;
    }

    private List<CaBenh> getCaBenhsByFilters(
            Date fromDate,
            Date toDate,
            String maTinhThanh,
            String loaiBenh) {

        Specification<CaBenh> spec = Specification.where(null);

        if (fromDate != null) {
            spec = spec.and((root, query, builder) -> 
                builder.greaterThanOrEqualTo(root.get("ngayPhatHien"), fromDate));
        }

        if (toDate != null) {
            spec = spec.and((root, query, builder) -> 
                builder.lessThanOrEqualTo(root.get("ngayPhatHien"), toDate));
        }

        if (maTinhThanh != null) {
            spec = spec.and((root, query, builder) -> 
                builder.equal(root.get("trangTrai").get("donViHanhChinh").get("maTinhThanh"), maTinhThanh));
        }

        if (loaiBenh != null) {
            spec = spec.and((root, query, builder) -> 
                builder.equal(root.get("benh").get("tenBenh"), loaiBenh));
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

    private VungDich createZoneFromCluster(Geometry center, List<CaBenh> caBenhs, Benh benh) {
        if (caBenhs.isEmpty()) {
            throw new IllegalArgumentException("Không thể tạo vùng dịch từ danh sách ca bệnh rỗng");
        }

        VungDich vungDich = new VungDich();

        // 1. Thông tin cơ bản
        float radius = (float) calculateRadius(caBenhs);
        MucDoVungDichEnum severity = calculateSeverity(caBenhs);
        java.sql.Date ngayBatDau = caBenhs.stream()
                .map(CaBenh::getNgayPhatHien)
                .min(Date::compareTo)
                .orElseThrow(() -> new IllegalStateException("Không thể xác định ngày bắt đầu"));

        // 2. Thiết lập thông tin vùng dịch
        String maVung = generateZoneCode(benh.getId());
        vungDich.setMaVung(maVung);
        vungDich.setBenh(benh);
        vungDich.setGeom(center);
        vungDich.setBanKinh(radius);
        vungDich.setMucDo(severity);
        vungDich.setNgayBatDau(ngayBatDau);
        // TODO: Set trạng thái vùng dịch dựa vào tính toán ca bệnh và tốc độ lây lan
        // của bệnh
        vungDich.setTrangThai(TrangThaiVungDichEnum.DANG_BUNG_PHAT);
        vungDich.setMucDoNghiemTrong(severity.ordinal() + 1);

        // 3. Tạo tên và mô tả
        String tenVung = String.format("Vùng dịch %s - %s",
                benh.getTenBenh(),
                maVung);
        vungDich.setTenVung(tenVung);

        String moTa = String.format(
                "Vùng dịch %s\n" +
                        "Số ca nhiễm: %d\n" +
                        "Mức độ: %s\n" +
                        "Bán kính ảnh hưởng: %.1f mét",
                benh.getTenBenh(),
                caBenhs.size(),
                severity.name(),
                radius);

        // 5. Thiết lập các trang trại bị ảnh hưởng
        Set<VungDichTrangTrai> affectedFarms = calculateAffectedFarms(vungDich).stream()
                .peek(vdt -> {
                    vdt.setNgayBatDauAnhHuong(ngayBatDau);
                    vdt.setMucDoAnhHuong(calculateFarmImpact(vdt.getKhoangCach(), radius));
                })
                .collect(Collectors.toSet());
        vungDich.setTrangTrais(affectedFarms);

        // 6. Thiết lập các biện pháp phòng chống
        vungDich.setBienPhapXuLy(benh.getBienPhapPhongNgua());
        Set<BienPhapPhongChong> bienPhaps = bienPhapPhongChongService.getDefaultPreventiveMeasures(severity);
        vungDich.setBienPhapPhongChongs(bienPhaps);

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

    private double calculateRadius(List<CaBenh> caBenhs) {
        if (caBenhs.isEmpty())
            return 1000.0; // Default 1km

        // Tính centroid của cluster
        Point centroid = geometryService.calculateCentroid(
                caBenhs.stream()
                        .map(cb -> cb.getTrangTrai().getPoint())
                        .collect(Collectors.toList()))
                .getCentroid();

        // Tìm điểm xa nhất từ centroid
        double maxDistance = caBenhs.stream()
                .mapToDouble(cb -> geometryService.calculateDistance(
                        centroid,
                        cb.getTrangTrai().getPoint()))
                .max()
                .orElse(1000.0);

        // Thêm buffer 20%
        return maxDistance * 1.2;
    }

    private MucDoVungDichEnum calculateSeverity(List<CaBenh> caBenhs) {
        if (caBenhs.isEmpty())
            return MucDoVungDichEnum.CAP_DO_1;

        // Tính các tiêu chí đánh giá mức độ
        int totalCases = caBenhs.size();
        double timeSpan = calculateTimeSpan(caBenhs);
        double spreadRate = calculateSpreadRate(caBenhs);

        // Xác định mức độ dựa trên các ngưỡng
        if (totalCases >= 10 && timeSpan <= 7 && spreadRate >= 2.0) {
            return MucDoVungDichEnum.CAP_DO_4;
        } else if (totalCases >= 7 && timeSpan <= 14 && spreadRate >= 1.5) {
            return MucDoVungDichEnum.CAP_DO_3;
        } else if (totalCases >= 5 && timeSpan <= 21 && spreadRate >= 1.0) {
            return MucDoVungDichEnum.CAP_DO_2;
        } else {
            return MucDoVungDichEnum.CAP_DO_1;
        }
    }

    private double calculateTimeSpan(List<CaBenh> caBenhs) {
        Date earliest = caBenhs.stream()
                .map(CaBenh::getNgayPhatHien)
                .min(Date::compareTo)
                .orElse((java.sql.Date) new Date());

        Date latest = caBenhs.stream()
                .map(CaBenh::getNgayPhatHien)
                .max(Date::compareTo)
                .orElse((java.sql.Date) new Date());

        return (latest.getTime() - earliest.getTime()) / (1000.0 * 60 * 60 * 24); // Convert to days
    }

    private double calculateSpreadRate(List<CaBenh> caBenhs) {
        double timeSpan = calculateTimeSpan(caBenhs);
        if (timeSpan == 0)
            return 0;

        return caBenhs.size() / timeSpan; // Cases per day
    }
}