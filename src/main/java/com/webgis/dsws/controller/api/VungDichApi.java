package com.webgis.dsws.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.locationtech.jts.geom.Coordinate;

import com.webgis.dsws.domain.service.VungDichService;
import com.webgis.dsws.domain.dto.VungDichMapDTO;
import com.webgis.dsws.domain.model.VungDich;
import com.webgis.dsws.domain.model.VungDichTrangTrai;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;

import com.webgis.dsws.domain.model.enums.MucDoVungDichEnum;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.persistence.EntityNotFoundException;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/vung-dich")
@Tag(name = "Vùng dịch", description = "API quản lý vùng dịch")
@PreAuthorize("isAuthenticated()") // Require authentication for all endpoints
public class VungDichApi {

    @Autowired
    private VungDichService vungDichService;

    @Value("${springdoc.swagger-ui.path:}")
    private String baseUrl;

    /**
     * API kiểm tra tọa độ có nằm trong vùng dịch hay không.
     * 
     * @param id ID của vùng dịch.
     * @param x  Tọa độ x.
     * @param y  Tọa độ y.
     * @return true nếu nằm trong vùng dịch, ngược lại false.
     */
    @GetMapping("/{id}/contains")
    @Operation(summary = "Kiểm tra tọa độ có nằm trong vùng dịch hay không")
    public ResponseEntity<Boolean> contains(@PathVariable Long id, @RequestParam double x, @RequestParam double y) {
        Coordinate coordinate = new Coordinate(x, y);
        boolean result = vungDichService.contains(id, coordinate);
        return ResponseEntity.ok(result);
    }

    /**
     * API lấy cảnh báo mức độ vùng dịch.
     * 
     * @param id ID của vùng dịch.
     * @return Thông báo cảnh báo.
     */
    @GetMapping("/{id}/canh-bao")
    @Operation(summary = "Lấy cảnh báo mức độ vùng dịch")
    public ResponseEntity<String> getCanhBaoMucDo(@PathVariable Long id) {
        String canhBao = vungDichService.canhBaoMucDo(id);
        return ResponseEntity.ok(canhBao);
    }

