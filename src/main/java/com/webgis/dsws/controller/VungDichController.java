package com.webgis.dsws.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.locationtech.jts.geom.Coordinate;
import com.webgis.dsws.domain.service.VungDichService;
import com.webgis.dsws.domain.model.VungDich;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import com.webgis.dsws.domain.model.enums.MucDoVungDichEnum;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Value;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/v1/vung-dich")
@Tag(name = "Vùng dịch", description = "API quản lý vùng dịch")
public class VungDichController {

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
}