package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.model.CaBenh;
import com.webgis.dsws.domain.model.DienBienCaBenh;
import com.webgis.dsws.domain.repository.CaBenhRepository;
import com.webgis.dsws.domain.repository.DienBienCaBenhRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DienBienCaBenhService {
    private final DienBienCaBenhRepository dienBienCaBenhRepository;
    private final CaBenhRepository caBenhRepository;

    @Transactional
    public DienBienCaBenh create(DienBienCaBenh dienBienCaBenh) {
        // Validate ca bệnh exists
        CaBenh caBenh = caBenhRepository.findById(dienBienCaBenh.getCaBenh().getId())
                .orElseThrow(() -> new EntityNotFoundException("Ca bệnh không tồn tại"));

        dienBienCaBenh.setNgayCapNhat(new Date());
        DienBienCaBenh saved = dienBienCaBenhRepository.save(dienBienCaBenh);
        log.info("Đã lưu diễn biến ca bệnh mới: ID={}, CaBenhID={}, NgayCapNhat={}, SoCaNhiemMoi={}",
                saved.getId(), saved.getCaBenh().getId(), saved.getNgayCapNhat(), saved.getSoCaNhiemMoi());
        return saved;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DienBienCaBenh createInitialDienBienCaBenh(DienBienCaBenh dienBienCaBenh) {
        try {
            validateInitialDienBien(dienBienCaBenh);
            DienBienCaBenh saved = dienBienCaBenhRepository.saveAndFlush(dienBienCaBenh);
            log.info("Đã lưu diễn biến ban đầu cho ca bệnh: ID={}, CaBenhID={}, SoCaNhiemBanDau={}, SoCaTuVong={}",
                    saved.getId(), saved.getCaBenh().getId(), saved.getSoCaNhiemMoi(), saved.getSoCaTuVong());
            return saved;
        } catch (Exception e) {
            log.error("Lỗi khi lưu diễn biến ca bệnh: {}", e.getMessage());
            throw e;
        }
    }

    private void validateInitialDienBien(DienBienCaBenh dienBienCaBenh) {
        if (dienBienCaBenh.getCaBenh() == null) {
            throw new IllegalArgumentException("Ca bệnh không được để trống");
        }
        if (dienBienCaBenh.getSoCaNhiemMoi() == null || dienBienCaBenh.getSoCaNhiemMoi() < 0) {
            throw new IllegalArgumentException("Số ca nhiễm mới không hợp lệ");
        }
        // Add more validations as needed
    }

    @Transactional(readOnly = true)
    public List<DienBienCaBenh> findByCaBenhId(Long caBenhId) {
        return dienBienCaBenhRepository.findByCaBenhId(caBenhId);
    }

    @Transactional(readOnly = true)
    public DienBienCaBenh findLatestByCaBenhId(Long caBenhId) {
        return dienBienCaBenhRepository.findLatestByCaBenhId(caBenhId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public DienBienCaBenh update(Long id, DienBienCaBenh dienBienCaBenh) {
        Optional<DienBienCaBenh> existing = dienBienCaBenhRepository.findById(id);
        if (existing.isPresent()) {
            DienBienCaBenh toUpdate = existing.get();
            toUpdate.setNgayCapNhat(dienBienCaBenh.getNgayCapNhat());

            toUpdate.setSoCaNhiemMoi(dienBienCaBenh.getSoCaNhiemMoi());
            toUpdate.setSoCaTuVong(dienBienCaBenh.getSoCaTuVong());
            toUpdate.setSoCaKhoi(dienBienCaBenh.getSoCaKhoi());
            toUpdate.setBienPhapXuLy(dienBienCaBenh.getBienPhapXuLy());
            toUpdate.setKetQuaXuLy(dienBienCaBenh.getKetQuaXuLy());
            toUpdate.setNguoiCapNhat(dienBienCaBenh.getNguoiCapNhat());
            toUpdate.setChanDoanChiTiet(dienBienCaBenh.getChanDoanChiTiet());
            DienBienCaBenh updated = dienBienCaBenhRepository.save(toUpdate);
            log.info("Đã cập nhật diễn biến ca bệnh: ID={}, CaBenhID={}, SoCaNhiemMoi={}, SoCaTuVong={}, SoCaKhoi={}",
                    updated.getId(), updated.getCaBenh().getId(),
                    updated.getSoCaNhiemMoi(), updated.getSoCaTuVong(), updated.getSoCaKhoi());
            return updated;
        }
        log.error("Không tìm thấy diễn biến ca bệnh để cập nhật: ID={}", id);
        throw new EntityNotFoundException("Không tìm thấy diễn biến ca bệnh với ID: " + id);
    }

    @Transactional
    public void delete(Long id) {
        if (!dienBienCaBenhRepository.existsById(id)) {
            log.error("Không tìm thấy diễn biến ca bệnh để xóa: ID={}", id);
            throw new EntityNotFoundException("Không tìm thấy diễn biến ca bệnh với ID: " + id);
        }
        dienBienCaBenhRepository.deleteById(id);
        log.info("Đã xóa diễn biến ca bệnh: ID={}", id);
    }
}
