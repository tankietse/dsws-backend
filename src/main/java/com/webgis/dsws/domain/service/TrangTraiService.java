package com.webgis.dsws.domain.service;

import java.util.List;
import java.util.Set;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.webgis.dsws.util.ImportEntityProcessor;
import com.webgis.dsws.domain.dto.TrangTraiCreateDto;
import com.webgis.dsws.domain.dto.TrangTraiUpdateDto;
import com.webgis.dsws.domain.model.DonViHanhChinh;
import com.webgis.dsws.domain.model.TrangTrai;
import com.webgis.dsws.domain.model.TrangTraiVatNuoi;
import com.webgis.dsws.domain.model.VungDich;
import com.webgis.dsws.domain.model.VungDichTrangTrai;
import com.webgis.dsws.domain.repository.TrangTraiRepository;
import com.webgis.dsws.domain.repository.VungDichTrangTraiRepository;

import org.springframework.data.domain.Page;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.persistence.EntityNotFoundException;
import com.google.common.base.Preconditions;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service
@Validated
@Transactional
public class TrangTraiService {
    private final TrangTraiRepository trangTraiRepository;
    private final GeometryService geometryService;
    private final ImportEntityProcessor<TrangTrai> trangTraiProcessor;
    private final VungDichTrangTraiRepository vungDichTrangTraiRepository;

    @Autowired
    private LoaiVatNuoiService loaiVatNuoiService;

    @Autowired
    private DonViHanhChinhService donViHanhChinhService;

    public TrangTraiService(TrangTraiRepository trangTraiRepository, GeometryService geometryService,
            ImportEntityProcessor<TrangTrai> trangTraiProcessor,
            VungDichTrangTraiRepository vungDichTrangTraiRepository) {
        this.trangTraiRepository = trangTraiRepository;
        this.geometryService = geometryService;
        this.trangTraiProcessor = trangTraiProcessor;
        this.vungDichTrangTraiRepository = vungDichTrangTraiRepository;
    }

    public TrangTrai findOrCreate(String name) {
        return trangTraiProcessor.findOrCreate(name);
    }

    public Set<TrangTrai> processAndSave(Set<String> names) {
        return trangTraiProcessor.processAndSave(names);
    }

    public TrangTrai save(TrangTrai trangTrai) {
        return trangTraiRepository.save(trangTrai);
    }

    public TrangTrai findById(Long id) {
        return trangTraiRepository.findByIdWithFullDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy trang trại với ID: " + id));
    }

    public List<TrangTrai> findAll() {
        return trangTraiRepository.findAll();
    }

    public Page<TrangTrai> getAllTrangTrai(Pageable pageable) {
        return trangTraiRepository.findAll((org.springframework.data.domain.Pageable) pageable);
    }

    public void deleteById(Long id) {
        trangTraiRepository.deleteById(id);
    }

    public TrangTrai update(TrangTrai trangTrai) {
        return trangTraiRepository.save(trangTrai);
    }

    /**
     * Cập nhật thông tin trang trại
     */
    public TrangTrai updateTrangTrai(Long id, TrangTrai trangTraiDetails) {
        TrangTrai trangTrai = findById(id);

        // Chỉ cập nhật các trường không null
        if (trangTraiDetails.getTenTrangTrai() != null) {
            trangTrai.setTenTrangTrai(trangTraiDetails.getTenTrangTrai());
        }
        if (trangTraiDetails.getTenChu() != null) {
            trangTrai.setTenChu(trangTraiDetails.getTenChu());
        }
        if (trangTraiDetails.getSoDienThoai() != null) {
            trangTrai.setSoDienThoai(trangTraiDetails.getSoDienThoai());
        }
        if (trangTraiDetails.getEmail() != null) {
            trangTrai.setEmail(trangTraiDetails.getEmail());
        }
        if (trangTraiDetails.getPoint() != null) {
            trangTrai.setPoint(trangTraiDetails.getPoint());
        }
        if (trangTraiDetails.getDiaChiDayDu() != null) {
            trangTrai.setDiaChiDayDu(trangTraiDetails.getDiaChiDayDu());
        }

        if (trangTraiDetails.getDonViHanhChinh() != null) {
            DonViHanhChinh dvhc = donViHanhChinhService.findById(trangTraiDetails.getDonViHanhChinh().getId());
            trangTrai.setDonViHanhChinh(dvhc);
        }

        return trangTraiRepository.save(trangTrai);
    }

    /**
     * Tìm các trang trại trong bán kính
     */
    public List<TrangTrai> findTrangTraiInRadius(Point center, double radius) {
        Preconditions.checkNotNull(center, "Điểm trung tâm không được null");
        Preconditions.checkArgument(radius > 0, "Bán kính phải lớn hơn 0");
        return trangTraiRepository.findFarmsWithinDistance((Geometry) center, radius);
    }

