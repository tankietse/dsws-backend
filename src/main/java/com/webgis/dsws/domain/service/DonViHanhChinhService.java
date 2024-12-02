package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.model.DonViHanhChinh;
import com.webgis.dsws.domain.repository.DonViHanhChinhRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

@Service
public class DonViHanhChinhService {

    @Autowired
    private DonViHanhChinhRepository donViHanhChinhRepository;

    public List<DonViHanhChinh> findAll() {
        return donViHanhChinhRepository.findAll();
    }

    public DonViHanhChinh findById(Integer id) {
        return donViHanhChinhRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn vị hành chính với ID: " + id));
    }

    public List<DonViHanhChinh> findByCapHanhChinh(String capHanhChinh) {
        return donViHanhChinhRepository.findByCapHanhChinh(capHanhChinh);
    }

    public List<DonViHanhChinh> findByDonViCha(DonViHanhChinh donViCha) {
        return donViHanhChinhRepository.findByDonViCha(donViCha);
    }

    public List<DonViHanhChinh> searchByName(String name) {
        return donViHanhChinhRepository.findByTenContainingIgnoreCaseAndDiacritics(name);
    }

    @Transactional
    public DonViHanhChinh save(DonViHanhChinh donViHanhChinh) {
        validateDonViHanhChinh(donViHanhChinh);
        return donViHanhChinhRepository.save(donViHanhChinh);
    }

    @Transactional
    public DonViHanhChinh update(Integer id, DonViHanhChinh donViHanhChinhDetails) {
        DonViHanhChinh existingDonViHanhChinh = findById(id);

        existingDonViHanhChinh.setTen(donViHanhChinhDetails.getTen());
        existingDonViHanhChinh.setTenTiengAnh(donViHanhChinhDetails.getTenTiengAnh());
        existingDonViHanhChinh.setCapHanhChinh(donViHanhChinhDetails.getCapHanhChinh());
        existingDonViHanhChinh.setAdminLevel(donViHanhChinhDetails.getAdminLevel());
        existingDonViHanhChinh.setRanhGioi(donViHanhChinhDetails.getRanhGioi());
        existingDonViHanhChinh.setCenterPoint(donViHanhChinhDetails.getCenterPoint());

        if (donViHanhChinhDetails.getDonViCha() != null) {
            existingDonViHanhChinh.setDonViCha(donViHanhChinhDetails.getDonViCha());
        }

        validateDonViHanhChinh(existingDonViHanhChinh);
        return donViHanhChinhRepository.save(existingDonViHanhChinh);
    }

    @Transactional
    public void delete(Integer id) {
        if (!donViHanhChinhRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy đơn vị hành chính với ID: " + id);
        }
        donViHanhChinhRepository.deleteById(id);
    }

    public List<Integer> getAllChildrenIds(Integer rootId) {
        return donViHanhChinhRepository.findAllChildrenIds(rootId);
    }

    public List<Integer> getAllDescendantIds(Integer rootId) {
        return donViHanhChinhRepository.findAllDescendantIds(rootId);
    }

    public Map<String, Object> getGeoJSONByCapHanhChinh(String capHanhChinh) {
        List<DonViHanhChinh> donViHanhChinhs = findByCapHanhChinh(capHanhChinh);
        Map<String, Object> featureCollection = new HashMap<>();
        featureCollection.put("type", "FeatureCollection");

        List<Map<String, Object>> features = donViHanhChinhs.stream()
                .filter(dv -> dv.getRanhGioi() != null)
                .map(dv -> {
                    Map<String, Object> feature = new HashMap<>();
                    feature.put("type", "Feature");

                    // Add geometry directly from ranhGioi
                    Map<String, Object> geometry = new HashMap<>();
                    geometry.put("type", dv.getRanhGioi().getGeometryType());
                    geometry.put("coordinates", extractCoordinates(dv.getRanhGioi()));
                    feature.put("geometry", geometry);

                    // Add properties
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("id", dv.getId());
                    properties.put("ten", dv.getTen());
                    properties.put("tenTiengAnh", dv.getTenTiengAnh());
                    properties.put("capHanhChinh", dv.getCapHanhChinh());
                    properties.put("adminLevel", dv.getAdminLevel());
                    feature.put("properties", properties);

                    return feature;
                })
                .collect(Collectors.toList());

        featureCollection.put("features", features);
        return featureCollection;
    }

