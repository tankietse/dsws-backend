package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.model.Benh;
import com.webgis.dsws.domain.model.CaBenh;
import com.webgis.dsws.domain.model.LoaiVatNuoi;
import com.webgis.dsws.domain.model.TrangTrai;
import com.webgis.dsws.domain.model.enums.MucDoBenhEnum;

import jakarta.persistence.EntityNotFoundException;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service phân tích và nhóm các ca bệnh thành cluster.
 * Tuân thủ Single Responsibility Principle - chỉ tập trung vào phân tích
 * cluster.
 */
@Service
@RequiredArgsConstructor
public class ClusterAnalysisService {

    private final GeometryService geometryService;
    private final TrangTraiService trangTraiService;
    private final BenhService benhService;

    // Constants for distance calculation
    private static final double BASE_DISTANCE = 1000.0; // Base distance 1km
    private static final double MAX_DISTANCE = 5000.0; // Maximum allowed distance 5km
    private static final double MIN_DISTANCE = 500.0; // Minimum allowed distance 500m

    // Các hệ số điều chỉnh
    private static final double SEVERITY_WEIGHT = 0.4; // Trọng số mức độ nghiêm trọng
    private static final double DENSITY_WEIGHT = 0.3; // Trọng số mật độ
    private static final double TRANSMISSION_WEIGHT = 0.3; // Trọng số khả năng lây lan

    // Thêm hệ số cho tính toán mức độ nghiêm trọng
    private static final double MORTALITY_WEIGHT = 0.5; // Trọng số tỷ lệ tử vong
    private static final double INFECTION_WEIGHT = 0.3; // Trọng số khả năng lây nhiễm
    private static final double RECOVERY_WEIGHT = 0.2; // Trọng số khả năng hồi phục

    // Khoảng cách để kiểm tra mật độ trang trại
    private static final double DENSITY_CHECK_RADIUS = 2000.0; // 2km
    private static final double AREA_IN_SQ_KM = Math.PI * Math.pow(DENSITY_CHECK_RADIUS / 1000, 2); // Area in km²
    private static final double AVERAGE_FARM_SIZE = 10000.0; // 1 hectare = 10000m²
    private static final double FARM_SPACING = 50.0; // Minimum spacing between farms in meters

    // Base factors cho các loại bệnh theo phân loại
    private static final Map<MucDoBenhEnum, Double> DISEASE_SEVERITY_FACTORS = Map.of(
            MucDoBenhEnum.BANG_A, 1.0, // Nghiêm trọng nhất
            MucDoBenhEnum.BANG_B, 0.8,
            MucDoBenhEnum.NGUY_HIEM, 0.7,
            MucDoBenhEnum.PHONG_BENH_BAT_BUOC, 0.5,
            MucDoBenhEnum.THONG_THUONG, 0.3);

    /**
     * Nhóm các ca bệnh theo khoảng cách địa lý
     * 
     * @param caBenhs Danh sách ca bệnh cần phân tích
     * @return Map chứa geometry trung tâm và danh sách ca bệnh trong cluster
     */
    @Transactional(readOnly = true)
    public Map<Geometry, List<CaBenh>> clusterDiseasesByLocation(List<CaBenh> caBenhs) {
        Map<Geometry, List<CaBenh>> clusters = new HashMap<>();
        List<CaBenh> unprocessed = new ArrayList<>(caBenhs);

        while (!unprocessed.isEmpty()) {
            CaBenh current = unprocessed.remove(0);
            List<CaBenh> cluster = buildCluster(current, unprocessed);
            Geometry centroid = calculateClusterCentroid(cluster);
            clusters.put(centroid, cluster);
        }

        return clusters;
    }

    private List<CaBenh> buildCluster(CaBenh center, List<CaBenh> remainingCases) {
        List<CaBenh> cluster = new ArrayList<>();
        cluster.add(center);

        Iterator<CaBenh> iterator = remainingCases.iterator();
        while (iterator.hasNext()) {
            CaBenh other = iterator.next();
            if (isWithinClusterDistance(center, other)) {
                cluster.add(other);
                iterator.remove();
            }
        }

        return cluster;
    }

    private boolean isWithinClusterDistance(CaBenh center, CaBenh other) {
        double calculatedDistance = calculateClusterDistance(center);
        double actualDistance = geometryService.calculateDistance(
                center.getTrangTrai().getPoint(),
                other.getTrangTrai().getPoint());
        return actualDistance <= calculatedDistance;
    }