    /**
     * Liên kết các trang trại bị ảnh hưởng với vùng dịch
     * 
     * @param vungDich Vùng dịch cần xử lý
     * @return Danh sách các VungDichTrangTrai đã được tạo
     */
    public List<VungDichTrangTrai> associateAffectedFarms(VungDich vungDich) {
        List<TrangTrai> affectedFarms = trangTraiRepository.findFarmsWithinDistance(vungDich.getGeom(),
                vungDich.getBanKinh());
        List<VungDichTrangTrai> vungDichTrangTrais = new ArrayList<>();
        for (TrangTrai trangTrai : affectedFarms) {
            double distance = geometryService.calculateDistance(vungDich.getGeom(), trangTrai.getPoint());
            VungDichTrangTrai vdt = new VungDichTrangTrai();
            vdt.setVungDich(vungDich);
            vdt.setTrangTrai(trangTrai);
            vdt.setKhoangCach((float) distance);
            // TODO: Xác định mức độ ảnh hưởng dựa trên khoảng cách
            // vdt.setMucDoAnhHuong(tínhMứcĐộẢnhHưởng(distance));
            vungDichTrangTraiRepository.save(vdt);
            vungDichTrangTrais.add(vdt);
        }
        return vungDichTrangTrais;
    }

    /**
     * Lấy dữ liệu GeoJSON của trang trại cho hiển thị bản đồ
     */
    public Map<String, Object> getGeoJSONData(String loaiVatNuoi) {
        List<TrangTrai> trangTrais;
        if (loaiVatNuoi != null && !loaiVatNuoi.isEmpty()) {
            // Lọc theo loại vật nuôi nếu có
            trangTrais = trangTraiRepository.findByLoaiVatNuoi(loaiVatNuoi);
        } else {
            trangTrais = findAll();
        }

        Map<String, Object> featureCollection = new HashMap<>();
        featureCollection.put("type", "FeatureCollection");

        List<Map<String, Object>> features = trangTrais.stream()
                .map(trangTrai -> {
                    Map<String, Object> feature = new HashMap<>();
                    feature.put("type", "Feature");
                    feature.put("geometry", trangTrai.getPoint());

                    Map<String, Object> properties = new HashMap<>();
                    properties.put("maTrangTrai", trangTrai.getMaTrangTrai());
                    properties.put("tenTrangTrai", trangTrai.getTenTrangTrai());
                    properties.put("tenChu", trangTrai.getTenChu());
                    properties.put("soDienThoai", trangTrai.getSoDienThoai());
                    properties.put("email", trangTrai.getEmail());
                    properties.put("diaChiDayDu", trangTrai.getDiaChiDayDu());
                    properties.put("dienTich", trangTrai.getDienTich());
                    properties.put("tongDan", trangTrai.getTongDan());
                    properties.put("phuongThucChanNuoi", trangTrai.getPhuongThucChanNuoi());
                    properties.put("trangThaiHoatDong", trangTrai.getTrangThaiHoatDong());

                    // Thêm thông tin vật nuôi kèm số lượng từng loại
                    Map<String, Integer> demVatNuoi = trangTrai.getTrangTraiVatNuois().stream()
                            .collect(Collectors.toMap(
                                    ttv -> ttv.getLoaiVatNuoi().getTenLoai(),
                                    TrangTraiVatNuoi::getSoLuong));
                    properties.put("vatNuoi", demVatNuoi);
                    properties.put("donViHanhChinh", trangTrai.getDonViHanhChinh().getTen());
                    properties.put("ngayCapNhat", trangTrai.getNgayCapNhat());
                    properties.put("nguoiQuanLy",
                            trangTrai.getNguoiQuanLy() == null ? "Updating..." : trangTrai.getNguoiQuanLy().getHoTen());

                    feature.put("properties", properties);

                    return feature;
                })
                .collect(Collectors.toList());

        featureCollection.put("features", features);
        return featureCollection;
    }

    /**
     * Thống kê trang trại theo khu vực hành chính
     */
    public Map<String, Object> getThongKeTheoKhuVuc(String capHanhChinh) {
        Map<String, Object> thongKe = new HashMap<>();
        // Logic thống kê theo cấp hành chính
        if ("tinh".equals(capHanhChinh)) {
            thongKe = trangTraiRepository.thongKeTheoTinh();
        } else if ("huyen".equals(capHanhChinh)) {
            thongKe = trangTraiRepository.thongKeTheoHuyen();
        } else if ("xa".equals(capHanhChinh)) {
            thongKe = trangTraiRepository.thongKeTheoXa();
        } else {
            // Mặc định thống kê theo tỉnh
            thongKe = trangTraiRepository.thongKeTheoTinh();
        }
        return thongKe;
    }

