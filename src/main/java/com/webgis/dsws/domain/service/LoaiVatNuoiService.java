package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.model.LoaiVatNuoi;
import com.webgis.dsws.domain.repository.LoaiVatNuoiRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class LoaiVatNuoiService {

    private final LoaiVatNuoiRepository loaiVatNuoiRepository;

    public List<LoaiVatNuoi> findAll() {
        return loaiVatNuoiRepository.findAll();
    }

    public Page<LoaiVatNuoi> findAllPaged(String search, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return loaiVatNuoiRepository.findByTenLoaiContainingIgnoreCase(search, pageable);
        }
        return loaiVatNuoiRepository.findAll(pageable);
    }

    public LoaiVatNuoi findById(Long id) {
        return loaiVatNuoiRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy loại vật nuôi với ID: " + id));
    }

    public LoaiVatNuoi create(LoaiVatNuoi loaiVatNuoi) {
        // Validate
        validateLoaiVatNuoi(loaiVatNuoi);
        return loaiVatNuoiRepository.save(loaiVatNuoi);
    }

    public LoaiVatNuoi update(Long id, LoaiVatNuoi loaiVatNuoi) {
        LoaiVatNuoi existingLoaiVatNuoi = findById(id);
        // Update fields
        existingLoaiVatNuoi.setTenLoai(loaiVatNuoi.getTenLoai());
        existingLoaiVatNuoi.setMoTa(loaiVatNuoi.getMoTa());
        // Validate
        validateLoaiVatNuoi(existingLoaiVatNuoi);
        return loaiVatNuoiRepository.save(existingLoaiVatNuoi);
    }

    public void delete(Long id) {
        LoaiVatNuoi loaiVatNuoi = findById(id);
        loaiVatNuoiRepository.delete(loaiVatNuoi);
    }

    public List<LoaiVatNuoi> searchByName(String keyword) {
        return loaiVatNuoiRepository.findByTenLoaiContainingIgnoreCase(keyword);
    }

    public Map<String, Object> getThongKeVatNuoi(String loaiVatNuoi) {
        Map<String, Object> thongKe = new HashMap<>();
        // Add statistics logic here
        // For example:
        thongKe.put("tongSoLoai", loaiVatNuoiRepository.count());
        thongKe.put("chiTietTheoLoai", loaiVatNuoiRepository.getThongKeTheoLoai());
        return thongKe;
    }

    private void validateLoaiVatNuoi(LoaiVatNuoi loaiVatNuoi) {
        if (loaiVatNuoi.getTenLoai() == null || loaiVatNuoi.getTenLoai().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại vật nuôi không được để trống");
        }
        // Add more validation rules as needed
    }
}
