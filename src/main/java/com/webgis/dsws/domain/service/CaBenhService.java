package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.model.CaBenh;
import com.webgis.dsws.domain.model.DonViHanhChinh;
import com.webgis.dsws.domain.model.NguoiDung;
import com.webgis.dsws.domain.model.TrangTrai;
import com.webgis.dsws.domain.model.TrangTraiVatNuoi;
import com.webgis.dsws.domain.model.VungDich;
import com.webgis.dsws.domain.model.enums.MucDoBenhEnum;
import com.webgis.dsws.domain.model.enums.TrangThaiEnum;
import com.webgis.dsws.domain.dto.CaBenhStatisticsDTO;
import com.webgis.dsws.domain.model.Benh;
import com.webgis.dsws.domain.repository.CaBenhRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class CaBenhService {
    private final CaBenhRepository caBenhRepository;
    private final TrangTraiService trangTraiService;
    private final GeometryService geometryService;
    private final DonViHanhChinhService donViHanhChinhService;
    private BenhService benhService;

    private VungDichService vungDichService;
    private VungDichAutoImportService vungDichAutoImportService;

    @Transactional(readOnly = true)
    public Map<String, Object> getCaBenhGeoJSON(Date fromDate, Date toDate, String maTinhThanh, String loaiBenh,
            boolean chiHienThiChuaKetThuc) {
        Specification<CaBenh> spec = buildCaBenhSpecification(fromDate, toDate, maTinhThanh, loaiBenh,
                chiHienThiChuaKetThuc);

        List<CaBenh> caBenhs = caBenhRepository.findAll(spec);

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
    public Page<CaBenh> findAll(Pageable pageable) {
        return caBenhRepository.findAllWithFullDetails(pageable);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getThongKeCaBenh(String maTinhThanh, Date fromDate, Date toDate, String loaiBenh,
            boolean chiHienThiChuaKetThuc) {
        Specification<CaBenh> spec = buildCaBenhSpecification(fromDate, toDate, maTinhThanh, loaiBenh,
                chiHienThiChuaKetThuc);

        List<CaBenh> caBenhs = caBenhRepository.findAll(spec);

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

    // Add a method to build the specification
    private Specification<CaBenh> buildCaBenhSpecification(Date fromDate, Date toDate, String maTinhThanh,
            String loaiBenh, boolean chiHienThiChuaKetThuc) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("ngayPhatHien"), fromDate));
            }
            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("ngayPhatHien"), toDate));
            }
            if (maTinhThanh != null) {
                predicates.add(
                        criteriaBuilder.equal(root.join("trangTrai").join("donViHanhChinh").get("id"), maTinhThanh));
            }
            if (loaiBenh != null) {
                predicates.add(criteriaBuilder.equal(root.join("benh").get("tenBenh"), loaiBenh));
            }
            if (chiHienThiChuaKetThuc) {
                predicates.add(criteriaBuilder.isFalse(root.get("daKetThuc")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public CaBenh createCaBenh(CaBenh caBenh) {
        // Set relationships based on IDs
        if (caBenh.getTrangTrai().getId() != null) {
            TrangTrai trangTrai = trangTraiService.findById(caBenh.getTrangTrai().getId());
            caBenh.setTrangTrai(trangTrai);
        }

        if (caBenh.getBenh().getId() != null) {
            Benh benh = benhService.findById(caBenh.getBenh().getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Không tìm thấy bệnh với ID: " + caBenh.getBenh().getId()));
            caBenh.setBenh(benh);
        }

        caBenh.setNgayTao(new Date());
        caBenh.setDaKetThuc(false);

        // Validate bệnh và trang trại
        if (caBenh.getBenh() == null || caBenh.getTrangTrai() == null) {
            throw new IllegalArgumentException("Bệnh và trang trại không được để trống");
        }

        // Create final copy of caBenh for use in lambda
        final CaBenh finalCaBenh = caBenh;
        // Kiểm tra trùng lặp ca bệnh
        boolean caBenhExists = caBenhRepository.findAll().stream()
                .anyMatch(cb -> cb.getTrangTrai().getId().equals(finalCaBenh.getTrangTrai().getId())
                        && cb.getBenh().getId().equals(finalCaBenh.getBenh().getId())
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

    @Transactional
    public CaBenh createCaBenh(CaBenh caBenh, NguoiDung nguoiTao) {
        caBenh.setNgayTao(new Date());
        caBenh.setDaKetThuc(false);
        caBenh.setTrangThai(TrangThaiEnum.PENDING);
        caBenh.setNguoiTao(nguoiTao);

        // Validate bệnh và trang trại
        if (caBenh.getBenh() == null || caBenh.getTrangTrai() == null) {
            throw new IllegalArgumentException("Bệnh và trang trại không được để trống");
        }

        // Create final copy of caBenh for use in lambda
        final CaBenh finalCaBenh = caBenh;
        // Kiểm tra trùng lặp ca bệnh
        boolean caBenhExists = caBenhRepository.findAll().stream()
                .anyMatch(cb -> cb.getTrangTrai().getId().equals(finalCaBenh.getTrangTrai().getId())
                        && cb.getBenh().getId().equals(finalCaBenh.getBenh().getId())
                        && !cb.getDaKetThuc());

        if (caBenhExists) {
            throw new IllegalStateException("Đã tồn tại ca bệnh chưa kết thúc cho bệnh này tại trang trại");
        }

        // Save case first
        caBenh = caBenhRepository.save(caBenh);

        // Check conditions and auto-create zone if needed
        if (shouldCreateNewZone(caBenh)) {
            vungDichAutoImportService.autoCreateZoneForCase(caBenh);
        } else {
            // Try to add to existing zone
            vungDichService.addCaBenhToExistingZone(caBenh);
        }

        return caBenh;
    }

    @Transactional
    public CaBenh updateCaBenh(Long id, CaBenh caBenhDetails) {
        CaBenh existingCaBenh = findById(id);

        // Check if status or severity changed
        boolean statusChanged = !existingCaBenh.getTrangThai().equals(caBenhDetails.getTrangThai());
        boolean severityChanged = existingCaBenh.getSoCaNhiemBanDau() != caBenhDetails.getSoCaNhiemBanDau()
                || existingCaBenh.getSoCaTuVongBanDau() != caBenhDetails.getSoCaTuVongBanDau();

        // Update case details
        updateCaBenhDetails(existingCaBenh, caBenhDetails);
        existingCaBenh = caBenhRepository.save(existingCaBenh);

        // Handle zone updates if needed
        if (statusChanged || severityChanged) {
            handleZoneUpdates(existingCaBenh);
        }

        return existingCaBenh;
    }

    private boolean shouldCreateNewZone(CaBenh caBenh) {
        // Check for high severity conditions
        if (caBenh.getBenh().getMucDoBenhs().contains(MucDoBenhEnum.BANG_A)) {
            return true;
        }

        // Check infection rate
        TrangTrai trangTrai = caBenh.getTrangTrai();
        double totalAnimals = trangTrai.getTrangTraiVatNuois().stream()
                .filter(ttv -> caBenh.getBenh().getLoaiVatNuoi().contains(ttv.getLoaiVatNuoi()))
                .mapToDouble(TrangTraiVatNuoi::getSoLuong)
                .sum();

        if (totalAnimals > 0) {
            double infectionRate = caBenh.getSoCaNhiemBanDau() / totalAnimals;
            if (infectionRate >= 0.2) { // 20% threshold
                return true;
            }
        }

        return false;
    }

    private void handleZoneUpdates(CaBenh caBenh) {
        // Get related zones
        Set<VungDich> relatedZones = vungDichService.findZonesForCase(caBenh);

        if (caBenh.getTrangThai() == TrangThaiEnum.REJECTED) {
            // Remove case from zones if rejected
            relatedZones.forEach(zone -> vungDichService.removeCaBenhFromZone(caBenh, zone));
        } else {
            // Update zone severity and metrics
            relatedZones.forEach(zone -> vungDichService.recalculateZoneMetrics(zone));
        }
    }

    @Transactional
    public CaBenh thayDoiCaBenh(CaBenh caBenh, NguoiDung nguoiDung) {
        caBenh.setTrangThai(TrangThaiEnum.PENDING);
        caBenh.setNguoiTao(nguoiDung);
        caBenh.setNgayTao(new Date(System.currentTimeMillis()));
        return caBenhRepository.save(caBenh);
    }

    @Transactional
    public CaBenh duyetCaBenh(Long caBenhId, NguoiDung nguoiQuanLy, boolean approved) {
        CaBenh caBenh = caBenhRepository.findById(caBenhId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy ca bệnh"));

        if (approved) {
            caBenh.setTrangThai(TrangThaiEnum.APPROVED);
        } else {
            caBenh.setTrangThai(TrangThaiEnum.REJECTED);
        }

        caBenh.setNguoiDuyet(nguoiQuanLy);
        caBenh.setNgayDuyet(new Date(System.currentTimeMillis()));

        return caBenhRepository.save(caBenh);
    }

    @Transactional(readOnly = true)
    public Page<CaBenh> findByTrangThai(TrangThaiEnum trangThaiEnum, Pageable pageable) {
        return caBenhRepository.findByTrangThaiWithDetails(trangThaiEnum, pageable);
    }

    @Transactional(readOnly = true)
    public CaBenh findById(Long id) {
        return caBenhRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy ca bệnh với ID: " + id));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCaBenhByRegionGeoJSON(
            String capHanhChinh,
            Long benhId,
            String mucDoBenh,
            Long loaiVatNuoiId,
            Date fromDate,
            Date toDate) {

        try {
            final MucDoBenhEnum mucDoBenhEnum = mucDoBenh != null ? MucDoBenhEnum.valueOf(mucDoBenh.toUpperCase())
                    : MucDoBenhEnum.BANG_A;
            if (mucDoBenh != null) {
                try {
                    MucDoBenhEnum.valueOf(mucDoBenh.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Mức độ bệnh không hợp lệ: " + mucDoBenh);
                }
            }

            // Build specification
            Specification<CaBenh> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (capHanhChinh != null) {
                    predicates.add(cb.equal(
                            root.join("trangTrai").join("donViHanhChinh").get("capHanhChinh"), capHanhChinh));
                }
                if (benhId != null) {
                    predicates.add(cb.equal(root.join("benh").get("id"), benhId));
                }
                if (mucDoBenhEnum != null) {
                    predicates.add(cb.isMember(mucDoBenhEnum, root.join("benh").get("mucDoBenhs")));
                }
                if (loaiVatNuoiId != null) {
                    predicates.add(cb.equal(
                            root.join("trangTrai").join("trangTraiVatNuois").join("loaiVatNuoi").get("id"),
                            loaiVatNuoiId));
                }
                if (fromDate != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("ngayPhatHien"), fromDate));
                }
                if (toDate != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("ngayPhatHien"), toDate));
                }
                // Ensure ranhGioi is not null
                predicates.add(cb.isNotNull(root.join("trangTrai").join("donViHanhChinh").get("ranhGioi")));

                return cb.and(predicates.toArray(new Predicate[0]));
            };

            List<CaBenh> caBenhs = caBenhRepository.findAll(spec);

            Map<String, Object> featureCollection = new HashMap<>();
            featureCollection.put("type", "FeatureCollection");

            List<Map<String, Object>> features = processFeatures(caBenhs);
            featureCollection.put("features", features);

            return featureCollection;

        } catch (Exception e) {
            throw new RuntimeException("Error processing data: " + e.getMessage(), e);
        }
    }

    // Extract feature processing to separate method
    private List<Map<String, Object>> processFeatures(List<CaBenh> caBenhs) {
        // Group cases by administrative region
        Map<DonViHanhChinh, List<CaBenh>> casesByRegion = caBenhs.stream()
                .collect(Collectors.groupingBy(caBenh -> caBenh.getTrangTrai().getDonViHanhChinh()));

        return casesByRegion.entrySet().stream()
                .map(entry -> createFeature(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private Map<String, Object> createFeature(DonViHanhChinh region, List<CaBenh> cases) {
        Map<String, Object> feature = new HashMap<>();
        feature.put("type", "Feature");

        // Add geometry
        Map<String, Object> geometry = new HashMap<>();
        geometry.put("type", region.getRanhGioi().getGeometryType());
        geometry.put("coordinates", geometryService.extractCoordinates(region.getRanhGioi()));
        feature.put("geometry", geometry);

        // Add properties
        Map<String, Object> properties = new HashMap<>();
        properties.put("id", region.getId());
        properties.put("ten", region.getTen());
        properties.put("capHanhChinh", region.getCapHanhChinh());
        properties.put("totalCases", cases.size());
        properties.put("caBenhs", getDetailedCaseInfo(cases));
        feature.put("properties", properties);

        return feature;
    }

    private List<Map<String, Object>> getDetailedCaseInfo(List<CaBenh> caBenhs) {
        return caBenhs.stream()
                .map(caBenh -> {
                    Map<String, Object> caseInfo = new HashMap<>();
                    caseInfo.put("id", caBenh.getId());
                    caseInfo.put("benh", caBenh.getBenh().getTenBenh());
                    caseInfo.put("ngayPhatHien", caBenh.getNgayPhatHien());
                    caseInfo.put("soCaNhiem", caBenh.getSoCaNhiemBanDau());
                    caseInfo.put("trangThai", caBenh.getTrangThai());
                    caseInfo.put("daKetThuc", caBenh.getDaKetThuc());
                    return caseInfo;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CaBenhStatisticsDTO getThongKe() {
        List<CaBenh> caBenhs = caBenhRepository.findAllWithDetails();
        CaBenhStatisticsDTO statistics = new CaBenhStatisticsDTO();

        // Calculate general statistics
        Map<String, Object> generalStats = calculateGeneralStats(caBenhs);
        statistics.setGeneralStats(generalStats);

        // Disease distribution
        List<Map<String, Object>> diseaseDistribution = calculateDiseaseDistribution(caBenhs);
        statistics.setDiseaseDistribution(diseaseDistribution);

        // Case trend over time
        List<Map<String, Object>> caseTrend = calculateCaseTrend(caBenhs);
        statistics.setCaseTrend(caseTrend);

        // Recent cases (last 10 cases)
        List<Map<String, Object>> recentCases = getRecentCases(caBenhs);
        statistics.setRecentCases(recentCases);

        // Status distribution
        Map<String, Object> statusDistribution = calculateStatusDistribution(caBenhs);
        statistics.setStatusDistribution(statusDistribution);

        // Severity summary
        Map<String, Object> severitySummary = calculateSeveritySummary(caBenhs);
        statistics.setSeveritySummary(severitySummary);

        // Regional statistics
        List<Map<String, Object>> regionStats = calculateRegionalStats(caBenhs);
        statistics.setRegionStats(regionStats);

        // Monthly comparison
        Map<String, Object> monthlyComparison = calculateMonthlyComparison(caBenhs);
        statistics.setMonthlyComparison(monthlyComparison);

        // Top affected farms
        List<Map<String, Object>> topFarms = calculateTopFarms(caBenhs);
        statistics.setTopFarms(topFarms);

        // Animal type statistics
        Map<String, Object> animalTypeStats = calculateAnimalTypeStats(caBenhs);
        statistics.setAnimalTypeStats(animalTypeStats);

        return statistics;
    }

    private Map<String, Object> calculateGeneralStats(List<CaBenh> caBenhs) {
        Map<String, Object> stats = new HashMap<>();
        long totalCases = caBenhs.size();
        long activeCases = caBenhs.stream().filter(cb -> !cb.getDaKetThuc()).count();
        long recoveredCases = caBenhs.stream().filter(CaBenh::getDaKetThuc).count();
        long severeCases = caBenhs.stream()
                .filter(cb -> cb.getBenh().getMucDoBenhs().contains(MucDoBenhEnum.BANG_A))
                .count();
        long pendingApproval = caBenhs.stream()
                .filter(cb -> cb.getTrangThai() == TrangThaiEnum.PENDING)
                .count();

        stats.put("totalCases", totalCases);
        stats.put("activeCases", activeCases);
        stats.put("recoveredCases", recoveredCases);
        stats.put("severeCases", severeCases);
        stats.put("pendingApproval", pendingApproval);
        stats.put("monthlyGrowthRate", calculateMonthlyGrowthRate(caBenhs));
        stats.put("totalInfected", caBenhs.stream().mapToInt(CaBenh::getSoCaNhiemBanDau).sum());
        stats.put("totalDeaths", caBenhs.stream().mapToInt(CaBenh::getSoCaTuVongBanDau).sum());

        return stats;
    }

    private List<Map<String, Object>> getRecentCases(List<CaBenh> caBenhs) {
        return caBenhs.stream()
                .sorted(Comparator.comparing(CaBenh::getNgayTao).reversed())
                .limit(10)
                .map(caBenh -> {
                    Map<String, Object> caseInfo = new HashMap<>();
                    caseInfo.put("id", caBenh.getId());
                    caseInfo.put("benhId", caBenh.getBenh().getId());
                    caseInfo.put("tenBenh", caBenh.getBenh().getTenBenh());
                    caseInfo.put("trangTrai", caBenh.getTrangTrai().getTenTrangTrai());
                    caseInfo.put("ngayPhatHien", caBenh.getNgayPhatHien());
                    caseInfo.put("trangThai", caBenh.getTrangThai());
                    caseInfo.put("soCaNhiem", caBenh.getSoCaNhiemBanDau());
                    caseInfo.put("soCaTuVong", caBenh.getSoCaTuVongBanDau());
                    caseInfo.put("mucDoBenh", caBenh.getBenh().getMucDoBenhs());
                    return caseInfo;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> calculateStatusDistribution(List<CaBenh> caBenhs) {
        Map<TrangThaiEnum, Long> distribution = caBenhs.stream()
                .collect(Collectors.groupingBy(CaBenh::getTrangThai, Collectors.counting()));

        Map<String, Object> result = new HashMap<>();
        Arrays.stream(TrangThaiEnum.values())
                .forEach(status -> result.put(status.name(), distribution.getOrDefault(status, 0L)));

        return result;
    }

    private List<Map<String, Object>> calculateDiseaseDistribution(List<CaBenh> caBenhs) {
        return caBenhs.stream()
                .collect(Collectors.groupingBy(
                        caBenh -> caBenh.getBenh().getTenBenh(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                cases -> {
                                    Map<String, Object> distribution = new HashMap<>();
                                    CaBenh firstCase = cases.get(0);
                                    distribution.put("name", firstCase.getBenh().getTenBenh());
                                    distribution.put("count", (long) cases.size());
                                    distribution.put("mucDoBenh", firstCase.getBenh().getMucDoBenhs());
                                    distribution.put("soCaNhiem", cases.stream()
                                            .mapToInt(CaBenh::getSoCaNhiemBanDau)
                                            .sum());
                                    distribution.put("soCaTuVong", cases.stream()
                                            .mapToInt(CaBenh::getSoCaTuVongBanDau)
                                            .sum());
                                    return distribution;
                                })))
                .values()
                .stream()
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> calculateCaseTrend(List<CaBenh> caBenhs) {
        // Group cases by date and count
        Map<Date, Long> trendMap = caBenhs.stream()
                .collect(Collectors.groupingBy(
                        caBenh -> {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(caBenh.getNgayPhatHien());
                            cal.set(Calendar.HOUR_OF_DAY, 0);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);
                            return cal.getTime();
                        },
                        Collectors.counting()));

        // Convert to list and sort by date
        return trendMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> trend = new HashMap<>();
                    trend.put("date", entry.getKey());
                    trend.put("count", entry.getValue());
                    // Add additional metrics for the day
                    List<CaBenh> casesForDay = caBenhs.stream()
                            .filter(caBenh -> isSameDay(caBenh.getNgayPhatHien(), entry.getKey()))
                            .collect(Collectors.toList());
                    trend.put("infected", casesForDay.stream().mapToInt(CaBenh::getSoCaNhiemBanDau).sum());
                    trend.put("deaths", casesForDay.stream().mapToInt(CaBenh::getSoCaTuVongBanDau).sum());
                    return trend;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> calculateSeveritySummary(List<CaBenh> caBenhs) {
        Map<String, Object> summary = new HashMap<>();

        // Count cases by severity level
        Map<MucDoBenhEnum, Long> severityCounts = caBenhs.stream()
                .flatMap(caBenh -> caBenh.getBenh().getMucDoBenhs().stream())
                .collect(Collectors.groupingBy(
                        mucDo -> mucDo,
                        Collectors.counting()));

        // Calculate statistics for each severity level
        Arrays.stream(MucDoBenhEnum.values()).forEach(mucDo -> {
            Map<String, Object> levelStats = new HashMap<>();
            levelStats.put("count", severityCounts.getOrDefault(mucDo, 0L));
            levelStats.put("percentage", calculatePercentage(severityCounts.getOrDefault(mucDo, 0L), caBenhs.size()));

            // Calculate infected and death counts for this severity level
            List<CaBenh> casesWithSeverity = caBenhs.stream()
                    .filter(caBenh -> caBenh.getBenh().getMucDoBenhs().contains(mucDo))
                    .collect(Collectors.toList());

            levelStats.put("infected", casesWithSeverity.stream().mapToInt(CaBenh::getSoCaNhiemBanDau).sum());
            levelStats.put("deaths", casesWithSeverity.stream().mapToInt(CaBenh::getSoCaTuVongBanDau).sum());

            summary.put(mucDo.name(), levelStats);
        });

        return summary;
    }

    private List<Map<String, Object>> calculateRegionalStats(List<CaBenh> caBenhs) {
        return caBenhs.stream()
                .collect(Collectors.groupingBy(
                        caBenh -> caBenh.getTrangTrai().getDonViHanhChinh(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                casesInRegion -> {
                                    Map<String, Object> regionStat = new HashMap<>();
                                    DonViHanhChinh region = casesInRegion.get(0).getTrangTrai().getDonViHanhChinh();
                                    regionStat.put("id", region.getId());
                                    regionStat.put("ten", region.getTen());
                                    regionStat.put("capHanhChinh", region.getCapHanhChinh());
                                    regionStat.put("totalCases", casesInRegion.size());
                                    regionStat.put("activeCases", casesInRegion.stream()
                                            .filter(c -> !c.getDaKetThuc())
                                            .count());
                                    regionStat.put("infected", casesInRegion.stream()
                                            .mapToInt(CaBenh::getSoCaNhiemBanDau)
                                            .sum());
                                    regionStat.put("deaths", casesInRegion.stream()
                                            .mapToInt(CaBenh::getSoCaTuVongBanDau)
                                            .sum());
                                    return regionStat;
                                })))
                .values()
                .stream()
                .collect(Collectors.toList());
    }

    private Map<String, Object> calculateMonthlyComparison(List<CaBenh> caBenhs) {
        Map<String, Object> comparison = new HashMap<>();
        Calendar cal = Calendar.getInstance();

        // Current month data
        Date now = new Date();
        cal.setTime(now);
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);

        List<CaBenh> currentMonthCases = caBenhs.stream()
                .filter(caBenh -> isInMonth(caBenh.getNgayPhatHien(), currentMonth, currentYear))
                .collect(Collectors.toList());

        // Previous month data
        cal.add(Calendar.MONTH, -1);
        int previousMonth = cal.get(Calendar.MONTH);
        int previousYear = cal.get(Calendar.YEAR);

        List<CaBenh> previousMonthCases = caBenhs.stream()
                .filter(caBenh -> isInMonth(caBenh.getNgayPhatHien(), previousMonth, previousYear))
                .collect(Collectors.toList());

        // Calculate growth rates and changes
        double growthRate = calculateGrowthRate(previousMonthCases.size(), currentMonthCases.size());

        comparison.put("currentMonth", Map.of(
                "count", currentMonthCases.size(),
                "infected", currentMonthCases.stream().mapToInt(CaBenh::getSoCaNhiemBanDau).sum(),
                "deaths", currentMonthCases.stream().mapToInt(CaBenh::getSoCaTuVongBanDau).sum()));

        comparison.put("previousMonth", Map.of(
                "count", previousMonthCases.size(),
                "infected", previousMonthCases.stream().mapToInt(CaBenh::getSoCaNhiemBanDau).sum(),
                "deaths", previousMonthCases.stream().mapToInt(CaBenh::getSoCaTuVongBanDau).sum()));

        comparison.put("growthRate", growthRate);

        return comparison;
    }

    private List<Map<String, Object>> calculateTopFarms(List<CaBenh> caBenhs) {
        return caBenhs.stream()
                .collect(Collectors.groupingBy(
                        caBenh -> caBenh.getTrangTrai(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                farmCases -> {
                                    Map<String, Object> farmStats = new HashMap<>();
                                    TrangTrai farm = farmCases.get(0).getTrangTrai();
                                    farmStats.put("id", farm.getId());
                                    farmStats.put("tenChu", farm.getTenChu());
                                    farmStats.put("tenTrangTrai", farm.getTenTrangTrai());
                                    farmStats.put("diaChiDayDu", farm.getDiaChiDayDu());
                                    farmStats.put("totalCases", farmCases.size());
                                    farmStats.put("activeCases", farmCases.stream()
                                            .filter(c -> !c.getDaKetThuc())
                                            .count());
                                    farmStats.put("infected", farmCases.stream()
                                            .mapToInt(CaBenh::getSoCaNhiemBanDau)
                                            .sum());
                                    farmStats.put("deaths", farmCases.stream()
                                            .mapToInt(CaBenh::getSoCaTuVongBanDau)
                                            .sum());
                                    return farmStats;
                                })))
                .values()
                .stream()
                .sorted((a, b) -> ((Integer) b.get("totalCases")).compareTo((Integer) a.get("totalCases")))
                .limit(10)
                .collect(Collectors.toList());
    }

    private Map<String, Object> calculateAnimalTypeStats(List<CaBenh> caBenhs) {
        Map<String, Object> stats = new HashMap<>();

        // Group cases by animal type
        Map<String, List<CaBenh>> casesByAnimalType = caBenhs.stream()
                .flatMap(caBenh -> caBenh.getTrangTrai().getTrangTraiVatNuois().stream()
                        .map(ttvn -> Map.entry(ttvn.getLoaiVatNuoi().getTenLoai(), caBenh)))
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        // Calculate statistics for each animal type
        Map<String, Object> typeStats = new HashMap<>();
        casesByAnimalType.forEach((type, cases) -> {
            Map<String, Object> typeStat = new HashMap<>();
            typeStat.put("totalCases", cases.size());
            typeStat.put("infected", cases.stream().mapToInt(CaBenh::getSoCaNhiemBanDau).sum());
            typeStat.put("deaths", cases.stream().mapToInt(CaBenh::getSoCaTuVongBanDau).sum());
            typeStat.put("activeCases", cases.stream().filter(c -> !c.getDaKetThuc()).count());
            typeStats.put(type, typeStat);
        });

        stats.put("byType", typeStats);
        stats.put("totalTypes", casesByAnimalType.size());

        return stats;
    }

    // Utility methods
    private double calculatePercentage(long count, long total) {
        return total == 0 ? 0 : (count * 100.0) / total;
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isInMonth(Date date, int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year;
    }

    private double calculateGrowthRate(int previousValue, int currentValue) {
        if (previousValue == 0)
            return currentValue == 0 ? 0 : 100;
        return ((currentValue - previousValue) * 100.0) / previousValue;
    }

    private double calculateMonthlyGrowthRate(List<CaBenh> caBenhs) {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);

        long currentMonthCases = caBenhs.stream()
                .filter(caBenh -> isInMonth(caBenh.getNgayPhatHien(), currentMonth, currentYear))
                .count();

        cal.add(Calendar.MONTH, -1);
        int previousMonth = cal.get(Calendar.MONTH);
        int previousYear = cal.get(Calendar.YEAR);

        long previousMonthCases = caBenhs.stream()
                .filter(caBenh -> isInMonth(caBenh.getNgayPhatHien(), previousMonth, previousYear))
                .count();

        return calculateGrowthRate((int) previousMonthCases, (int) currentMonthCases);
    }

    private void updateCaBenhDetails(CaBenh existingCaBenh, CaBenh caBenhDetails) {
        existingCaBenh.setNgayPhatHien(caBenhDetails.getNgayPhatHien());
        existingCaBenh.setSoCaNhiemBanDau(caBenhDetails.getSoCaNhiemBanDau());
        existingCaBenh.setSoCaTuVongBanDau(caBenhDetails.getSoCaTuVongBanDau());
        existingCaBenh.setMoTaBanDau(caBenhDetails.getMoTaBanDau());
        existingCaBenh.setNguyenNhanDuDoan(caBenhDetails.getNguyenNhanDuDoan());
        existingCaBenh.setTrangThai(caBenhDetails.getTrangThai());
        existingCaBenh.setDaKetThuc(caBenhDetails.getDaKetThuc());

        if (caBenhDetails.getNgayDuyet() != null) {
            existingCaBenh.setNgayDuyet(caBenhDetails.getNgayDuyet());
        }

        if (caBenhDetails.getNguoiDuyet() != null) {
            existingCaBenh.setNguoiDuyet(caBenhDetails.getNguoiDuyet());
        }
    }

    private double calculateInfectionRate(TrangTrai trangTrai, int soCaNhiem, Long loaiVatNuoiId) {
        int totalAnimals = trangTrai.getTrangTraiVatNuois().stream()
                .filter(ttv -> ttv.getLoaiVatNuoi().getId().equals(loaiVatNuoiId))
                .mapToInt(TrangTraiVatNuoi::getSoLuong)
                .sum();

        return totalAnimals > 0 ? (double) soCaNhiem / totalAnimals : 0;
    }

    private double calculateInfectionRate(CaBenh caBenh, TrangTrai trangTrai) {
        Long loaiVatNuoiId = caBenh.getBenh().getLoaiVatNuoi().stream().findFirst().orElse(null).getId();
        int totalAnimals = trangTrai.getTrangTraiVatNuois().stream()
                .filter(ttv -> ttv.getLoaiVatNuoi().getId().equals(loaiVatNuoiId))
                .mapToInt(TrangTraiVatNuoi::getSoLuong)
                .sum();
        return totalAnimals > 0 ? (double) caBenh.getSoCaNhiemBanDau() / totalAnimals : 0;
    }

    // ...existing code...
}