    /**
     * Lấy dữ liệu cluster cho hiển thị nhóm trang trại trên bản đồ
     */
    public List<Map<String, Object>> getClusterData(double radius) {
        List<TrangTrai> allFarms = findAll();
        // Implement clustering algorithm based on radius
        List<Map<String, Object>> clusters = new ArrayList<>();

        // Group farms into clusters based on distance
        Map<Point, List<TrangTrai>> clusterGroups = new HashMap<>();
        for (TrangTrai farm : allFarms) {
            Point location = farm.getPoint();
            boolean addedToCluster = false;

            for (Point center : clusterGroups.keySet()) {
                if (geometryService.calculateDistance(center, location) <= radius) {
                    clusterGroups.get(center).add(farm);
                    addedToCluster = true;
                    break;
                }
            }

            if (!addedToCluster) {
                List<TrangTrai> newCluster = new ArrayList<>();
                newCluster.add(farm);
                clusterGroups.put(location, newCluster);
            }
        }

        // Convert cluster groups to response format
        for (Map.Entry<Point, List<TrangTrai>> entry : clusterGroups.entrySet()) {
            Map<String, Object> cluster = new HashMap<>();
            cluster.put("center", entry.getKey());
            cluster.put("count", entry.getValue().size());
            cluster.put("farms", entry.getValue());
            clusters.add(cluster);
        }

        return clusters;
    }

    /**
     * Lấy dữ liệu biểu tượng cho feature layer
     */
    public Map<String, Object> getFeatureLayerSymbols(String loaiVatNuoi) {
        Map<String, Object> symbolData = new HashMap<>();

        // Build symbol definitions
        Map<String, Object> symbols = new HashMap<>();
        // Add default symbol
        symbols.put("default", getDefaultSymbol());

        if (loaiVatNuoi != null) {
            // Add specific symbols for animal types
            symbols.put(loaiVatNuoi, getSymbolForAnimalType(loaiVatNuoi));
        }

        symbolData.put("symbols", symbols);

        // Get features with symbol assignments
        List<TrangTrai> farms = (loaiVatNuoi != null) ? trangTraiRepository.findByLoaiVatNuoi(loaiVatNuoi) : findAll();

        List<Map<String, Object>> features = farms.stream()
                .map(farm -> {
                    Map<String, Object> feature = new HashMap<>();
                    feature.put("id", farm.getId());
                    feature.put("geometry", farm.getPoint());
                    feature.put("symbol", getSymbolKeyForFarm(farm));
                    feature.put("properties", getFeatureProperties(farm)); // Add detailed properties
                    return feature;
                })
                .collect(Collectors.toList());

        symbolData.put("features", features);

        return symbolData;
    }

    private Map<String, Object> getDefaultSymbol() {
        Map<String, Object> symbol = new HashMap<>();
        symbol.put("type", "simple-marker");
        symbol.put("color", "#666666");
        symbol.put("size", 8);
        return symbol;
    }

    private Map<String, Object> getSymbolForAnimalType(String loaiVatNuoi) {
        Map<String, Object> symbol = new HashMap<>();
        symbol.put("type", "picture-marker");
        symbol.put("url", "/img/icons/" + loaiVatNuoi + ".png");
        symbol.put("width", 24);
        symbol.put("height", 24);
        return symbol;
    }

    private String getSymbolKeyForFarm(TrangTrai farm) {
        // Get the primary animal type based on highest count
        return farm.getTrangTraiVatNuois().stream()
                .max((a, b) -> a.getSoLuong().compareTo(b.getSoLuong()))
                .map(ttv -> ttv.getLoaiVatNuoi().getTenLoai())
                .orElse("default");
    }

    // For feature data, add additional animal type info
    private Map<String, Object> getFeatureProperties(TrangTrai farm) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("id", farm.getId());
        properties.put("tenTrangTrai", farm.getTenTrangTrai());

        // Add all animal types and their counts
        Map<String, Integer> animalCounts = farm.getTrangTraiVatNuois().stream()
                .collect(Collectors.toMap(
                        ttv -> ttv.getLoaiVatNuoi().getTenLoai(),
                        TrangTraiVatNuoi::getSoLuong));
        properties.put("vatNuoi", animalCounts);

