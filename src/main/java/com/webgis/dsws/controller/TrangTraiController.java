package com.webgis.dsws.controller;

import com.webgis.dsws.domain.model.TrangTrai;
import com.webgis.dsws.domain.model.VungDichTrangTrai;
import com.webgis.dsws.domain.service.TrangTraiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.locationtech.jts.geom.Point;
import com.webgis.dsws.domain.model.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/trang-trai")
@Tag(name = "Trang trại", description = "API quản lý trang trại")
public class TrangTraiController {

    private final TrangTraiService trangTraiService;

    public TrangTraiController(TrangTraiService trangTraiService) {
        this.trangTraiService = trangTraiService;
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
     * @param pageable Thông tin phân trang (số trang, kích thước trang, sắp xếp)
     * @return ResponseEntity chứa trang kết quả bao gồm danh sách trang trại
     */
    @GetMapping("/paged")
    @Operation(summary = "Lấy danh sách trang trại phân trang")
    public ResponseEntity<Page<TrangTrai>> getTrangTraiPaged(Pageable pageable) {
        return ResponseEntity.ok(trangTraiService.getAllTrangTrai(pageable));
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
    public ResponseEntity<TrangTrai> createTrangTrai(@RequestBody TrangTrai trangTrai) {
        return ResponseEntity.ok(trangTraiService.save(trangTrai));
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
    public ResponseEntity<TrangTrai> updateTrangTrai(
            @PathVariable Long id,
            @RequestBody TrangTrai trangTraiDetails) {
        return ResponseEntity.ok(trangTraiService.updateTrangTrai(id, trangTraiDetails));
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
}