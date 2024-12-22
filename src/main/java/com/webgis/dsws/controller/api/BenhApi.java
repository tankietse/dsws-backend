package com.webgis.dsws.controller.api;

import com.webgis.dsws.domain.dto.BenhDto;
import com.webgis.dsws.domain.model.Benh;
import com.webgis.dsws.domain.service.BenhService;
import com.webgis.dsws.domain.service.impl.BenhServiceImpl;
import com.webgis.dsws.mapper.BenhMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/benh")
@RequiredArgsConstructor
@Tag(name = "Bệnh", description = "API quản lý bệnh")
public class BenhApi {

    private final BenhServiceImpl benhService;
    private final BenhMapper benhMapper;

    @GetMapping
    public ResponseEntity<List<BenhDto>> getAll() {
        List<BenhDto> benhs = benhService.findAll().stream()
                .map(benhMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(benhs);
    }

    @GetMapping("/page")
    public Page<Benh> listBenh(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return benhService.findAll(PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BenhDto> getById(@PathVariable Long id) {
        return benhService.findById(id)
                .map(benhMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bệnh với ID: " + id));
    }

    @PostMapping
    public ResponseEntity<BenhDto> create(@RequestBody BenhDto benhDto) {
        Benh benh = benhMapper.toEntity(benhDto);
        Benh saved = benhService.save(benh);
        return ResponseEntity.ok(benhMapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BenhDto> update(@PathVariable Long id, @RequestBody BenhDto benhDto) {
        Benh benh = benhMapper.toEntity(benhDto);
        Benh updated = benhService.update(id, benh);
        return ResponseEntity.ok(benhMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        benhService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/by-ten-benh/{tenBenh}")
    public ResponseEntity<BenhDto> getByTenBenh(@PathVariable String tenBenh) {
        return benhService.findByTenBenh(tenBenh)
                .map(benhMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bệnh với tên: " + tenBenh));
    }

    @GetMapping("/by-loai-vat-nuoi/{loaiVatNuoiId}")
    @Operation(summary = "Lấy danh sách bệnh theo loại vật nuôi")
    public ResponseEntity<List<BenhDto>> getByLoaiVatNuoi(@PathVariable Long loaiVatNuoiId) {
        List<BenhDto> benhs = benhService.findByLoaiVatNuoiId(loaiVatNuoiId).stream()
                .map(benhMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(benhs);
    }

    @GetMapping("/by-muc-do/{mucDoBenh}")
    @Operation(summary = "Lấy danh sách bệnh theo mức độ")
    public ResponseEntity<List<BenhDto>> getByMucDo(@PathVariable String mucDoBenh) {
        List<BenhDto> benhs = benhService.findByMucDoBenh(mucDoBenh).stream()
                .map(benhMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(benhs);
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm bệnh theo từ khóa")
    public ResponseEntity<List<BenhDto>> search(@RequestParam String keyword) {
        List<BenhDto> benhs = benhService.searchByKeyword(keyword).stream()
                .map(benhMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(benhs);
    }
}
