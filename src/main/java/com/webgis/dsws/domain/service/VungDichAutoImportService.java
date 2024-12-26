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
    public List<VungDich> autoCreateFromData(Integer capHanhChinh) {
        // 1. Lấy danh sách ca bệnh theo điều kiện
        List<CaBenh> caBenhs = getCaBenhsByCapHanhChinh(capHanhChinh);

        // 2. Nhóm các ca bệnh theo đơn vị hành chính trước
        Map<DonViHanhChinh, List<CaBenh>> adminGroups = caBenhs.stream()
                .filter(caBenh -> !caBenh.getDaKetThuc())
                .collect(Collectors.groupingBy(cb -> cb.getTrangTrai().getDonViHanhChinh()));

        List<VungDich> newZones = new ArrayList<>();

        // 3. Xử lý từng đơn vị hành chính
        for (Map.Entry<DonViHanhChinh, List<CaBenh>> adminGroup : adminGroups.entrySet()) {
            DonViHanhChinh dvhc = adminGroup.getKey();
            List<CaBenh> adminCases = adminGroup.getValue();

            // 4. Trong mỗi đơn vị hành chính, nhóm theo bệnh
            Map<Benh, List<CaBenh>> diseasesGroups = adminCases.stream()
                    .collect(Collectors.groupingBy(CaBenh::getBenh));

            // 5. Xử lý từng loại bệnh trong đơn vị hành chính
            for (Map.Entry<Benh, List<CaBenh>> diseaseGroup : diseasesGroups.entrySet()) {
                Benh benh = diseaseGroup.getKey();
                List<CaBenh> caBenhsByDisease = diseaseGroup.getValue();

                // 6. Chỉ tạo cluster cho các ca bệnh trong cùng đơn vị hành chính
                Map<Geometry, List<CaBenh>> diseaseClusters = clusterAnalysisService
                        .clusterDiseasesByLocation(caBenhsByDisease);

                for (Map.Entry<Geometry, List<CaBenh>> cluster : diseaseClusters.entrySet()) {
                    boolean shouldCreateZone = false;
                    if (benh.getMucDoBenhs().contains(MucDoBenhEnum.BANG_A)) {
                        shouldCreateZone = true;
                    } else {
                        int totalAnimals = cluster.getValue().stream()
                                .flatMap(caBenh -> caBenh.getTrangTrai().getTrangTraiVatNuois().stream())
                                .filter(ttvn -> benh.getLoaiVatNuoi().contains(ttvn.getLoaiVatNuoi()))
                                .mapToInt(TrangTraiVatNuoi::getSoLuong)
                                .sum();

                        int totalInfections = cluster.getValue().stream()
                                .mapToInt(CaBenh::getSoCaNhiemBanDau)
                                .sum();

                        double infectionRatio = (double) totalInfections / totalAnimals;
                        if (infectionRatio >= 0.2) {
                            shouldCreateZone = true;
                        }
                    }

                    if (shouldCreateZone) {
                        VungDich vungDich = createZoneFromCluster(cluster.getKey(), cluster.getValue(), benh, dvhc);
                        vungDich = vungDichRepository.save(vungDich);
                        newZones.add(vungDich);
                    }
                }
            }
        }
        return newZones;
    }

    @Transactional
    public VungDich autoCreateZoneForCase(CaBenh caBenh) {
        DonViHanhChinh dvhc = caBenh.getTrangTrai().getDonViHanhChinh();
        Benh benh = caBenh.getBenh();

        // Create geometry from case location
        Point caseLocation = caBenh.getTrangTrai().getPoint();
        double initialRadius = calculateInitialRadius(caBenh);
        Geometry zoneGeom = geometryService.createCircle(caseLocation, initialRadius);

        // Create and save new zone
        VungDich vungDich = new VungDich();
        vungDich.setMaVung(generateZoneCode(benh.getId()));
        vungDich.setTenVung("Vùng dịch " + benh.getTenBenh() + " - " + caBenh.getTrangTrai().getDiaChiDayDu());
        vungDich.setMucDoNghiemTrong(calculateSeverityLevel(Collections.singletonList(caBenh)));
        vungDich.setMucDo(calculateMucDoVungDich(vungDich.getMucDoNghiemTrong()));
        vungDich.setBenh(benh);
        vungDich.setGeom(zoneGeom);
        vungDich.setBanKinh((float) initialRadius);
        vungDich.setNgayBatDau(caBenh.getNgayPhatHien());

        return vungDichService.save(vungDich, Collections.singletonList(caBenh));
    }

    private double calculateInitialRadius(CaBenh caBenh) {
        // Base radius based on disease severity
        double baseRadius = 1000; // 1km default

        if (caBenh.getBenh().getMucDoBenhs().contains(MucDoBenhEnum.BANG_A)) {
            baseRadius = 3000; // 3km for category A diseases
        }

        // Adjust based on infection rate
        TrangTrai trangTrai = caBenh.getTrangTrai();
        double infectionRate = calculateInfectionRate(caBenh, trangTrai);

        return baseRadius * (1 + infectionRate);
    }

    private double calculateInfectionRate(CaBenh caBenh, TrangTrai trangTrai) {
        int totalAnimals = trangTrai.getTrangTraiVatNuois().stream()
                .filter(ttvn -> caBenh.getBenh().getLoaiVatNuoi().contains(ttvn.getLoaiVatNuoi()))
                .mapToInt(TrangTraiVatNuoi::getSoLuong)
                .sum();
        return totalAnimals > 0 ? (double) caBenh.getSoCaNhiemBanDau() / totalAnimals : 0;
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

    @Transactional
    protected VungDich createZoneFromCluster(Geometry center, List<CaBenh> caBenhs, Benh benh, DonViHanhChinh dvhc) {
        if (caBenhs.isEmpty()) {
            throw new IllegalArgumentException("Không thể tạo vùng dịch từ danh sách ca bệnh rỗng");
        }

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

        // Chỉ liên kết với các ca bệnh trong cùng đơn vị hành chính
        List<CaBenh> filteredCaBenhs = caBenhs.stream()
                .filter(cb -> cb.getTrangTrai().getDonViHanhChinh().equals(dvhc))
                .collect(Collectors.toList());

        vungDich = vungDichService.save(vungDich, filteredCaBenhs);
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
}
