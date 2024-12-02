package com.webgis.dsws.domain.service;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.webgis.dsws.util.ImportEntityProcessor;
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

@Service
@Validated
public class TrangTraiService {
    private final TrangTraiRepository trangTraiRepository;
    private final GeometryService geometryService;
    private final ImportEntityProcessor<TrangTrai> trangTraiProcessor;
    private final VungDichTrangTraiRepository vungDichTrangTraiRepository;

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
        return trangTraiRepository.findById(id)
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
                    properties.put("id", trangTrai.getId());
                    properties.put("tenTrangTrai", trangTrai.getTenTrangTrai());
                    properties.put("tenChu", trangTrai.getTenChu());
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
}
