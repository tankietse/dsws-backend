package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.model.CaBenh;
import com.webgis.dsws.domain.model.DonViHanhChinh;
import com.webgis.dsws.domain.model.TrangTrai;
import com.webgis.dsws.domain.model.Benh;
import com.webgis.dsws.domain.repository.CaBenhRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import org.locationtech.jts.geom.Geometry;

@Service
@RequiredArgsConstructor
public class CaBenhService {
    private final CaBenhRepository caBenhRepository;
    private final TrangTraiService trangTraiService;
    private final DonViHanhChinhService donViHanhChinhService;

    @Transactional(readOnly = true)
    public Map<String, Object> getCaBenhGeoJSON(Date fromDate, Date toDate, String maTinhThanh, String loaiBenh,
            boolean chiHienThiChuaKetThuc) {
        List<CaBenh> caBenhs = caBenhRepository.findByFilters(fromDate, toDate, maTinhThanh, loaiBenh,
                chiHienThiChuaKetThuc);

        Map<String, Object> featureCollection = new HashMap<>();
        featureCollection.put("type", "FeatureCollection");

        List<Map<String, Object>> features = new ArrayList<>();

        for (CaBenh caBenh : caBenhs) {
            TrangTrai trangTrai = caBenh.getTrangTrai();
            if (trangTrai != null && trangTrai.getPoint() != null) {
                Map<String, Object> feature = new HashMap<>();
                feature.put("type", "Feature");

                // Add geometry
                Map<String, Object> geometry = new HashMap<>();
                geometry.put("type", "Point");
                geometry.put("coordinates", Arrays.asList(
                        trangTrai.getPoint().getX(),
                        trangTrai.getPoint().getY()));
                feature.put("geometry", geometry);

                // Add properties
                Map<String, Object> properties = new HashMap<>();
                properties.put("id", caBenh.getId());
                properties.put("trangTraiId", trangTrai.getId());
                properties.put("tenTrangTrai", trangTrai.getTenTrangTrai());
                properties.put("tenBenh", caBenh.getBenh().getTenBenh());
                properties.put("ngayPhatHien", caBenh.getNgayPhatHien());
                properties.put("soCaNhiem", caBenh.getSoCaNhiemBanDau());
                properties.put("soCaTuVong", caBenh.getSoCaTuVongBanDau());
                properties.put("diaChiDayDu", trangTrai.getDiaChiDayDu());
                properties.put("trangThaiHoatDong", caBenh.getDaKetThuc() ? "Đã kết thúc" : "Đang diễn ra");

                feature.put("properties", properties);
                features.add(feature);
            }
        }

        featureCollection.put("features", features);
        return featureCollection;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getThongKeCaBenh(String maTinhThanh, Date fromDate, Date toDate, String loaiBenh,
            boolean chiHienThiChuaKetThuc) {
        List<CaBenh> caBenhs = caBenhRepository.findByFilters(fromDate, toDate, maTinhThanh, null, false);

        Map<String, Object> thongKe = new HashMap<>();
        thongKe.put("tongSoCa", caBenhs.size());

        // Thống kê theo bệnh
        Map<String, Integer> thongKeBenh = new HashMap<>();
        Map<String, Integer> thongKeTuVong = new HashMap<>();

        for (CaBenh caBenh : caBenhs) {
            String tenBenh = caBenh.getBenh().getTenBenh();
            thongKeBenh.merge(tenBenh, 1, Integer::sum);
            thongKeTuVong.merge(tenBenh, caBenh.getSoCaTuVongBanDau(), Integer::sum);
        }

        thongKe.put("thongKeTheoBenhCaNhiem", thongKeBenh);
        thongKe.put("thongKeTheoBenhTuVong", thongKeTuVong);

        return thongKe;
    }

    @Transactional
    public CaBenh createCaBenh(CaBenh caBenh) {
        caBenh.setNgayTao(new Date());
        caBenh.setDaKetThuc(false);

        // Validate bệnh và trang trại
        if (caBenh.getBenh() == null || caBenh.getTrangTrai() == null) {
            throw new IllegalArgumentException("Bệnh và trang trại không được để trống");
        }

        // Kiểm tra trùng lặp ca bệnh
        boolean caBenhExists = caBenhRepository.findAll().stream()
                .anyMatch(cb -> cb.getTrangTrai().getId().equals(caBenh.getTrangTrai().getId())
                        && cb.getBenh().getId().equals(caBenh.getBenh().getId())
                        && !cb.getDaKetThuc());

        if (caBenhExists) {
            throw new IllegalStateException("Đã tồn tại ca bệnh chưa kết thúc cho bệnh này tại trang trại");
        }

        return caBenhRepository.save(caBenh);
    }

    @Transactional
    public CaBenh endCaBenh(Long id, String lyDo) {
        CaBenh caBenh = caBenhRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy ca bệnh với ID: " + id));

        if (caBenh.getDaKetThuc()) {
            throw new IllegalStateException("Ca bệnh đã kết thúc trước đó");
        }

        caBenh.setDaKetThuc(true);
        caBenh.setMoTaBanDau(lyDo != null ? lyDo : "Kết thúc ca bệnh");
        caBenh.setNgayDuyet(new Date());

        return caBenhRepository.save(caBenh);
    }

    @Transactional(readOnly = true)
    public List<CaBenh> findCaBenhsInRadius(double longitude, double latitude, double radiusInMeters) {
        return caBenhRepository.findCaBenhsWithinRadius(longitude, latitude, radiusInMeters);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCaBenhByRegionGeoJSON(String targetCapHanhChinh) {

        List<CaBenh> caBenhs = caBenhRepository.findAllWithRegions();

        // Maps to hold aggregated data per region
        Map<Integer, Integer> totalCasesMap = new HashMap<>();
        Map<Integer, Set<String>> diseaseTypesMap = new HashMap<>();
        Map<Integer, Map<String, Integer>> diseaseCasesMap = new HashMap<>();
        Map<Integer, List<Map<String, Object>>> diseaseDetailsMap = new HashMap<>();

        for (CaBenh caBenh : caBenhs) {
            // Get the administrative unit of the TrangTrai
            DonViHanhChinh donViHanhChinh = caBenh.getTrangTrai().getDonViHanhChinh();

            // Traverse up the administrative hierarchy to find the unit at the target
            // capHanhChinh
            while (donViHanhChinh != null && !donViHanhChinh.getCapHanhChinh().equalsIgnoreCase(targetCapHanhChinh)) {
                donViHanhChinh = donViHanhChinh.getDonViCha();
            }

            if (donViHanhChinh != null) {
                Integer regionId = donViHanhChinh.getId();
                String tenBenh = caBenh.getBenh().getTenBenh();
                int soCaNhiemBanDau = caBenh.getSoCaNhiemBanDau();

                // Update total cases
                totalCasesMap.merge(regionId, soCaNhiemBanDau, Integer::sum);

                // Update disease types
                diseaseTypesMap.computeIfAbsent(regionId, k -> new HashSet<>()).add(tenBenh);

                // Update disease-specific case counts
                diseaseCasesMap.computeIfAbsent(regionId, k -> new HashMap<>())
                        .merge(tenBenh, soCaNhiemBanDau, Integer::sum);

                // Update disease details with properly serialized mucDoBenh
                Map<String, Object> diseaseDetail = new HashMap<>();
                diseaseDetail.put("tenBenh", tenBenh);

                // Convert Set<MucDoBenhEnum> to List<String>
                List<String> mucDoBenhList = new ArrayList<>();
                if (caBenh.getBenh().getMucDoBenhs() != null) {
                    mucDoBenhList = caBenh.getBenh().getMucDoBenhs().stream()
                            .map(Enum::name)
                            .collect(Collectors.toList());
                }
                diseaseDetail.put("mucDoBenh", mucDoBenhList);

                diseaseDetailsMap.computeIfAbsent(regionId, k -> new ArrayList<>()).add(diseaseDetail);
            }
        }

        // Get regions by administrative level
        List<DonViHanhChinh> regions = donViHanhChinhService.findByCapHanhChinh(targetCapHanhChinh);

        Map<String, Object> featureCollection = new HashMap<>();
        featureCollection.put("type", "FeatureCollection");

        List<Map<String, Object>> features = new ArrayList<>();

        for (DonViHanhChinh region : regions) {
            if (region.getRanhGioi() != null) {
                try {
                    Map<String, Object> feature = new HashMap<>();
                    feature.put("type", "Feature");

                    // Add geometry with validation
                    Object coordinates = donViHanhChinhService.extractCoordinates(region.getRanhGioi());
                    if (coordinates != null) {
                        Map<String, Object> geometry = new HashMap<>();
                        String geoType = region.getRanhGioi().getGeometryType();
                        // Ensure proper GeoJSON type names
                        geometry.put("type", geoType.equals("MultiPolygon") ? "MultiPolygon" : "Polygon");
                        geometry.put("coordinates", coordinates);
                        feature.put("geometry", geometry);

                        // Add properties
                        Map<String, Object> properties = new HashMap<>();
                        properties.put("id", region.getId());
                        properties.put("ten", region.getTen());
                        properties.put("totalCases", totalCasesMap.getOrDefault(region.getId(), 0));
                        properties.put("diseaseTypes",
                                new ArrayList<>(diseaseTypesMap.getOrDefault(region.getId(), Collections.emptySet())));
                        properties.put("diseaseCases",
                                diseaseCasesMap.getOrDefault(region.getId(), Collections.emptyMap()));
                        properties.put("diseaseDetails",
                                diseaseDetailsMap.getOrDefault(region.getId(), Collections.emptyList()));

                        feature.put("properties", properties);
                        features.add(feature);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing region " + region.getId() + ": " + e.getMessage());
                }
            }
        }

        featureCollection.put("features", features);
        return featureCollection;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCaBenhGroupedByDiseaseGeoJSON() {

        List<CaBenh> caBenhs = caBenhRepository.findAllWithRegions();

        // Map to hold features grouped by disease types
        Map<String, List<Map<String, Object>>> diseaseFeaturesMap = new HashMap<>();

        // Map to hold statistics per disease
        Map<String, Integer> diseaseCaseCountMap = new HashMap<>();

        for (CaBenh caBenh : caBenhs) {
            String tenBenh = caBenh.getBenh().getTenBenh();
            TrangTrai trangTrai = caBenh.getTrangTrai();

            if (trangTrai != null && trangTrai.getPoint() != null) {
                Map<String, Object> feature = new HashMap<>();
                feature.put("type", "Feature");

                // Add geometry
                Map<String, Object> geometry = new HashMap<>();
                geometry.put("type", "Point");
                geometry.put("coordinates", Arrays.asList(
                        trangTrai.getPoint().getX(),
                        trangTrai.getPoint().getY()));
                feature.put("geometry", geometry);

                // Add properties
                Map<String, Object> properties = new HashMap<>();
                properties.put("id", caBenh.getId());
                properties.put("trangTraiId", trangTrai.getId());
                properties.put("tenTrangTrai", trangTrai.getTenTrangTrai());
                properties.put("tenBenh", tenBenh);
                properties.put("ngayPhatHien", caBenh.getNgayPhatHien());
                properties.put("soCaNhiem", caBenh.getSoCaNhiemBanDau());
                properties.put("soCaTuVong", caBenh.getSoCaTuVongBanDau());
                properties.put("diaChiDayDu", trangTrai.getDiaChiDayDu());
                properties.put("trangThaiHoatDong", caBenh.getDaKetThuc() ? "Đã kết thúc" : "Đang diễn ra");

                feature.put("properties", properties);

                // Group features by disease
                diseaseFeaturesMap.computeIfAbsent(tenBenh, k -> new ArrayList<>()).add(feature);

                // Update disease case count
                int soCaNhiemBanDau = caBenh.getSoCaNhiemBanDau();
                diseaseCaseCountMap.merge(tenBenh, soCaNhiemBanDau, Integer::sum);
            }
        }

        // Prepare the final GeoJSON structure
        Map<String, Object> result = new HashMap<>();
        result.put("type", "FeatureCollection");

        List<Map<String, Object>> features = new ArrayList<>();

        // Flatten features with disease information
        for (Map.Entry<String, List<Map<String, Object>>> entry : diseaseFeaturesMap.entrySet()) {
            String tenBenh = entry.getKey();
            List<Map<String, Object>> diseaseFeatures = entry.getValue();
            int totalCases = diseaseCaseCountMap.getOrDefault(tenBenh, 0);

            // Add disease statistics as properties to each feature
            for (Map<String, Object> feature : diseaseFeatures) {
                Map<String, Object> properties = (Map<String, Object>) feature.get("properties");
                properties.put("totalCases", totalCases);
            }

            features.addAll(diseaseFeatures);
        }

        result.put("features", features);
        return result;
    }
}