package com.webgis.dsws.service;

import com.webgis.dsws.model.CaBenh;
import com.webgis.dsws.model.NguoiDung;
import com.webgis.dsws.model.enums.TrangThaiEnum;
import com.webgis.dsws.repository.CaBenhRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CaBenhService {
    @Autowired
    private CaBenhRepository caBenhRepository;

    @Transactional
    public CaBenh thayDoiCaBenh(CaBenh caBenh, NguoiDung nguoiDung) {
        // Đảm bảo ca bệnh được thay đổi ở trạng thái PENDING
        caBenh.setTrangThai(TrangThaiEnum.PENDING);
        caBenh.setNguoiTao(nguoiDung);
        caBenh.setNgayTao(new Date(System.currentTimeMillis()));

        return caBenhRepository.save(caBenh);
    }

    @Transactional
    public CaBenh duyetCaBenh(Long caBenhId, NguoiDung nguoiQuanLy, boolean approved) {
        CaBenh caBenh = caBenhRepository.findById(caBenhId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy ca bệnh"));

        if (approved) {
            caBenh.setTrangThai(TrangThaiEnum.APPROVED);
            caBenh.setNguoiDuyet(nguoiQuanLy);
            caBenh.setNgayDuyet(new Date(System.currentTimeMillis()));
        } else {
            caBenh.setTrangThai(TrangThaiEnum.REJECTED);
            caBenh.setNguoiDuyet(nguoiQuanLy);
            caBenh.setNgayDuyet(new Date(System.currentTimeMillis()));
        }

        return caBenhRepository.save(caBenh);
    }

    public Optional<CaBenh> findById(Long id) {
        return caBenhRepository.findById(id);
    }

    public List<CaBenh> findByTrangThai(TrangThaiEnum trangThaiEnum) {
        return caBenhRepository.findByTrangThai(trangThaiEnum);
    }
}