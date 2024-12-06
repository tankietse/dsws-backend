package com.webgis.dsws.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import com.webgis.dsws.domain.model.VungDich;
import com.webgis.dsws.domain.model.VungDichTrangTrai;
import com.webgis.dsws.domain.model.enums.MucDoVungDichEnum;
import com.webgis.dsws.domain.repository.TrangTraiRepository;
import com.webgis.dsws.domain.repository.VungDichRepository;
import com.webgis.dsws.domain.model.BienPhapPhongChong;
import com.webgis.dsws.domain.model.TrangTrai;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Validated
public class VungDichService {
    private final VungDichRepository vungDichRepository;
    private final TrangTraiRepository trangTraiRepository;
    private final GeometryFactory geometryFactory;
    private final GeometryService geometryService;

    public VungDichService(VungDichRepository vungDichRepository, TrangTraiRepository trangTraiRepository) {
        this.vungDichRepository = vungDichRepository;
        this.trangTraiRepository = trangTraiRepository;
        this.geometryService = new GeometryService();
        this.geometryFactory = new GeometryFactory();
    }

    private static final Map<MucDoVungDichEnum, String> SYMBOL_COLORS = Map.of(
            MucDoVungDichEnum.CAP_DO_1, MucDoVungDichEnum.CAP_DO_1.getMauHienThi(),
            MucDoVungDichEnum.CAP_DO_2, MucDoVungDichEnum.CAP_DO_2.getMauHienThi(),
            MucDoVungDichEnum.CAP_DO_3, MucDoVungDichEnum.CAP_DO_3.getMauHienThi(),
            MucDoVungDichEnum.CAP_DO_4, MucDoVungDichEnum.CAP_DO_4.getMauHienThi()// Cam

    );

    /**
     * Kiểm tra xem một tọa độ có nằm trong vùng dịch hay không.
     * 
     * @param vungDichId ID của vùng dịch.
     * @param coordinate Tọa độ cần kiểm tra.
     * @return true nếu tọa độ nằm trong vùng dịch, ngược lại false.
     */
    public boolean contains(Long vungDichId, Coordinate coordinate) {
        VungDich vungDich = vungDichRepository.findById(vungDichId).orElse(null);
        if (vungDich != null && vungDich.getGeom() != null) {
            Point point = geometryFactory.createPoint(coordinate);
            return vungDich.getGeom().contains(point);
        }
        return false;
    }

    /**
     * Lấy thông tin vùng dịch theo ID
     */
    public VungDich getVungDichById(Long id) {
        return vungDichRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vùng dịch không tồn tại với ID: " + id));
    }

    /**
     * Cập nhật thông tin vùng dịch
     */
    public VungDich updateVungDich(Long id, VungDich vungDichDetails) {
        VungDich vungDich = getVungDichById(id);
        vungDich.setTenVung(vungDichDetails.getTenVung());
        vungDich.setMucDo(vungDichDetails.getMucDo());
        vungDich.setGeom(vungDichDetails.getGeom());
        vungDich.setBanKinh(vungDichDetails.getBanKinh());
        return vungDichRepository.save(vungDich);
    }

    /**
     * Xóa vùng dịch
     */
    public void deleteVungDich(Long id) {
        VungDich vungDich = getVungDichById(id);
        vungDichRepository.delete(vungDich);
    }

    /**
     * Phương thức cảnh báo nếu vùng dịch đang ở mức nghiêm trọng.
     * 
     * @param vungDichId ID của vùng dịch.
     * @return Thông báo cảnh báo.
     */
    public String canhBaoMucDo(Long vungDichId) {
        VungDich vungDich = vungDichRepository.findById(vungDichId).orElse(null);
        if (vungDich != null) {
            if (vungDich.getMucDo() == MucDoVungDichEnum.CAP_DO_4) {
                return "Cảnh báo: Vùng dịch " + vungDich.getTenVung() + " đang ở mức nghiêm trọng.";
            }
            return "Vùng dịch " + vungDich.getTenVung() + " an toàn.";
        }
        return "Vùng dịch không tồn tại.";
    }