    /**
     * Tính toán khoảng cách cluster dựa trên các yếu tố:
     * - Mức độ nghiêm trọng của bệnh
     * - Mật độ trang trại trong khu vực
     * - Khả năng lây lan của bệnh
     */
    private double calculateClusterDistance(CaBenh caBenh) {
        // Hệ số mức độ nghiêm trọng (0-1)
        double severityFactor = calculateSeverityFactor(caBenh.getBenh());

        // Hệ số mật độ trang trại (0-1)
        double densityFactor = calculateDensityFactor(caBenh.getTrangTrai());

        // Hệ số khả năng lây lan (0-1)
        double transmissionFactor = calculateTransmissionFactor(caBenh.getBenh());

        // Tính toán khoảng cách tổng hợp
        double distance = BASE_DISTANCE * (SEVERITY_WEIGHT * severityFactor +
                DENSITY_WEIGHT * densityFactor +
                TRANSMISSION_WEIGHT * transmissionFactor);

        // Giới hạn trong khoảng cho phép
        return Math.min(Math.max(distance, MIN_DISTANCE), MAX_DISTANCE);
    }

    private double calculateSeverityFactor(Benh benh) {
        // Fetch fresh entity with mucDoBenhs loaded
        Benh freshBenh = benhService.findById(benh.getId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bệnh với ID: " + benh.getId()));

        Set<MucDoBenhEnum> mucDoSet = freshBenh.getMucDoBenhs();
        if (mucDoSet == null || mucDoSet.isEmpty()) {
            return 0.5;
        }

        return mucDoSet.stream()
                .mapToDouble(mucDo -> DISEASE_SEVERITY_FACTORS.getOrDefault(mucDo, 0.3))
                .max()
                .orElse(0.5);
    }

    private double calculateDensityFactor(TrangTrai trangTrai) {
        // TÌm các trang trại gần kề trong bán kính
        List<TrangTrai> nearbyFarms = trangTraiService.findTrangTraiInRadius(
                trangTrai.getPoint(),
                DENSITY_CHECK_RADIUS);

        // Tính toán số trang trại tối đa dựa trên diện tích và khoảng cách tối thiểu
        double maxTheoretical = Math.PI * Math.pow(DENSITY_CHECK_RADIUS, 2) /
                Math.pow(FARM_SPACING, 2);

        // Tính toán số trang trại tối đa dựa trên diện tích trung bình của trang trại
        double maxRealistic = (Math.PI * Math.pow(DENSITY_CHECK_RADIUS, 2)) /
                AVERAGE_FARM_SIZE;

        // DÙng giá trị nhỏ hơn của hai giá trị tối đa
        double maxExpectedDensity = Math.min(maxTheoretical, maxRealistic);

        // Calculate normalized density (0-1)
        double density = Math.min(1.0, nearbyFarms.size() / maxExpectedDensity);

        // Apply logarithmic scaling to better represent density impact
        return Math.log10(density * 9 + 1);
    }

    private double calculateTransmissionFactor(Benh benh) {
        // Fetch fresh entity with collections loaded
        Benh freshBenh = benhService.findById(benh.getId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bệnh với ID: " + benh.getId()));

        double baseTransmissionScore = 0.5;

        if (freshBenh.getCanCongBoDich() != null && freshBenh.getCanCongBoDich()) {
            baseTransmissionScore += 0.3;
        }
        if (freshBenh.getCanPhongBenhBatBuoc() != null && freshBenh.getCanPhongBenhBatBuoc()) {
            baseTransmissionScore += 0.2;
        }

        double speciesAffectedFactor = 0.0;
        Set<LoaiVatNuoi> loaiVatNuoi = freshBenh.getLoaiVatNuoi();
        if (loaiVatNuoi != null) {
            speciesAffectedFactor = Math.min(loaiVatNuoi.size() * 0.2, 0.4);
        }

        return Math.min(baseTransmissionScore + speciesAffectedFactor, 1.0);
    }

    private Geometry calculateClusterCentroid(List<CaBenh> cluster) {
        return geometryService.calculateCentroid(
                cluster.stream()
                        .map(cb -> cb.getTrangTrai().getPoint())
                        .collect(Collectors.toList()));
    }
}