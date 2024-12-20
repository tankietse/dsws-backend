package com.webgis.dsws.controller.api;

import com.webgis.dsws.domain.model.CaBenh;
import com.webgis.dsws.domain.service.CaBenhService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ca-benh")
@Tag(name = "Ca bệnh", description = "API quản lý ca bệnh")
@RequiredArgsConstructor
public class CaBenhApi {

    private final CaBenhService caBenhService;

    @GetMapping("/geojson")
    @Operation(summary = "Lấy dữ liệu ca bệnh dạng GeoJSON để hiển thị trên bản đồ")
    public ResponseEntity<Map<String, Object>> getCaBenhGeoJSON(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
            @RequestParam(required = false) String maTinhThanh,
            @RequestParam(required = false) String loaiBenh,
            @RequestParam(required = false, defaultValue = "false") boolean chiHienThiChuaKetThuc) {
        return ResponseEntity
                .ok(caBenhService.getCaBenhGeoJSON(fromDate, toDate, maTinhThanh, loaiBenh, chiHienThiChuaKetThuc));
    }

    @GetMapping("/geojson-vung")
    @Operation(summary = "Lấy dữ liệu ca bệnh dạng GeoJSON theo vùng và các bộ lọc khác để hiển thị trên bản đồ")
    public ResponseEntity<Map<String, Object>> getCaBenhByRegionGeoJSON(
            @RequestParam(required = false) String capHanhChinh,
            @RequestParam(required = false) Long benhId,
            @RequestParam(required = false) String mucDoBenh,
            @RequestParam(required = false) Long loaiVatNuoiId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate) {
        try {
            return ResponseEntity.ok(caBenhService.getCaBenhByRegionGeoJSON(
                    capHanhChinh,
                    benhId,
                    mucDoBenh,
                    loaiVatNuoiId,
                    fromDate,
                    toDate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "details", "Date format should be yyyy-MM-dd"));
        }
    }

    @GetMapping("/geojson-theo-benh")
    @Operation(summary = "Lấy dữ liệu ca bệnh dạng GeoJSON nhóm theo bệnh để hiển thị trên bản đồ")
    public ResponseEntity<Map<String, Object>> getCaBenhGroupedByDiseaseGeoJSON() {
        return ResponseEntity.ok(caBenhService.getCaBenhGroupedByDiseaseGeoJSON());
    }

    @GetMapping("/thong-ke")
    @Operation(summary = "Lấy thống kê ca bệnh theo khu vực và thời gian")
    public ResponseEntity<Map<String, Object>> getThongKeCaBenh(
            @RequestParam(required = false) String maTinhThanh,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate) {
        return ResponseEntity.ok(caBenhService.getThongKeCaBenh(maTinhThanh, fromDate, toDate, null, true));
    }

    @PostMapping
    @Operation(summary = "Tạo mới ca bệnh")
    public ResponseEntity<?> createCaBenh(@RequestBody CaBenh caBenh) {
        try {
            CaBenh newCaBenh = caBenhService.createCaBenh(caBenh);
            return ResponseEntity.ok(newCaBenh);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/ket-thuc")
    @Operation(summary = "Kết thúc ca bệnh")
    public ResponseEntity<?> endCaBenh(
            @PathVariable Long id,
            @RequestParam(required = false) String lyDo) {
        try {
            CaBenh caBenh = caBenhService.endCaBenh(id, lyDo);
            return ResponseEntity.ok(caBenh);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/trong-vung")
    @Operation(summary = "Tìm các ca bệnh trong phạm vi")
    public ResponseEntity<?> findCaBenhsInRadius(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(defaultValue = "1000") double radiusInMeters) {
        try {
            return ResponseEntity.ok(caBenhService.findCaBenhsInRadius(longitude, latitude, radiusInMeters));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}