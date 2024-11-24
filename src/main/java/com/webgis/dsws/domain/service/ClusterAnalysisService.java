
package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.model.CaBenh;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
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
    private static final double DEFAULT_CLUSTER_DISTANCE = 1000.0; // 1km

    /**
     * Nhóm các ca bệnh theo khoảng cách địa lý
     * 
     * @param caBenhs Danh sách ca bệnh cần phân tích
     * @return Map chứa geometry trung tâm và danh sách ca bệnh trong cluster
     */
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
        double distance = geometryService.calculateDistance(
                center.getTrangTrai().getPoint(),
                other.getTrangTrai().getPoint());
        return distance <= DEFAULT_CLUSTER_DISTANCE;
    }

    private Geometry calculateClusterCentroid(List<CaBenh> cluster) {
        return geometryService.calculateCentroid(
                cluster.stream()
                        .map(cb -> cb.getTrangTrai().getPoint())
                        .collect(Collectors.toList()));
    }
}