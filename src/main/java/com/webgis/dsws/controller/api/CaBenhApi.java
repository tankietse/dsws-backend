package com.webgis.dsws.controller.api;

import com.webgis.dsws.domain.dto.CaBenhStatisticsDTO;
import com.webgis.dsws.domain.model.CaBenh;
import com.webgis.dsws.domain.service.CaBenhService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.webgis.dsws.domain.model.NguoiDung;
import com.webgis.dsws.domain.model.enums.TrangThaiEnum;

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

    // @GetMapping("/thong-ke")
    // @Operation(summary = "Lấy thống kê ca bệnh theo khu vực và thời gian")
    // public ResponseEntity<Map<String, Object>> getThongKeCaBenh(
    // @RequestParam(required = false) String maTinhThanh,
    // @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date
    // fromDate,
    // @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date
    // toDate) {
    // return ResponseEntity.ok(caBenhService.getThongKeCaBenh(maTinhThanh,
    // fromDate, toDate, null, true));
    // }

    @GetMapping("/thong-ke")
    @Operation(summary = "Lấy thống kê tổng quan về ca bệnh")
    public ResponseEntity<CaBenhStatisticsDTO> getThongKe() {
        return ResponseEntity.ok(caBenhService.getThongKe());
    }

    @PostMapping
    @Operation(summary = "Tạo mới ca bệnh")
    public ResponseEntity<?> createCaBenh(
            @RequestBody CaBenh caBenh,
            @AuthenticationPrincipal NguoiDung nguoiDung) {
        try {
            CaBenh newCaBenh = caBenhService.createCaBenh(caBenh, nguoiDung);
            return ResponseEntity.ok(newCaBenh);
        } catch (IllegalArgumentException | IllegalStateException e) {
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

    @GetMapping
    @Operation(summary = "Lấy danh sách ca bệnh có phân trang và sắp xếp")
    public ResponseEntity<Page<CaBenh>> getAllCaBenh(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "benh.tenBenh") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) TrangThaiEnum trangThai) {

        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<CaBenh> caBenh;
        if (trangThai != null) {
            caBenh = caBenhService.findByTrangThai(trangThai, pageable);
        } else {
            caBenh = caBenhService.findAll(pageable);
        }

        return ResponseEntity.ok(caBenh);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin chi tiết của một ca bệnh")
    public ResponseEntity<?> getCaBenhById(@PathVariable Long id) {
        try {
            CaBenh caBenh = caBenhService.findById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("id", caBenh.getId());

            // Handle Benh data safely
            if (caBenh.getBenh() != null) {
                Map<String, Object> benhData = new HashMap<>();
                benhData.put("id", caBenh.getBenh().getId());
                benhData.put("tenBenh", caBenh.getBenh().getTenBenh());
                benhData.put("mucDoBenhs",
                        caBenh.getBenh().getMucDoBenhs() != null ? caBenh.getBenh().getMucDoBenhs() : new HashSet<>());
                benhData.put("loaiVatNuoi",
                        caBenh.getBenh().getLoaiVatNuoi() != null ? caBenh.getBenh().getLoaiVatNuoi()
                                : new HashSet<>());
                benhData.put("canCongBoDich", caBenh.getBenh().getCanCongBoDich());
                benhData.put("canPhongBenhBatBuoc", caBenh.getBenh().getCanPhongBenhBatBuoc());
                response.put("benh", benhData);
            }

            // Handle TrangTrai data safely
            if (caBenh.getTrangTrai() != null) {
                Map<String, Object> trangTraiData = new HashMap<>();
                trangTraiData.put("id", caBenh.getTrangTrai().getId());

                trangTraiData.put("tenChu", caBenh.getTrangTrai().getTenChu());
                trangTraiData.put("tenTrangTrai", caBenh.getTrangTrai().getTenTrangTrai());
                // Thong tin vat nuoi
                // tong dan, dien tich va phuong thuc chan nuoi
                trangTraiData.put("tongDan", caBenh.getTrangTrai().getTongDan());
                trangTraiData.put("dienTich", caBenh.getTrangTrai().getDienTich());
                trangTraiData.put("phuongThucChanNuoi", caBenh.getTrangTrai().getPhuongThucChanNuoi());

                trangTraiData.put("tenTrangTrai", caBenh.getTrangTrai().getTenTrangTrai());
                trangTraiData.put("diaChiDayDu", caBenh.getTrangTrai().getDiaChiDayDu());

                if (caBenh.getTrangTrai().getDonViHanhChinh() != null) {
                    trangTraiData.put("donViHanhChinh", Map.of(
                            "id", caBenh.getTrangTrai().getDonViHanhChinh().getId(),
                            "ten", caBenh.getTrangTrai().getDonViHanhChinh().getTen(),
                            "capHanhChinh", caBenh.getTrangTrai().getDonViHanhChinh().getCapHanhChinh()));
                }

                // Add vatNuoi information if available
                if (caBenh.getTrangTrai().getTrangTraiVatNuois() != null) {
                    List<Map<String, Object>> danhSachVatNuoi = caBenh.getTrangTrai().getTrangTraiVatNuois().stream()
                            .filter(ttvn -> ttvn != null && ttvn.getLoaiVatNuoi() != null)
                            .map(ttvn -> Map.of(
                                    "loaiVatNuoi", Map.of(
                                            "id", ttvn.getLoaiVatNuoi().getId(),
                                            "tenLoai", ttvn.getLoaiVatNuoi().getTenLoai()),
                                    "soLuong", ttvn.getSoLuong()))
                            .collect(Collectors.toList());
                    trangTraiData.put("danhSachVatNuoi", danhSachVatNuoi);
                }

                response.put("trangTrai", trangTraiData);
            }

            // Add basic fields with null checks
            response.put("ngayPhatHien", caBenh.getNgayPhatHien());
            response.put("moTaBanDau", caBenh.getMoTaBanDau());
            response.put("soCaNhiemBanDau", caBenh.getSoCaNhiemBanDau());
            response.put("soCaTuVongBanDau", caBenh.getSoCaTuVongBanDau());
            response.put("nguyenNhanDuDoan", caBenh.getNguyenNhanDuDoan());
            response.put("trangThai", caBenh.getTrangThai());
            response.put("daKetThuc", caBenh.getDaKetThuc());

            // Handle DienBienCaBenh data safely
            if (caBenh.getDienBienCaBenhs() != null && !caBenh.getDienBienCaBenhs().isEmpty()) {
                List<Map<String, Object>> dienBienList = caBenh.getDienBienCaBenhs().stream()
                        .map(db -> {
                            Map<String, Object> dienBien = new HashMap<>();
                            dienBien.put("id", db.getId());
                            dienBien.put("ngayCapNhat", db.getNgayCapNhat());
                            dienBien.put("soCaNhiemMoi", db.getSoCaNhiemMoi());
                            dienBien.put("soCaKhoi", db.getSoCaKhoi());
                            dienBien.put("soCaTuVong", db.getSoCaTuVong());
                            dienBien.put("bienPhapXuLy", db.getBienPhapXuLy());
                            dienBien.put("ketQuaXuLy", db.getKetQuaXuLy());
                            if (db.getNguoiCapNhat() != null) {
                                dienBien.put("nguoiCapNhat", Map.of(
                                        "id", db.getNguoiCapNhat().getId(),
                                        "hoTen", db.getNguoiCapNhat().getHoTen()));
                            }
                            return dienBien;
                        })
                        .collect(Collectors.toList());
                response.put("dienBienCaBenhs", dienBienList);
            }

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy thông tin ca bệnh: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin ca bệnh")
    public ResponseEntity<?> updateCaBenh(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates, // Change to Map to handle partial updates
            @AuthenticationPrincipal NguoiDung nguoiDung) {
        try {
            CaBenh existingCaBenh = caBenhService.findById(id);
            if (existingCaBenh == null) {
                return ResponseEntity.notFound().build();
            }

            // Update only the fields that are present in the request
            if (updates.containsKey("ngayPhatHien")) {
                existingCaBenh.setNgayPhatHien(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .parse((String) updates.get("ngayPhatHien")));
            }
            if (updates.containsKey("soCaNhiemBanDau")) {
                existingCaBenh.setSoCaNhiemBanDau(((Number) updates.get("soCaNhiemBanDau")).intValue());
            }
            if (updates.containsKey("soCaTuVongBanDau")) {
                existingCaBenh.setSoCaTuVongBanDau(((Number) updates.get("soCaTuVongBanDau")).intValue());
            }
            if (updates.containsKey("moTaBanDau")) {
                existingCaBenh.setMoTaBanDau((String) updates.get("moTaBanDau"));
            }
            if (updates.containsKey("nguyenNhanDuDoan")) {
                existingCaBenh.setNguyenNhanDuDoan((String) updates.get("nguyenNhanDuDoan"));
            }
            if (updates.containsKey("trangThai")) {
                existingCaBenh.setTrangThai(TrangThaiEnum.valueOf((String) updates.get("trangThai")));
            }

            CaBenh updatedCaBenh = caBenhService.thayDoiCaBenh(existingCaBenh, nguoiDung);
            return ResponseEntity.ok(updatedCaBenh);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Lỗi cập nhật: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/duyet")
    @Operation(summary = "Duyệt/từ chối ca bệnh")
    public ResponseEntity<?> duyetCaBenh(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @AuthenticationPrincipal NguoiDung nguoiQuanLy) {
        try {
            CaBenh caBenh = caBenhService.duyetCaBenh(id, nguoiQuanLy, approved);
            return ResponseEntity.ok(caBenh);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}