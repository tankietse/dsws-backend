package com.webgis.dsws.controller.api;

import com.webgis.dsws.domain.model.TrangTrai;
import com.webgis.dsws.domain.model.VungDichTrangTrai;
import com.webgis.dsws.domain.service.TrangTraiService;
import com.webgis.dsws.mapper.TrangTraiMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import org.locationtech.jts.geom.Point;

import com.webgis.dsws.domain.dto.TrangTraiCreateDto;
import com.webgis.dsws.domain.dto.TrangTraiUpdateDto;
import com.webgis.dsws.domain.model.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;

import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/v1/trang-trai")
@Tag(name = "Trang trại", description = "API quản lý trang trại")
@PreAuthorize("isAuthenticated()") // Require authentication for all endpoints
public class TrangTraiApi {

    private final TrangTraiService trangTraiService;
    private final TrangTraiMapper trangTraiMapper;

    public TrangTraiApi(TrangTraiService trangTraiService, TrangTraiMapper trangTraiMapper) {
        this.trangTraiService = trangTraiService;
        this.trangTraiMapper = trangTraiMapper;
    }

    /**
     * Lấy danh sách tất cả trang trại
     *
     * @return ResponseEntity chứa danh sách các trang trại
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả trang trại")
    public ResponseEntity<List<TrangTrai>> getAllTrangTrai() {
        return ResponseEntity.ok(trangTraiService.findAll());
    }

    /**
     * Lấy danh sách trang trại phân trang
     *
     * @param size Thông tin phân trang (số trang, kích thước trang, sắp xếp)
     * @return ResponseEntity chứa trang kết quả bao gồm danh sách trang trại
     */
    @GetMapping("/paged")
    @Operation(summary = "Lấy danh sách trang trại phân trang")
    public ResponseEntity<Page<TrangTrai>> getPagedTrangTrai(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String tenTrangTrai,
            @RequestParam(required = false) String capDonViHanhChinh,
            @RequestParam(required = false) Integer idDonViHanhChinh) {

        // Validate size parameter
        if (size == null || size <= 0) {
            size = 10; // Default size
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<TrangTrai> result = trangTraiService.findAllWithFilters(tenTrangTrai, capDonViHanhChinh, idDonViHanhChinh,
                pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Lấy thông tin trang trại theo ID
     *
     * @param id ID của trang trại cần lấy thông tin
     * @return ResponseEntity chứa thông tin trang trại hoặc thông báo lỗi
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin trang trại theo ID")
    public ResponseEntity<?> getTrangTraiById(@PathVariable Long id) {
        try {
            TrangTrai trangTrai = trangTraiService.findById(id);
            if (trangTrai == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Không tìm thấy trang trại với ID: " + id));
            }
            return ResponseEntity.ok(trangTrai);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi khi tìm trang trại: " + e.getMessage()));
        }
    }

    /**
     * Thêm mới một trang trại
     *
     * @param trangTrai Thông tin trang trại cần thêm mới
     * @return ResponseEntity chứa trang trại vừa được thêm
     */
    @PostMapping
    @Operation(summary = "Thêm mới trang trại")
    public ResponseEntity<TrangTrai> createTrangTrai(@Valid @RequestBody TrangTraiCreateDto dto) {
        TrangTrai trangTrai = trangTraiService.createTrangTrai(dto);
        return ResponseEntity.ok(trangTrai);
    }

    /**
     * Cập nhật thông tin trang trại
     *
     * @param id               ID của trang trại cần cập nhật
     * @param trangTraiDetails Thông tin cập nhật cho trang trại
     * @return ResponseEntity chứa trang trại đã được cập nhật
     */
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin trang trại")
    public ResponseEntity<?> updateTrangTrai(
            @PathVariable Long id,
            @Valid @RequestBody TrangTraiUpdateDto dto) {
        try {
            TrangTrai trangTrai = trangTraiService.updateTrangTrai(id, dto);
            return ResponseEntity.ok(trangTrai);
        } catch (EntityNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi khi cập nhật trang trại: " + e.getMessage()));
        }
    }

    /**
     * Xóa một trang trại
     *
     * @param id ID của trang trại cần xóa
     * @return ResponseEntity với mã trạng thái HTTP
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa trang trại")
    public ResponseEntity<Void> deleteTrangTrai(@PathVariable Long id) {
        trangTraiService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Tìm các trang trại trong bán kính từ một điểm trung tâm
     *
     * @param center Điểm trung tâm (tọa độ Point)
     * @param radius Bán kính tìm kiếm (đơn vị mét)
     * @return ResponseEntity chứa danh sách trang trại trong bán kính
     */
    @GetMapping("/radius")
    @Operation(summary = "Tìm trang trại trong bán kính")
    public ResponseEntity<List<TrangTrai>> findTrangTraiInRadius(
            @RequestParam Point center,
            @RequestParam double radius) {
        return ResponseEntity.ok(trangTraiService.findTrangTraiInRadius(center, radius));
    }

    /**
     * Lấy danh sách vùng dịch ảnh hưởng đến trang trại
     *
     * @param id ID của trang trại
     * @return ResponseEntity chứa tập hợp các vùng dịch ảnh hưởng
     */
    @GetMapping("/{id}/vung-dich")
    @Operation(summary = "Lấy danh sách vùng dịch ảnh hưởng đến trang trại")
    public ResponseEntity<Set<VungDichTrangTrai>> getAffectedZones(@PathVariable Long id) {
        TrangTrai trangTrai = trangTraiService.findById(id);
        if (trangTrai == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(trangTrai.getVungDichs());
    }

    /**
     * Lấy dữ liệu trang trại dạng GeoJSON để hiển thị trên bản đồ
     *
     * @param loaiVatNuoi Loại vật nuôi để lọc (không bắt buộc)
     * @return ResponseEntity chứa dữ liệu GeoJSON
     */
    @GetMapping("/geojson")
    @Operation(summary = "Lấy dữ liệu trang trại dạng GeoJSON")
    public ResponseEntity<Map<String, Object>> getTrangTraiGeoJSON(
            @RequestParam(required = false) String loaiVatNuoi) {
        return ResponseEntity.ok(trangTraiService.getGeoJSONData(loaiVatNuoi));
    }

    /**
     * Lấy thống kê trang trại theo khu vực
     *
     * @param capHanhChinh Cấp hành chính (tinh/huyen/xa)
     * @return ResponseEntity chứa dữ liệu thống kê
     */
    @GetMapping("/thong-ke")
    @Operation(summary = "Lấy thống kê trang trại theo khu vực")
    public ResponseEntity<Map<String, Object>> getThongKeTheoKhuVuc(
            @RequestParam(required = false) String capHanhChinh) {
        return ResponseEntity.ok(trangTraiService.getThongKeTheoKhuVuc(capHanhChinh));
    }

    /**
     * Lấy dữ liệu cho hiển thị cluster trên bản đồ
     * 
     * @param radius Bán kính cluster (mét)
     * @return ResponseEntity chứa dữ liệu clusters
     */
    @GetMapping("/cluster")
    @Operation(summary = "Lấy dữ liệu cluster")
    public ResponseEntity<List<Map<String, Object>>> getClusterData(
            @RequestParam(defaultValue = "1000") double radius) {
        return ResponseEntity.ok(trangTraiService.getClusterData(radius));
    }

    /**
     * Lấy dữ liệu biểu tượng cho feature layer
     * 
     * @param loaiVatNuoi Loại vật nuôi để lọc (không bắt buộc)
     * @return ResponseEntity chứa dữ liệu symbols
     */
    @GetMapping("/symbols")
    @Operation(summary = "Lấy dữ liệu biểu tượng cho feature layer")
    public ResponseEntity<Map<String, Object>> getFeatureLayerSymbols(
            @RequestParam(required = false) String loaiVatNuoi) {
        return ResponseEntity.ok(trangTraiService.getFeatureLayerSymbols(loaiVatNuoi));
    }

    /**
     * Lấy chi tiết trang trại bao gồm thông tin dịch bệnh
     */
    @GetMapping("/{id}/chi-tiet")
    @Operation(summary = "Lấy chi tiết trang trại và thông tin dịch bệnh")
    public ResponseEntity<Map<String, Object>> getTrangTraiDetail(@PathVariable Long id) {
        return ResponseEntity.ok(trangTraiService.getTrangTraiDetail(id));
    }

    /**
     * Phân tích và đánh giá nguy cơ dịch bệnh cho trang trại
     */
    @GetMapping("/{id}/phan-tich-nguy-co")
    @Operation(summary = "Phân tích và đánh giá nguy cơ dịch bệnh")
    public ResponseEntity<Map<String, Object>> getPhanTichNguyCo(
            @PathVariable Long id,
            @RequestParam(required = false) Double radius) {
        return ResponseEntity.ok(trangTraiService.getPhanTichNguyCo(id, radius));
    }

    /**
     * Thống kê theo loại bệnh và thời gian
     */
    @GetMapping("/thong-ke-benh")
    @Operation(summary = "Thống kê theo loại bệnh và thời gian")
    public ResponseEntity<Map<String, Object>> getThongKeTheoBenh(
            @RequestParam(required = false) String loaiVatNuoi,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate) {
        return ResponseEntity.ok(trangTraiService.getThongKeTheoBenh(loaiVatNuoi, fromDate, toDate));
    }

    /**
     * Lấy dữ liệu heatmap phân bố mật độ trang trại
     */
    @GetMapping("/heatmap")
    @Operation(summary = "Lấy dữ liệu heatmap phân bố mật độ trang trại")
    public ResponseEntity<Map<String, Object>> getHeatmapData(
            @RequestParam(required = false) String loaiVatNuoi,
            @RequestParam(required = false) String capHanhChinh) {
        return ResponseEntity.ok(trangTraiService.getHeatmapData(loaiVatNuoi, capHanhChinh));
    }

    /**
     * Lấy cảnh báo dịch bệnh cho trang trại
     */
    @GetMapping("/{id}/canh-bao")
    @Operation(summary = "Lấy cảnh báo dịch bệnh cho trang trại")
    public ResponseEntity<List<Map<String, Object>>> getCanhBaoDichBenh(
            @PathVariable Long id,
            @RequestParam(required = false) Double radius) {
        return ResponseEntity.ok(trangTraiService.getCanhBaoDichBenh(id, radius));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm trang trại")
    public ResponseEntity<List<Map<String, Object>>> searchTrangTrai(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {

        List<TrangTrai> results = trangTraiService.searchByKeyword(q, limit);

        List<Map<String, Object>> response = results.stream()
                .map(farm -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", farm.getId());
                    dto.put("tenTrangTrai", farm.getTenTrangTrai());
                    dto.put("tenChu", farm.getTenChu());
                    dto.put("maTrangTrai", farm.getMaTrangTrai());
                    dto.put("diaChiDayDu", farm.getDiaChiDayDu());
                    dto.put("tongDan", farm.getTongDan());

                    // Add vat nuoi info
                    List<Map<String, Object>> danhSachVatNuoi = farm.getTrangTraiVatNuois().stream()
                            .map(ttvn -> {
                                Map<String, Object> vn = new HashMap<>();
                                vn.put("loaiVatNuoi", Map.of(
                                    "id", ttvn.getLoaiVatNuoi().getId(),
                                    "tenLoai", ttvn.getLoaiVatNuoi().getTenLoai()
                                ));
                                vn.put("soLuong", ttvn.getSoLuong());
                                return vn;
                            })
                            .collect(Collectors.toList());
                    dto.put("danhSachVatNuoi", danhSachVatNuoi);

                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}