    /**
     * Lấy danh sách tất cả vùng dịch với phân trang và lọc.
     * 
     * @return Danh sách vùng dịch.
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả vùng dịch với phân trang và lọc")
    public ResponseEntity<Page<VungDich>> getAllVungDich(
            @PageableDefault(size = 10, sort = "id") Pageable pageable,
            @RequestParam(required = false) String tenVung,
            @RequestParam(required = false) MucDoVungDichEnum mucDo) {
        Page<VungDich> vungDichPage = vungDichService.findAll(pageable, tenVung, mucDo);
        return ResponseEntity.ok(vungDichPage);
    }

    /**
     * Lấy thông tin vùng dịch theo ID.
     * 
     * @param id ID của vùng dịch.
     * @return Vùng dịch tương ứng.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin vùng dịch theo ID")
    public ResponseEntity<VungDich> getVungDichById(@PathVariable Long id) {
        try {
            VungDich vungDich = vungDichService.findById(id);
            return ResponseEntity.ok(vungDich);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    

    /**
     * Thêm mới vùng dịch.
     * 
     * @param vungDich Thông tin vùng dịch cần thêm.
     * @return Vùng dịch đã được tạo.
     */
    @PostMapping
    @Operation(summary = "Thêm mới vùng dịch")
    public ResponseEntity<VungDich> createVungDich(@RequestBody VungDich vungDich) {
        VungDich createdVungDich = vungDichService.save(vungDich);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVungDich);
    }

    /**
     * Cập nhật vùng dịch theo ID.
     * 
     * @param id              ID của vùng dịch cần cập nhật.
     * @param vungDichDetails Thông tin cập nhật.
     * @return Vùng dịch đã được cập nhật.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật vùng dịch")
    public ResponseEntity<VungDich> updateVungDich(@PathVariable Long id, @RequestBody VungDich vungDichDetails) {
        VungDich updatedVungDich = vungDichService.update(id, vungDichDetails);
        if (updatedVungDich == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(updatedVungDich);
    }

    /**
     * Xóa vùng dịch theo ID.
     * 
     * @param id ID của vùng dịch cần xóa.
     * @return Phản hồi không có nội dung.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa vùng dịch")
    public ResponseEntity<Void> deleteVungDich(@PathVariable Long id) {
        try {
            vungDichService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * API lấy dữ liệu cho bản đồ nhiệt (heatmap)
     * 
     * @return Dữ liệu cho bản đồ nhiệt
     */
    @GetMapping("/heatmap")
    @Operation(summary = "Lấy dữ liệu cho bản đồ nhiệt")
    public ResponseEntity<List<Map<String, Object>>> getGradientHeatmapData() {
        List<Map<String, Object>> heatmapData = vungDichService.getGradientHeatmapData();
        return ResponseEntity.ok(heatmapData);
    }

    /**
     * API lấy dữ liệu cho cluster
     * 
     * @param mucDo  Mức độ vùng dịch cần lọc
     * @param radius Bán kính cluster (mét)
     * @return Dữ liệu cho cluster
     */
    @GetMapping("/cluster")
    @Operation(summary = "Lấy dữ liệu cho cluster")
    public ResponseEntity<List<Map<String, Object>>> getClusterData(
            @RequestParam(required = false) MucDoVungDichEnum mucDo,
            @RequestParam(defaultValue = "1000") double radius) {
        List<Map<String, Object>> clusterData = vungDichService.getClusterData(mucDo, radius);
        return ResponseEntity.ok(clusterData);
    }

    /**
     * API lấy dữ liệu biểu tượng cho feature layer với thông tin chi tiết
     * 
     * @param mucDo Mức độ vùng dịch cần lọc
     * @return Dữ liệu symbols cho feature layer
     */
    @GetMapping("/symbols")
    @Operation(summary = "Lấy dữ liệu biểu tượng cho feature layer với thông tin chi tiết")
    public ResponseEntity<Map<String, Object>> getFeatureLayerSymbols(
            @RequestParam(required = false) MucDoVungDichEnum mucDo) {
        Map<String, Object> symbolData = vungDichService.getFeatureLayerSymbols(mucDo);
        return ResponseEntity.ok(symbolData);
    }

    @GetMapping("/{id}/affected-farms")
    @Operation(summary = "Lấy danh sách trang trại bị ảnh hưởng bởi vùng dịch")
    public ResponseEntity<Set<VungDichTrangTrai>> getAffectedFarms(@PathVariable Long id) {
        VungDich vungDich = vungDichService.findById(id);
        if (vungDich == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vungDich.getTrangTrais());
    }

    @GetMapping("/map-data")
    @Operation(summary = "Lấy dữ liệu vùng dịch cho bản đồ")
    public ResponseEntity<List<VungDichMapDTO>> getVungDichMapData() {
        List<VungDichMapDTO> mapData = vungDichService.getVungDichMapData();
        return ResponseEntity.ok(mapData);
    }

    @GetMapping("/by-severity")
    @Operation(summary = "Lấy dữ liệu vùng dịch theo mức độ nghiêm trọng")
    public ResponseEntity<List<VungDich>> getVungDichBySeverity(
            @RequestParam MucDoVungDichEnum mucDo) {
        List<VungDich> vungDichList = vungDichService.findBySeverity(mucDo);
        return ResponseEntity.ok(vungDichList);
    }

    @GetMapping("/by-time")
    @Operation(summary = "Lấy dữ liệu vùng dịch theo thời gian")
    public ResponseEntity<List<VungDich>> getVungDichByTime(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        List<VungDich> vungDichList = vungDichService.findByTimeRange(startDate, endDate);
        return ResponseEntity.ok(vungDichList);
    }

    /**
     * Lấy thông tin vùng dịch theo loại vật nuôi chỉ định và có thể chọn thêm bệnh chỉ định hoặc không.
     * Dữ liệu trả về có thông tin vùng dịch kèm ranh giới hành chính và các thông tin khác để hiển thị lên bản đồ chi tiết nhất có thể.
     *
     * @param loaiVatNuoiId  ID của loại vật nuôi cần lọc.
     * @param benhId         ID của bệnh cần lọc (tùy chọn).
     * @return Danh sách vùng dịch chi tiết.
     */
    @GetMapping("/by-animal-type")
    @Operation(summary = "Lấy thông tin vùng dịch theo loại vật nuôi và bệnh chỉ định")
    public ResponseEntity<List<VungDichMapDTO>> getVungDichByAnimalTypeAndDisease(
            @RequestParam Long loaiVatNuoiId,
            @RequestParam(required = false) Long benhId) {
        List<VungDichMapDTO> vungDichDetails = vungDichService.getVungDichByAnimalTypeAndDisease(loaiVatNuoiId, benhId);
        return ResponseEntity.ok(vungDichDetails);
    }
}