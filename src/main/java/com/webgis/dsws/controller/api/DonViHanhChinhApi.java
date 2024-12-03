package com.webgis.dsws.controller.api;

import com.webgis.dsws.domain.model.DonViHanhChinh;
import com.webgis.dsws.domain.service.DonViHanhChinhService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/don-vi-hanh-chinh")
@Tag(name = "Đơn vị hành chính", description = "API quản lý đơn vị hành chính")
@PreAuthorize("isAuthenticated()")  // Require authentication for all endpoints
public class DonViHanhChinhApi {

    private final DonViHanhChinhService donViHanhChinhService;

    public DonViHanhChinhApi(DonViHanhChinhService donViHanhChinhService) {
        this.donViHanhChinhService = donViHanhChinhService;
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả đơn vị hành chính")
    public ResponseEntity<List<DonViHanhChinh>> getAllDonViHanhChinh() {
        return ResponseEntity.ok(donViHanhChinhService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin đơn vị hành chính theo ID")
    public ResponseEntity<DonViHanhChinh> getDonViHanhChinhById(@PathVariable Integer id) {
        return ResponseEntity.ok(donViHanhChinhService.findById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm đơn vị hành chính theo tên")
    public ResponseEntity<List<DonViHanhChinh>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(donViHanhChinhService.searchByName(name));
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/cap/{capHanhChinh}")
    @Operation(summary = "Lấy danh sách đơn vị hành chính theo cấp")
    public ResponseEntity<List<DonViHanhChinh>> getByCapHanhChinh(@PathVariable String capHanhChinh) {
        List<DonViHanhChinh> units = donViHanhChinhService.findByCapHanhChinh(capHanhChinh);
        return ResponseEntity.ok(units);
    }

    @GetMapping("/cap/{capHanhChinh}/geojson")
    @Operation(summary = "Lấy ranh giới hành chính theo cấp dưới dạng GeoJSON")
    public ResponseEntity<Map<String, Object>> getGeoJSONByCapHanhChinh(@PathVariable String capHanhChinh) {
        return ResponseEntity.ok(donViHanhChinhService.getGeoJSONByCapHanhChinh(capHanhChinh));
    }

    @GetMapping("/cap/tinh")
    @Operation(summary = "Get all provinces/cities")
    public ResponseEntity<List<DonViHanhChinh>> getAllProvinces() {
        return ResponseEntity.ok(donViHanhChinhService.findByCapHanhChinh("TINH"));
    }

    @PostMapping
    @Operation(summary = "Thêm mới đơn vị hành chính")
    public ResponseEntity<DonViHanhChinh> createDonViHanhChinh(@RequestBody DonViHanhChinh donViHanhChinh) {
        return new ResponseEntity<>(donViHanhChinhService.save(donViHanhChinh), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật đơn vị hành chính")
    public ResponseEntity<DonViHanhChinh> updateDonViHanhChinh(
            @PathVariable Integer id,
            @RequestBody DonViHanhChinh donViHanhChinh) {
        return ResponseEntity.ok(donViHanhChinhService.update(id, donViHanhChinh));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa đơn vị hành chính")
    public ResponseEntity<Void> deleteDonViHanhChinh(@PathVariable Integer id) {
        donViHanhChinhService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/children")
    @Operation(summary = "Lấy danh sách ID của tất cả đơn vị hành chính con")
    public ResponseEntity<List<Integer>> getAllChildrenIds(@PathVariable Integer id) {
        return ResponseEntity.ok(donViHanhChinhService.getAllChildrenIds(id));
    }

    @GetMapping("/parent/{parentId}")
    @Operation(summary = "Lấy danh sách đơn vị hành chính con theo ID cha")
    public ResponseEntity<List<DonViHanhChinh>> getByParentId(@PathVariable Integer parentId) {
        DonViHanhChinh parentUnit = donViHanhChinhService.findById(parentId);
        List<DonViHanhChinh> units = donViHanhChinhService.findByDonViCha(parentUnit);
        return ResponseEntity.ok(units);
    }
}
