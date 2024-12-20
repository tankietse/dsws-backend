package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.model.*;
import com.webgis.dsws.domain.repository.*;

import jakarta.transaction.Transactional;

import com.webgis.dsws.domain.model.enums.MucDoBenhEnum;
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

    private final CaBenhRepository caBenhRepository;
    private final VungDichRepository vungDichRepository;
    private final GeometryService geometryService;
    private final ClusterAnalysisService clusterAnalysisService;
    private final VungDichService vungDichService;

    @Transactional
    public List<VungDich> autoCreateFromData(
            Integer capHanhChinh) {

        // 1. Lấy danh sách ca bệnh theo điều kiện
        List<CaBenh> caBenhs = getCaBenhsByCapHanhChinh(capHanhChinh);

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
                boolean shouldCreateZone = false;
                // For diseases of category BANG_A, always create an epidemic zone
                // Trong trường hợp
                if (benh.getMucDoBenhs().contains(MucDoBenhEnum.BANG_A)) {
                    shouldCreateZone = true;
                } else {
                    // Xác định tổng số động vật của loại bị ảnh hưởng trong cluster
                    int totalAnimals = cluster.getValue().stream()
                            .flatMap(caBenh -> caBenh.getTrangTrai().getTrangTraiVatNuois().stream())
                            .filter(ttvn -> benh.getLoaiVatNuoi().contains(ttvn.getLoaiVatNuoi()))
                            .mapToInt(TrangTraiVatNuoi::getSoLuong)
                            .sum();
                    // Tính ra tổng số động vật bị nhiễm trong cluster
                    int totalInfections = cluster.getValue().stream()
                            .mapToInt(CaBenh::getSoCaNhiemBanDau)
                            .sum();
                    // TÍnh tỷ lệ nhiễm trên tổng số động vật
                    double infectionRatio = (double) totalInfections / totalAnimals;
                    // Nếu tỷ lệ nhiễm vượt quá ngưỡng cho phép, tạo vùng dịch
                    if (infectionRatio >= 0.2) {
                        shouldCreateZone = true;
                    }
                }
                if (shouldCreateZone) {
                    VungDich vungDich = createZoneFromCluster(cluster.getKey(), cluster.getValue(), benh);
                    vungDich = vungDichRepository.save(vungDich); // Ensure vungDich is saved before associating with
                                                                  // BienPhapPhongChong
                    newZones.add(vungDich);
                }
            }
        }

        return newZones;
    }

    private List<CaBenh> getCaBenhsByCapHanhChinh(Integer capHanhChinh) {
        Specification<CaBenh> spec = Specification
                .where((root, query, builder) -> builder.equal(root.get("daKetThuc"), false));

        if (capHanhChinh != null) {
            spec = spec.and((root, query, builder) -> builder.equal(
                    root.get("trangTrai")
                            .get("donViHanhChinh")
                            .get("capHanhChinh"),
                    capHanhChinh.toString()));
        }

        return caBenhRepository.findAll(spec);
    }

    // private Map<Geometry, List<CaBenh>> clusterDiseasesByLocation(List<CaBenh>
    // caBenhs) {
    // Map<Geometry, List<CaBenh>> clusters = new HashMap<>();
    // List<CaBenh> unprocessed = new ArrayList<>(caBenhs);
    // double clusterDistance = 1000.0; // 1km

    // while (!unprocessed.isEmpty()) {
    // CaBenh current = unprocessed.removeFirst();
    // List<CaBenh> cluster = new ArrayList<>();
    // cluster.add(current);

    // Iterator<CaBenh> iterator = unprocessed.iterator();
    // while (iterator.hasNext()) {
    // CaBenh other = iterator.next();
    // double distance = geometryService.calculateDistance(
    // current.getTrangTrai().getPoint(),
    // other.getTrangTrai().getPoint());

    // if (distance <= clusterDistance) {
    // cluster.add(other);
    // iterator.remove();
    // }
    // }

    // // Tính centroid cho cluster
    // Geometry centroid = geometryService.calculateCentroid(
    // cluster.stream()
    // .map(cb -> cb.getTrangTrai().getPoint())
    // .collect(Collectors.toList()));

    // clusters.put(centroid, cluster);
    // }

    // return clusters;
    // }

    @Transactional
    protected VungDich createZoneFromCluster(Geometry center, List<CaBenh> caBenhs, Benh benh) {
        // 1. Validate
        if (caBenhs.isEmpty() || caBenhs.stream().allMatch(CaBenh::getDaKetThuc)) {
            throw new IllegalArgumentException("Không thể tạo vùng dịch từ danh sách ca bệnh đã kết thúc");
        }

        // 2. Create VungDich
        VungDich vungDich = new VungDich();
        vungDich.setMaVung(generateZoneCode(benh.getId()));
        vungDich.setTenVung(
                "Vùng dịch " + benh.getTenBenh() + " - " + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        int severityLevel = calculateSeverityLevel(caBenhs);
        vungDich.setMucDoNghiemTrong(severityLevel);
        vungDich.setMucDo(calculateMucDoVungDich(severityLevel));
        vungDich.setColorCode(getColorCodeForSeverityLevel(severityLevel));
        vungDich.setNgayKetThuc(null);
        vungDich.setBenh(benh);
        vungDich.setGeom(center);
        vungDich.setBanKinh(calculateRadius(caBenhs));
        vungDich.setTrangThai(TrangThaiVungDichEnum.DANG_GIAM_SAT);
        vungDich.setNgayBatDau(
                caBenhs.stream()
                        .map(CaBenh::getNgayPhatHien)
                        .min(Date::compareTo)
                        .orElseThrow());
        String moTa = String.format("Vùng dịch %s với %d ca bệnh, bắt đầu từ %s",
                benh.getTenBenh(),
                caBenhs.size(),
                new SimpleDateFormat("dd/MM/yyyy").format(
                        caBenhs.stream()
                                .map(CaBenh::getNgayPhatHien)
                                .min(Date::compareTo)
                                .orElseThrow()));
        vungDich.setMoTa(moTa);

        // 3. Save VungDich using VungDichService to associate VungDichTrangTrai
        vungDich = vungDichService.save(vungDich, caBenhs);

        return vungDich;
    }

    @Transactional
    protected int calculateSeverityLevel(List<CaBenh> caBenhs) {
        return caBenhs.stream()
                .flatMap(caBenh -> caBenh.getBenh().getMucDoBenhs().stream())
                .mapToInt(MucDoBenhEnum::getSeverityLevel)
                .max()
                .orElse(1);
    }

    private MucDoVungDichEnum calculateMucDoVungDich(int severityLevel) {
        // Chuyển đổi severityLevel thành MucDoVungDichEnum tương ứng
        return MucDoVungDichEnum.fromSeverityLevel(severityLevel);
    }

    private String getColorCodeForSeverityLevel(int severityLevel) {
        for (MucDoBenhEnum mucDo : MucDoBenhEnum.values()) {
            if (mucDo.getSeverityLevel() == severityLevel) {
                return mucDo.getColorCode();
            }
        }
        return "#008000"; // Mặc định xanh lá cây
    }

    /**
     * @param maBenhCode
     * @return
     */
    private String generateZoneCode(Long maBenhCode) {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return String.format("VD_%s_%s", maBenhCode, timestamp);
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

    // // Todo: Chuyen doi cach tinh muc do vung dich
    // private MucDoVungDichEnum calculateSeverity(List<CaBenh> caBenhs) {
    // int totalCases = caBenhs.stream()
    // .mapToInt(CaBenh::getSoCaNhiemBanDau)
    // .sum();

    // if (totalCases >= 100)
    // return MucDoVungDichEnum.CAP_DO_4;
    // if (totalCases >= 50)
    // return MucDoVungDichEnum.CAP_DO_3;
    // if (totalCases >= 10)
    // return MucDoVungDichEnum.CAP_DO_2;
    // return MucDoVungDichEnum.CAP_DO_1;
    // }
}