    public Object extractCoordinates(Geometry geometry) {
        if (geometry == null) {
            return null;
        }

        try {
            // Fix geometry type handling
            String geoType = geometry.getGeometryType();
            switch (geoType) {
                case "Polygon":
                    return extractPolygonCoordinates((Polygon) geometry);
                case "MultiPolygon":
                    return extractMultiPolygonCoordinates((MultiPolygon) geometry);
                default:
                    System.err.println("Unsupported geometry type: " + geoType);
                    return null;
            }
        } catch (Exception e) {
            System.err.println("Error extracting coordinates: " + e.getMessage());
            return null;
        }
    }

    private List<List<List<Double>>> extractPolygonCoordinates(Polygon polygon) {
        List<List<List<Double>>> polygonCoords = new ArrayList<>();

        try {
            // Extract exterior ring
            Coordinate[] exteriorCoords = polygon.getExteriorRing().getCoordinates();
            List<List<Double>> exteriorRing = new ArrayList<>();
            for (Coordinate coord : exteriorCoords) {
                if (!Double.isNaN(coord.x) && !Double.isNaN(coord.y)) {
                    exteriorRing.add(Arrays.asList(coord.x, coord.y));
                }
            }

            // Ensure the ring is closed
            if (!exteriorRing.isEmpty() && !exteriorRing.get(0).equals(exteriorRing.get(exteriorRing.size() - 1))) {
                exteriorRing.add(exteriorRing.get(0));
            }
            polygonCoords.add(exteriorRing);

            // Extract interior rings
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                List<List<Double>> interiorRing = new ArrayList<>();
                Coordinate[] interiorCoords = polygon.getInteriorRingN(i).getCoordinates();
                for (Coordinate coord : interiorCoords) {
                    if (!Double.isNaN(coord.x) && !Double.isNaN(coord.y)) {
                        interiorRing.add(Arrays.asList(coord.x, coord.y));
                    }
                }
                if (!interiorRing.isEmpty() && !interiorRing.get(0).equals(interiorRing.get(interiorRing.size() - 1))) {
                    interiorRing.add(interiorRing.get(0));
                }
                polygonCoords.add(interiorRing);
            }
        } catch (Exception e) {
            System.err.println("Error extracting polygon coordinates: " + e.getMessage());
            return null;
        }

        return polygonCoords;
    }

    private List<List<List<List<Double>>>> extractMultiPolygonCoordinates(Geometry geometry) {
        List<List<List<List<Double>>>> multiPolygonCoords = new ArrayList<>();
        MultiPolygon multiPolygon = (MultiPolygon) geometry;
        int numGeometries = multiPolygon.getNumGeometries();

        for (int i = 0; i < numGeometries; i++) {
            Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
            multiPolygonCoords.add(extractPolygonCoordinates(polygon));
        }

        return multiPolygonCoords;
    }

    private void validateDonViHanhChinh(DonViHanhChinh donViHanhChinh) {
        if (donViHanhChinh.getTen() == null || donViHanhChinh.getTen().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đơn vị hành chính không được để trống");
        }

        if (donViHanhChinh.getAdminLevel() == null) {
            throw new IllegalArgumentException("Cấp hành chính không được để trống");
        }

        if (donViHanhChinh.getRanhGioi() == null) {
            throw new IllegalArgumentException("Ranh giới không được để trống");
        }

        // Kiểm tra trùng lặp
        boolean exists = donViHanhChinhRepository.existsByTenAndAdminLevelAndRanhGioi(
                donViHanhChinh.getTen(),
                donViHanhChinh.getAdminLevel(),
                donViHanhChinh.getRanhGioi());

        if (exists && donViHanhChinh.getId() == null) {
            throw new IllegalArgumentException("Đơn vị hành chính đã tồn tại");
        }
    }

    @Transactional(readOnly = true)
    public List<DonViHanhChinh> findByParentId(Integer parentId) {
        if (parentId == null) {
            return Collections.emptyList();
        }
        DonViHanhChinh parent = findById(parentId);
        return donViHanhChinhRepository.findByDonViCha(parent);
    }
}
