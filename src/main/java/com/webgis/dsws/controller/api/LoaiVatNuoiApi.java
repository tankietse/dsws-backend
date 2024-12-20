package com.webgis.dsws.controller.api;

import com.webgis.dsws.domain.model.LoaiVatNuoi;
import com.webgis.dsws.domain.model.ApiResponse;
import com.webgis.dsws.domain.service.LoaiVatNuoiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/loai-vat-nuoi")
@Tag(name = "Loại vật nuôi", description = "API quản lý loại vật nuôi")
@RequiredArgsConstructor
public class LoaiVatNuoiApi {

    private final LoaiVatNuoiService loaiVatNuoiService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả loại vật nuôi")
    public ResponseEntity<List<LoaiVatNuoi>> getAllLoaiVatNuoi() {
        return ResponseEntity.ok(loaiVatNuoiService.findAll());
    }

    @GetMapping("/paged")
    @Operation(summary = "Lấy danh sách loại vật nuôi có phân trang")
    public ResponseEntity<Page<LoaiVatNuoi>> getPagedLoaiVatNuoi(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(loaiVatNuoiService.findAllPaged(search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin loại vật nuôi theo ID")
    public ResponseEntity<LoaiVatNuoi> getLoaiVatNuoiById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(loaiVatNuoiService.findById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thêm mới loại vật nuôi")
    public ResponseEntity<ApiResponse> createLoaiVatNuoi(@RequestBody LoaiVatNuoi loaiVatNuoi) {
        try {
            LoaiVatNuoi newLoaiVatNuoi = loaiVatNuoiService.create(loaiVatNuoi);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Thêm loại vật nuôi thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi khi thêm loại vật nuôi: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật loại vật nuôi")
    public ResponseEntity<ApiResponse> updateLoaiVatNuoi(
            @PathVariable Long id,
            @RequestBody LoaiVatNuoi loaiVatNuoi) {
        try {
            loaiVatNuoiService.update(id, loaiVatNuoi);
            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật loại vật nuôi thành công"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi khi cập nhật loại vật nuôi: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa loại vật nuôi")
    public ResponseEntity<ApiResponse> deleteLoaiVatNuoi(@PathVariable Long id) {
        try {
            loaiVatNuoiService.delete(id);
            return ResponseEntity.ok(new ApiResponse(true, "Xóa loại vật nuôi thành công"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi khi xóa loại vật nuôi: " + e.getMessage()));
        }
    }

    @GetMapping("/thong-ke")
    @Operation(summary = "Thống kê số lượng vật nuôi theo loại")
    public ResponseEntity<Map<String, Object>> getThongKeVatNuoi(
            @RequestParam(required = false) String loaiVatNuoi) {
        return ResponseEntity.ok(loaiVatNuoiService.getThongKeVatNuoi(loaiVatNuoi));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm loại vật nuôi theo tên")
    public ResponseEntity<List<LoaiVatNuoi>> searchLoaiVatNuoi(@RequestParam String keyword) {
        return ResponseEntity.ok(loaiVatNuoiService.searchByName(keyword));
    }
}
