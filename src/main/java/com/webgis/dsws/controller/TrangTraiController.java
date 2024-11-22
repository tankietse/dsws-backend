package com.webgis.dsws.controller;

import com.webgis.dsws.model.TrangTrai;
import com.webgis.dsws.model.VungDichTrangTrai;
import com.webgis.dsws.service.TrangTraiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả trang trại")
    public ResponseEntity<List<TrangTrai>> getAllTrangTrai() {
        return ResponseEntity.ok(trangTraiService.findAll());
    }

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

    @PostMapping
    @Operation(summary = "Thêm mới trang trại")
    public ResponseEntity<TrangTrai> createTrangTrai(@RequestBody TrangTrai trangTrai) {
        return ResponseEntity.ok(trangTraiService.save(trangTrai));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin trang trại")
    public ResponseEntity<TrangTrai> updateTrangTrai(
            @PathVariable Long id,
            @RequestBody TrangTrai trangTraiDetails) {
        return ResponseEntity.ok(trangTraiService.updateTrangTrai(id, trangTraiDetails));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa trang trại")
    public ResponseEntity<Void> deleteTrangTrai(@PathVariable Long id) {
        trangTraiService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/radius")
    @Operation(summary = "Tìm trang trại trong bán kính")
    public ResponseEntity<List<TrangTrai>> findTrangTraiInRadius(
            @RequestParam Point center,
            @RequestParam double radius) {
        return ResponseEntity.ok(trangTraiService.findTrangTraiInRadius(center, radius));
    }

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

// Add ApiResponse class for better error handling
class ApiResponse {
    private boolean success;
    private String message;
    private Object data;

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getters and setters
    // ...
}