        return properties;
    }

    public Page<TrangTrai> findAllWithFilters(String tenTrangTrai, String capDonViHanhChinh, Integer idDonViHanhChinh,
            Pageable pageable) {
        Specification<TrangTrai> spec = Specification.where(null);

        if (tenTrangTrai != null && !tenTrangTrai.isEmpty()) {
            spec = spec.and((root, query, builder) -> builder.like(builder.lower(root.get("tenTrangTrai")),
                    "%" + tenTrangTrai.toLowerCase() + "%"));
        }

        if (idDonViHanhChinh != null) {
            DonViHanhChinh dvhc = donViHanhChinhService.findById(idDonViHanhChinh);
            if (capDonViHanhChinh != null && !capDonViHanhChinh.equalsIgnoreCase(dvhc.getCapHanhChinh())) {
                throw new IllegalArgumentException("Cấp đơn vị hành chính không khớp với ID cung cấp.");
            }
            List<Integer> donViHanhChinhIds = donViHanhChinhService.getAllDescendantIds(idDonViHanhChinh);
            spec = spec.and((root, query, builder) -> root.get("donViHanhChinh").get("id").in(donViHanhChinhIds));
        }

        return trangTraiRepository.findAll(spec, pageable);
    }

    /**
     * Lấy thông tin chi tiết của một trang trại bao gồm dữ liệu dịch bệnh
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTrangTraiDetail(Long id) {
        TrangTrai trangTrai = trangTraiRepository.findByIdWithFullDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy trang trại với ID: " + id));
        Map<String, Object> details = new HashMap<>();

        // Thông tin cơ bảnVungDichService vungDichService
        details.put("thongTinCoBan", getTrangTraiBasicInfo(trangTrai));

        // Thông tin vật nuôi
        details.put("thongTinVatNuoi", getThongTinVatNuoi(trangTrai));

        // Lịch sử dịch bệnh
        details.put("lichSuDichBenh", getLichSuDichBenh(trangTrai));

        // Cảnh báo hiện tại
        details.put("canhBao", getCanhBaoHienTai(trangTrai));

        return details;
    }

    /**
     * Phân tích và đánh giá nguy cơ dịch bệnh của trang trại
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPhanTichNguyCo(Long id, Double radius) {
        TrangTrai trangTrai = trangTraiRepository.findByIdWithFullDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy trang trại với ID: " + id));
        Map<String, Object> analysis = new HashMap<>();

        // Đánh giá mức độ nguy hiểm
        analysis.put("mucDoNguyHiem", calculateRiskLevel(trangTrai));

        // Phân tích ảnh hưởng từ vùng dịch
        analysis.put("anhHuongVungDich", analyzeDiseasesZoneImpact(trangTrai, radius));

        // Chỉ số rủi ro của khu vực
        analysis.put("chiSoRuiRoKhuVuc", calculateAreaRiskIndex(trangTrai));

        return analysis;
    }

    /**
     * Thống kê theo loại bệnh và thời gian
     */
    public Map<String, Object> getThongKeTheoBenh(String loaiVatNuoi, Date fromDate, Date toDate) {
        Map<String, Object> stats = new HashMap<>();

        // Thống kê số ca bệnh theo loại
        stats.put("thongKeCaBenh", getDiseaseCaseStats(loaiVatNuoi, fromDate, toDate));

        // Phân bố theo khu vực
        stats.put("phanBoKhuVuc", getAreaDistributionStats(loaiVatNuoi, fromDate, toDate));

        // Xu hướng thời gian
        stats.put("xuHuongThoiGian", getTimeTrendStats(loaiVatNuoi, fromDate, toDate));

        return stats;
    }

    /**
     * Lấy dữ liệu heatmap cho phân bố mật độ trang trại
     */
    public Map<String, Object> getHeatmapData(String loaiVatNuoi, String capHanhChinh) {
        Map<String, Object> heatmapData = new HashMap<>();

        // Tính toán mật độ phân bố
        List<Map<String, Object>> densityPoints = calculateDensityPoints(loaiVatNuoi, capHanhChinh);
        heatmapData.put("points", densityPoints);

        // Thiết lập gradient và các thông số
        heatmapData.put("gradient", getHeatmapGradient());
        heatmapData.put("radius", getHeatmapRadius(capHanhChinh));

        return heatmapData;
    }

    /**
     * Lấy cảnh báo dịch bệnh cho trang trại
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCanhBaoDichBenh(Long id, Double radius) {
        TrangTrai trangTrai = trangTraiRepository.findByIdWithFullDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy trang trại với ID: " + id));
        List<Map<String, Object>> warnings = new ArrayList<>();

        // Cảnh báo từ các ca bệnh gần đây
        warnings.addAll(getRecentDiseaseWarnings(trangTrai, radius));

        // Cảnh báo từ vùng dịch lân cận
        warnings.addAll(getNearbyDiseaseZoneWarnings(trangTrai, radius));

        // Dự báo nguy cơ lây nhiễm
        warnings.addAll(getInfectionRiskPredictions(trangTrai));

        return warnings;
    }

    // Helper methods
    private Map<String, Object> getTrangTraiBasicInfo(TrangTrai trangTrai) {
        Map<String, Object> basicInfo = new HashMap<>();
        basicInfo.put("id", trangTrai.getId());
        basicInfo.put("tenTrangTrai", trangTrai.getTenTrangTrai());
        basicInfo.put("tenChu", trangTrai.getTenChu());
        basicInfo.put("soDienThoai", trangTrai.getSoDienThoai());
        basicInfo.put("email", trangTrai.getEmail());
        basicInfo.put("diaChiDayDu", trangTrai.getDiaChiDayDu());
        basicInfo.put("dienTich", trangTrai.getDienTich());
        basicInfo.put("tongDan", trangTrai.getTongDan());
        basicInfo.put("phuongThucChanNuoi", trangTrai.getPhuongThucChanNuoi());
        basicInfo.put("trangThaiHoatDong", trangTrai.getTrangThaiHoatDong());
        return basicInfo;
    }

    private List<Map<String, Object>> getThongTinVatNuoi(TrangTrai trangTrai) {
        return trangTrai.getTrangTraiVatNuois().stream()
                .map(ttv -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("loaiVatNuoi", ttv.getLoaiVatNuoi().getTenLoai());
                    info.put("soLuong", ttv.getSoLuong());
                    info.put("ngayCapNhat", ttv.getNgayCapNhat());
                    return info;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getLichSuDichBenh(TrangTrai trangTrai) {
        return trangTrai.getCaBenhs().stream()
                .map(caBenh -> {
                    Map<String, Object> history = new HashMap<>();
                    history.put("id", caBenh.getId());
                    history.put("tenBenh", caBenh.getBenh().getTenBenh());
                    history.put("ngayPhatHien", caBenh.getNgayPhatHien());
                    history.put("soLuongNhiem", caBenh.getSoCaNhiemBanDau());
                    history.put("soLuongTuVong", caBenh.getSoCaTuVongBanDau());
                    history.put("trangThai", caBenh.getTrangThai());
                    history.put("daKetThuc", caBenh.getDaKetThuc());
                    return history;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getCanhBaoHienTai(TrangTrai trangTrai) {
        List<Map<String, Object>> warnings = new ArrayList<>();

        // Kiểm tra ca bệnh đang hoạt động
        trangTrai.getCaBenhs().stream()
                .filter(caBenh -> !caBenh.getDaKetThuc())
                .forEach(caBenh -> {
                    Map<String, Object> warning = new HashMap<>();
                    warning.put("loaiCanhBao", "CaBenhDangHoatDong");
                    warning.put("tenBenh", caBenh.getBenh().getTenBenh());
                    warning.put("ngayPhatHien", caBenh.getNgayPhatHien());
                    warning.put("mucDoNghiemTrong", caBenh.getBenh().getCanCongBoDich());
                    warnings.add(warning);
                });

        // Kiểm tra ảnh hưởng từ vùng dịch
        trangTrai.getVungDichs().forEach(vdt -> {
            Map<String, Object> warning = new HashMap<>();
            warning.put("loaiCanhBao", "TrongVungDich");
            warning.put("tenVungDich", vdt.getVungDich().getTenVung());
            warning.put("khoangCach", vdt.getKhoangCach());
            warnings.add(warning);
        });

        return warnings;
    }

    private String calculateRiskLevel(TrangTrai trangTrai) {
        int riskScore = 0;

        // Đánh giá dựa trên số lượng ca bệnh hiện tại
        long activeCases = trangTrai.getCaBenhs().stream()
                .filter(caBenh -> !caBenh.getDaKetThuc())
                .count();
        riskScore += activeCases * 2;

        // Đánh giá dựa trên số vùng dịch ảnh hưởng
        riskScore += trangTrai.getVungDichs().size();

        // Đánh giá dựa trên quy mô trang trại
        if (trangTrai.getTongDan() > 1000)
            riskScore += 2;
        else if (trangTrai.getTongDan() > 500)
            riskScore += 1;

        // Xác định mức độ nguy cơ
        if (riskScore >= 5)
            return "CAO";
        else if (riskScore >= 3)
            return "TRUNG_BINH";
        else
            return "THAP";
    }

    private Map<String, Object> analyzeDiseasesZoneImpact(TrangTrai trangTrai, Double radius) {
        Map<String, Object> impact = new HashMap<>();
        List<VungDichTrangTrai> vungDichs = radius != null
                ? vungDichTrangTraiRepository.findByTrangTraiAndKhoangCachLessThan(trangTrai, radius)
                : new ArrayList<>(trangTrai.getVungDichs());

        impact.put("soLuongVungDich", vungDichs.size());
        impact.put("danhSachVungDich", vungDichs.stream()
                .map(vdt -> {
                    Map<String, Object> vungDichInfo = new HashMap<>();
                    vungDichInfo.put("tenVung", vdt.getVungDich().getTenVung());
                    vungDichInfo.put("khoangCach", vdt.getKhoangCach());
                    return vungDichInfo;
                })
                .collect(Collectors.toList()));

        return impact;
    }

    private double calculateAreaRiskIndex(TrangTrai trangTrai) {
        // Cài đặt các trọng số cho các yếu tố
        final double DENSITY_WEIGHT = 0.4;
        final double DISEASE_HISTORY_WEIGHT = 0.3;
        final double INFRASTRUCTURE_WEIGHT = 0.3;

        // Tính toán các chỉ số thành phần
        double densityScore = calculateDensityScore(trangTrai);
        double diseaseHistoryScore = calculateDiseaseHistoryScore(trangTrai);
        double infrastructureScore = calculateInfrastructureScore(trangTrai);

        // Tính chỉ số tổng hợp
        return densityScore * DENSITY_WEIGHT +
                diseaseHistoryScore * DISEASE_HISTORY_WEIGHT +
                infrastructureScore * INFRASTRUCTURE_WEIGHT;
    }

    // ...other helper methods...

    private Map<String, Object> getDiseaseCaseStats(String loaiVatNuoi, Date fromDate, Date toDate) {
        Map<String, Object> stats = new HashMap<>();
        List<Object[]> results = trangTraiRepository.thongKeCaBenhTheoLoai(loaiVatNuoi, fromDate, toDate);

        stats.put("tongCaBenh", results.stream()
                .mapToInt(r -> ((Number) r[1]).intValue())
                .sum());

        stats.put("chiTietTheoBenh", results.stream()
                .map(r -> {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("tenBenh", r[0]);
                    detail.put("soCa", ((Number) r[1]).intValue());
                    return detail;
                })
                .collect(Collectors.toList()));

        return stats;
    }

    private Map<String, Object> getAreaDistributionStats(String loaiVatNuoi, Date fromDate, Date toDate) {
        Map<String, Object> distribution = new HashMap<>();
        List<Object[]> results = trangTraiRepository.thongKePhanBoTheoDonViHanhChinh(loaiVatNuoi, fromDate, toDate);

        distribution.put("phanBoTheoTinh", results.stream()
                .map(r -> {
                    Map<String, Object> area = new HashMap<>();
                    area.put("tenTinh", r[0]);
                    area.put("soTrangTrai", ((Number) r[1]).intValue());
                    area.put("soCaBenh", ((Number) r[2]).intValue());
                    return area;
                })
                .collect(Collectors.toList()));

        return distribution;
    }

    private Map<String, Object> getTimeTrendStats(String loaiVatNuoi, Date fromDate, Date toDate) {
        Map<String, Object> trends = new HashMap<>();
        List<Object[]> results = trangTraiRepository.thongKeXuHuongTheoDonVithoiGian(loaiVatNuoi, fromDate, toDate);

        trends.put("xuHuongTheoDonViThoiGian", results.stream()
                .map(r -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("thoiGian", r[0]);
                    point.put("soCaBenhMoi", ((Number) r[1]).intValue());
                    point.put("soCaBenhKetThuc", ((Number) r[2]).intValue());
                    return point;
                })
                .collect(Collectors.toList()));

        return trends;
    }

    private List<Map<String, Object>> calculateDensityPoints(String loaiVatNuoi, String capHanhChinh) {
        List<Object[]> rawPoints = trangTraiRepository.getDensityPoints(loaiVatNuoi, capHanhChinh);

        return rawPoints.stream()
                .map(point -> {
                    Map<String, Object> densityPoint = new HashMap<>();
                    densityPoint.put("location", point[0]);
                    densityPoint.put("weight", calculateDensityWeight((Number) point[1]));
                    return densityPoint;
                })
                .collect(Collectors.toList());
    }

    private double calculateDensityWeight(Number value) {
        // Normalize density value to 0-1 range using log scale
        double normalizedValue = Math.log10(value.doubleValue() + 1) / Math.log10(1000);
        return Math.min(1.0, Math.max(0.0, normalizedValue));
    }

    private Map<String, Object> getHeatmapGradient() {
        Map<String, Object> gradient = new HashMap<>();
        gradient.put("0.4", "blue");
        gradient.put("0.6", "yellow");
        gradient.put("0.8", "orange");
        gradient.put("1.0", "red");
        return gradient;
    }

    private int getHeatmapRadius(String capHanhChinh) {
        switch (capHanhChinh.toLowerCase()) {
            case "tinh":
                return 50;
            case "huyen":
                return 25;
            case "xa":
                return 10;
            default:
                return 30;
        }
    }

    private List<Map<String, Object>> getRecentDiseaseWarnings(TrangTrai trangTrai, Double radius) {
        List<Map<String, Object>> warnings = new ArrayList<>();
        List<Object[]> recentCases = trangTraiRepository.findRecentDiseaseCasesNearby(
                trangTrai.getPoint(),
                radius,
                new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000) // 30 days
        );

        for (Object[] caseData : recentCases) {
            Map<String, Object> warning = new HashMap<>();
            warning.put("loaiCanhBao", "CaBenhGanDay");
            warning.put("tenBenh", caseData[0]);
            warning.put("khoangCach", ((Number) caseData[1]).doubleValue());
            warning.put("ngayPhatHien", caseData[2]);
            warnings.add(warning);
        }

        return warnings;
    }

    private List<Map<String, Object>> getNearbyDiseaseZoneWarnings(TrangTrai trangTrai, Double radius) {
        return trangTrai.getVungDichs().stream()
                .filter(vdt -> radius == null || vdt.getKhoangCach() <= radius)
                .map(vdt -> {
                    Map<String, Object> warning = new HashMap<>();
                    warning.put("loaiCanhBao", "VungDichGanDay");
                    warning.put("tenVungDich", vdt.getVungDich().getTenVung());
                    warning.put("khoangCach", vdt.getKhoangCach());
                    warning.put("mucDoAnhHuong", calculateZoneImpact(vdt.getKhoangCach()));
                    return warning;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getInfectionRiskPredictions(TrangTrai trangTrai) {
        List<Map<String, Object>> predictions = new ArrayList<>();

        // Calculate risk based on farm characteristics and environment
        double riskIndex = calculateAreaRiskIndex(trangTrai);
        String riskLevel = calculateRiskLevel(trangTrai);

        Map<String, Object> prediction = new HashMap<>();
        prediction.put("loaiCanhBao", "DuBaoNguyCo");
        prediction.put("chiSoRuiRo", riskIndex);
        prediction.put("mucDoNguyCo", riskLevel);
        predictions.add(prediction);

        return predictions;
    }

    private double calculateZoneImpact(double distance) {
        // Impact decreases exponentially with distance
        return Math.exp(-distance / 1000.0); // Impact scale in km
    }

    private double calculateDensityScore(TrangTrai trangTrai) {
        // Calculate based on number of nearby farms within 5km radius
        List<TrangTrai> nearbyFarms = findTrangTraiInRadius(trangTrai.getPoint(), 5000);
        int farmCount = nearbyFarms.size();
        return Math.min(1.0, farmCount / 20.0); // Normalize with max expected density of 20 farms
    }

    private double calculateDiseaseHistoryScore(TrangTrai trangTrai) {
        // Calculate based on disease history in last 6 months
        long recentCases = trangTrai.getCaBenhs().stream()
                .filter(caBenh -> {
                    long sixMonthsAgo = System.currentTimeMillis() - 180L * 24 * 60 * 60 * 1000;
                    return caBenh.getNgayPhatHien().getTime() > sixMonthsAgo;
                })
                .count();
        return Math.min(1.0, recentCases / 5.0); // Normalize with max of 5 cases
    }

    private double calculateInfrastructureScore(TrangTrai trangTrai) {
        // Basic infrastructure risk assessment
        double score = 0.0;

        // Add risk for high density farming
        if (trangTrai.getTongDan() > 1000)
            score += 0.4;
        else if (trangTrai.getTongDan() > 500)
            score += 0.2;

        // Add risk for multiple animal types
        if (trangTrai.getTrangTraiVatNuois().size() > 1)
            score += 0.3;

        // Add risk for intensive farming methods
        if ("INTENSIVE".equals(trangTrai.getPhuongThucChanNuoi()))
            score += 0.3;

        return Math.min(1.0, score);
    }

    @Transactional
    public TrangTrai createTrangTrai(TrangTraiCreateDto dto) {
        TrangTrai trangTrai = new TrangTrai();

        // Set basic info
        trangTrai.setMaTrangTrai(dto.getMaTrangTrai());
        trangTrai.setTenTrangTrai(dto.getTenTrangTrai());
        trangTrai.setTenChu(dto.getTenChu());
        trangTrai.setSoDienThoai(dto.getSoDienThoai());
        trangTrai.setEmail(dto.getEmail());

        // Set address
        trangTrai.setSoNha(dto.getSoNha());
        trangTrai.setTenDuong(dto.getTenDuong());
        trangTrai.setKhuPho(dto.getKhuPho());

        // Set administrative unit
        DonViHanhChinh dvhc = donViHanhChinhService.findById(dto.getDonViHanhChinhId());
        trangTrai.setDonViHanhChinh(dvhc);

        // Build full address
        String diaChiDayDu = buildFullAddress(dto.getSoNha(), dto.getTenDuong(), dto.getKhuPho(), dvhc);
        trangTrai.setDiaChiDayDu(diaChiDayDu);

        // Set operating info
        trangTrai.setDienTich(dto.getDienTich());
        trangTrai.setTongDan(dto.getTongDan());
        trangTrai.setPhuongThucChanNuoi(dto.getPhuongThucChanNuoi());

        // Set location
        trangTrai.setPoint(geometryService.createPoint(dto.getLongitude(), dto.getLatitude()));

        // Set metadata
        trangTrai.setNgayTao(LocalDateTime.now());
        trangTrai.setTrangThaiHoatDong(true);

        return save(trangTrai);
    }

    private String buildFullAddress(String soNha, String tenDuong, String khuPho, DonViHanhChinh dvhc) {
        StringBuilder sb = new StringBuilder();
        if (soNha != null)
            sb.append(soNha).append(" ");
        if (tenDuong != null)
            sb.append(tenDuong).append(", ");
        if (khuPho != null)
            sb.append(khuPho).append(", ");
        sb.append(dvhc.getTen());
        return sb.toString();
    }

    @Transactional
    public TrangTrai updateTrangTrai(Long id, TrangTraiUpdateDto dto) {
        try {
            TrangTrai trangTrai = findById(id);

            // Basic validation
            if (dto.getTongDan() != null && dto.getTongDan() < 0) {
                throw new IllegalArgumentException("Tổng đàn không thể âm");
            }
            if (dto.getDienTich() != null && dto.getDienTich() <= 0) {
                throw new IllegalArgumentException("Diện tích phải lớn hơn 0");
            }

            // Update fields
            updateBasicInfo(trangTrai, dto);
            updateLocation(trangTrai, dto);
            updateOperatingInfo(trangTrai, dto);
            
            if (dto.getVatNuoi() != null && !dto.getVatNuoi().isEmpty()) {
                updateTrangTraiVatNuoi(trangTrai, dto.getVatNuoi());
            }

            trangTrai.setNgayCapNhat(LocalDateTime.now());
            return trangTraiRepository.save(trangTrai);
            
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi cập nhật trang trại: " + e.getMessage(), e);
        }
    }

    private void updateBasicInfo(TrangTrai trangTrai, TrangTraiUpdateDto dto) {
        if (dto.getTenTrangTrai() != null) trangTrai.setTenTrangTrai(dto.getTenTrangTrai());
        if (dto.getTenChu() != null) trangTrai.setTenChu(dto.getTenChu());
        if (dto.getSoDienThoai() != null) trangTrai.setSoDienThoai(dto.getSoDienThoai());
        if (dto.getEmail() != null) trangTrai.setEmail(dto.getEmail());
        if (dto.getSoNha() != null) trangTrai.setSoNha(dto.getSoNha());
        if (dto.getTenDuong() != null) trangTrai.setTenDuong(dto.getTenDuong());
        if (dto.getKhuPho() != null) trangTrai.setKhuPho(dto.getKhuPho());
    }

    private void updateLocation(TrangTrai trangTrai, TrangTraiUpdateDto dto) {
        if (dto.getDonViHanhChinhId() != null) {
            DonViHanhChinh dvhc = donViHanhChinhService.findById(dto.getDonViHanhChinhId());
            trangTrai.setDonViHanhChinh(dvhc);
            updateDiaChiDayDu(trangTrai);
        }
        
        if (dto.getLongitude() != null && dto.getLatitude() != null) {
            trangTrai.setPoint(geometryService.createPoint(dto.getLongitude(), dto.getLatitude()));
        }
    }

    private void updateOperatingInfo(TrangTrai trangTrai, TrangTraiUpdateDto dto) {
        if (dto.getDienTich() != null) trangTrai.setDienTich(dto.getDienTich());
        if (dto.getTongDan() != null) trangTrai.setTongDan(dto.getTongDan());
        if (dto.getPhuongThucChanNuoi() != null) trangTrai.setPhuongThucChanNuoi(dto.getPhuongThucChanNuoi());
        if (dto.getTrangThaiHoatDong() != null) trangTrai.setTrangThaiHoatDong(dto.getTrangThaiHoatDong());
    }

    private void updateDiaChiDayDu(TrangTrai trangTrai) {
        trangTrai.setDiaChiDayDu(buildFullAddress(
            trangTrai.getSoNha(),
            trangTrai.getTenDuong(),
            trangTrai.getKhuPho(),
            trangTrai.getDonViHanhChinh()
        ));
    }

    private void updateTrangTraiVatNuoi(TrangTrai trangTrai, List<TrangTraiCreateDto.VatNuoiDto> vatNuoiDtos) {
        trangTrai.getTrangTraiVatNuois().clear();

        for (TrangTraiCreateDto.VatNuoiDto vatNuoiDto : vatNuoiDtos) {
            TrangTraiVatNuoi ttvn = new TrangTraiVatNuoi();
            ttvn.setTrangTrai(trangTrai);
            ttvn.setLoaiVatNuoi(loaiVatNuoiService.findById(vatNuoiDto.getLoaiVatNuoiId()));
            ttvn.setSoLuong(vatNuoiDto.getSoLuong());
            trangTrai.getTrangTraiVatNuois().add(ttvn);
        }
    }
}