    /**
     * Thêm biện pháp phòng chống mới cho vùng dịch.
     * 
     * @param vungDichId ID của vùng dịch.
     * @param bienPhap   Biện pháp phòng chống cần thêm.
     */
    public void addBienPhapPhongChong(Long vungDichId, BienPhapPhongChong bienPhap) {
        VungDich vungDich = vungDichRepository.findById(vungDichId).orElse(null);
        if (vungDich != null) {
            vungDich.getBienPhapPhongChongs().add(bienPhap);
            vungDichRepository.save(vungDich);
        }
    }

    /**
     * Lấy danh sách tất cả vùng dịch.
     * 
     * @return Danh sách các vùng dịch.
     */
    public List<VungDich> findAll() {
        return vungDichRepository.findAll();
    }

    /**
     * Lưu vùng dịch mới vào cơ sở dữ liệu.
     * 
     * @param vungDich Thông tin vùng dịch cần lưu.
     * @return Vùng dịch đã được lưu.
     */
    @Transactional
    public VungDich save(VungDich vungDich) {
        vungDich = vungDichRepository.save(vungDich);
        associateAffectedFarms(vungDich);
        return vungDich;
    }

    /**
     * Cập nhật thông tin vùng dịch.
     * 
     * @param id              ID của vùng dịch cần cập nhật.
     * @param vungDichDetails Thông tin mới của vùng dịch.
     * @return Vùng dịch đã được cập nhật.
     */
    @Transactional
    public VungDich update(Long id, VungDich vungDichDetails) {
        VungDich vungDich = getVungDichById(id);
        vungDich.setTenVung(vungDichDetails.getTenVung());
        vungDich.setMucDo(vungDichDetails.getMucDo());
        vungDich.setGeom(vungDichDetails.getGeom());
        vungDich.setBanKinh(vungDichDetails.getBanKinh());
        vungDich.setMoTa(vungDichDetails.getMoTa());
        vungDich.setNgayBatDau(vungDichDetails.getNgayBatDau());
        vungDich.setNgayKetThuc(vungDichDetails.getNgayKetThuc());
        vungDich = vungDichRepository.save(vungDich);
        associateAffectedFarms(vungDich);
        return vungDich;
    }

    /**
     * Xóa vùng dịch theo ID.
     * 
     * @param id ID của vùng dịch cần xóa.
     */
    @Transactional
    public void deleteById(Long id) {
        VungDich vungDich = vungDichRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vùng dịch không tồn tại với ID: " + id));
        vungDichRepository.delete(vungDich);
    }

    /**
     * Tìm vùng dịch theo ID.
     * 
     * @param id ID của vùng dịch.
     * @return Vùng dịch tìm được.
     */
    public VungDich findById(Long id) {
        return getVungDichById(id);
    }

    /**
     * Lấy danh sách tất cả vùng dịch.
     * 
     * @return Danh sách vùng dịch.
     */
    public List<VungDich> getAllVungDich() {
        return vungDichRepository.findAll();
    }

    public Page<VungDich> findAll(Pageable pageable, String tenVung, MucDoVungDichEnum mucDo) {
        Specification<VungDich> spec = Specification.where(null);

        if (tenVung != null) {
            spec = spec.and(
                    (root, query, cb) -> cb.like(cb.lower(root.get("tenVung")), "%" + tenVung.toLowerCase() + "%"));
        }

        if (mucDo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("mucDo"), mucDo));
        }

        return vungDichRepository.findAll(spec, pageable);
    }

    /**
     * Lấy dữ liệu cho bản đồ nhiệt hiển thị mật độ vùng dịch
     */
    public List<Map<String, Object>> getHeatmapData() {
        // Lọc các vùng dịch có ngày kết thúc là null
        Specification<VungDich> spec = (root, query, cb) -> cb.isNull(root.get("ngayKetThuc"));
        List<VungDich> vungDichs = vungDichRepository.findAll(spec);

        return vungDichs.stream().map(vd -> {
            Map<String, Object> heatmapPoint = new HashMap<>();
            Point centroid = vd.getGeom().getCentroid();

            heatmapPoint.put("latitude", centroid.getY());
            heatmapPoint.put("longitude", centroid.getX());
            heatmapPoint.put("intensity", vd.getMucDo().ordinal() * 0.25);
            heatmapPoint.put("radius", vd.getBanKinh());

            heatmapPoint.put("id", vd.getId());
            heatmapPoint.put("tenVung", vd.getTenVung());
            heatmapPoint.put("mucDo", vd.getMucDo());
            heatmapPoint.put("color", vd.getMucDo().getMauHienThi());
            heatmapPoint.put("moTa", vd.getMoTa());
            heatmapPoint.put("ngayBatDau", vd.getNgayBatDau());
            heatmapPoint.put("ngayKetThuc", vd.getNgayKetThuc());
            heatmapPoint.put("tenBenh", vd.getBenh().getTenBenh());

            return heatmapPoint;
        }).collect(Collectors.toList());
    }

    /**
     * Lấy dữ liệu thống kê bệnh theo khu vực với gradient màu
     * 
     * @return Danh sách các điểm dữ liệu cho bản đồ nhiệt với gradient màu
     */
    public List<Map<String, Object>> getGradientHeatmapData() {
        // Lọc vùng dịch đang hoạt động
        Specification<VungDich> spec = (root, query, cb) -> cb.isNull(root.get("ngayKetThuc"));
        List<VungDich> vungDichs = vungDichRepository.findAll(spec);

        // Tính toán cường độ gradient dựa trên mức độ nghiêm trọng
        double maxIntensity = vungDichs.stream()
                .mapToDouble(vd -> calculateIntensity(vd))
                .max()
                .orElse(1.0);

        return vungDichs.stream().map(vd -> {
            Map<String, Object> heatmapPoint = new HashMap<>();
            Point centroid = vd.getGeom().getCentroid();

            double intensity = calculateIntensity(vd) / maxIntensity; // Normalize to 0-1
            String gradientColor = generateGradientColor(intensity);

            heatmapPoint.put("latitude", centroid.getY());
            heatmapPoint.put("longitude", centroid.getX());
            heatmapPoint.put("intensity", intensity);
            heatmapPoint.put("radius", vd.getBanKinh());
            heatmapPoint.put("id", vd.getId());
            heatmapPoint.put("tenVung", vd.getTenVung());
            heatmapPoint.put("mucDo", vd.getMucDo());
            heatmapPoint.put("color", gradientColor);
            heatmapPoint.put("moTa", vd.getMoTa());

            return heatmapPoint;
        }).collect(Collectors.toList());
    }

    private double calculateIntensity(VungDich vungDich) {
        // Tính cường độ dựa trên mức độ và bán kính
        double mucDoWeight = (vungDich.getMucDo().ordinal() + 1) * 0.7;
        double radiusWeight = Math.min(vungDich.getBanKinh() / 1000.0, 1.0) * 0.3;
        return mucDoWeight + radiusWeight;
    }

    private String generateGradientColor(double intensity) {
        // Tạo gradient màu từ xanh lá (an toàn) đến đỏ (nguy hiểm)
        int red = (int) (255 * intensity);
        int green = (int) (255 * (1 - intensity));
        int blue = 0;
        return String.format("#%02X%02X%02X", red, green, blue);
    }

    /**
     * Lấy dữ liệu cho cluster hiển thị nhóm vùng dịch
     */
    public List<Map<String, Object>> getClusterData(MucDoVungDichEnum mucDo, double radius) {
        // Lọc theo mức độ nếu có
        Specification<VungDich> spec = Specification.where(null);
        if (mucDo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("mucDo"), mucDo));
        }

        List<VungDich> vungDichs = vungDichRepository.findAll(spec);

        return vungDichs.stream().map(vd -> {
            Map<String, Object> clusterPoint = new HashMap<>();
            Point centroid = vd.getGeom().getCentroid();

            clusterPoint.put("id", vd.getId());
            clusterPoint.put("latitude", centroid.getY());
            clusterPoint.put("longitude", centroid.getX());
            clusterPoint.put("mucDo", vd.getMucDo());
            clusterPoint.put("color", vd.getMucDo().getMauHienThi());
            clusterPoint.put("tenVung", vd.getTenVung());
            clusterPoint.put("banKinh", vd.getBanKinh());
            clusterPoint.put("color", SYMBOL_COLORS.get(vd.getMucDo()));

            clusterPoint.put("tenVung", vd.getTenVung());
            clusterPoint.put("mucDo", vd.getMucDo());
            clusterPoint.put("moTa", vd.getMoTa());
            clusterPoint.put("ngayBatDau", vd.getNgayBatDau());
            clusterPoint.put("ngayKetThuc", vd.getNgayKetThuc());
            clusterPoint.put("tenBenh", vd.getBenh().getTenBenh());

            return clusterPoint;
        }).collect(Collectors.toList());
    }

    /**
     * Lấy dữ liệu symbols cho feature layer theo mức độ
     */
    public Map<String, Object> getFeatureLayerSymbols(MucDoVungDichEnum mucDo) {
        Map<String, Object> symbolData = new HashMap<>();

        // Lọc vùng dịch theo mức độ nếu có
        List<VungDich> vungDichs;
        if (mucDo != null) {
            vungDichs = vungDichRepository.findByMucDo(mucDo);
        } else {
            vungDichs = vungDichRepository.findAll();
        }

        GeoJsonWriter geoJsonWriter = new GeoJsonWriter();
        ObjectMapper objectMapper = new ObjectMapper();

        // Tạo features cho mỗi vùng dịch
        List<Map<String, Object>> features = vungDichs.stream().map(vd -> {
            Map<String, Object> feature = new HashMap<>();
            feature.put("type", "Feature");

            // Chuyển đổi Geometry sang GeoJSON
            String geometryJson = geoJsonWriter.write(vd.getGeom());
            Map<String, Object> geometry = null;
            try {
                geometry = objectMapper.readValue(geometryJson, Map.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            feature.put("geometry", geometry);

            Map<String, Object> properties = new HashMap<>();
            properties.put("id", vd.getId());
            properties.put("tenVung", vd.getTenVung());
            properties.put("mucDo", vd.getMucDo());
            properties.put("fillColor", SYMBOL_COLORS.get(vd.getMucDo()));
            properties.put("fillOpacity", 0.5);
            properties.put("strokeColor", SYMBOL_COLORS.get(vd.getMucDo()));
            properties.put("strokeWidth", 2);

            // Add more useful information
            properties.put("moTa", vd.getMoTa());
            properties.put("ngayBatDau", vd.getNgayBatDau());
            properties.put("ngayKetThuc", vd.getNgayKetThuc());
            properties.put("tenBenh", vd.getBenh().getTenBenh());
            properties.put("banKinh", vd.getBanKinh());
            properties.put("trangThai", vd.getTrangThai());
            properties.put("mucDoNghiemTrong", vd.getMucDoNghiemTrong());

            feature.put("properties", properties);
            return feature;
        }).collect(Collectors.toList());

        // Tạo GeoJSON FeatureCollection
        symbolData.put("type", "FeatureCollection");
        symbolData.put("features", features);

        return symbolData;
    }

    private void associateAffectedFarms(VungDich vungDich) {
        if (vungDich.getTrangTrais() == null) {
            vungDich.setTrangTrais(new HashSet<>());
        } else {
            vungDich.getTrangTrais().clear();
        }

        List<TrangTrai> affectedFarms = trangTraiRepository.findFarmsWithinDistance(
                vungDich.getGeom(), vungDich.getBanKinh());

        for (TrangTrai trangTrai : affectedFarms) {
            VungDichTrangTrai vdt = new VungDichTrangTrai();
            vdt.setVungDich(vungDich);
            vdt.setTrangTrai(trangTrai);
            float distance = (float) geometryService.calculateDistance(
                    vungDich.getGeom(), trangTrai.getPoint());
            vdt.setKhoangCach(distance);
            vungDich.getTrangTrais().add(vdt);
        }
    }
}