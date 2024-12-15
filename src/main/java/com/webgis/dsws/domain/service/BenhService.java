package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.dto.BenhDTO;
import com.webgis.dsws.domain.model.Benh;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface BenhService {
    // Chuyển đổi Entity sang DTO
    default BenhDTO convertToDTO(Benh benh) {
        if (benh == null) return null;

        BenhDTO dto = new BenhDTO();
        dto.setId(benh.getId());
        dto.setTenBenh(benh.getTenBenh());
        dto.setMoTa(benh.getMoTa());
        dto.setTacNhanGayBenh(benh.getTacNhanGayBenh());
        dto.setTrieuChung(benh.getTrieuChung());
        dto.setThoiGianUBenh(benh.getThoiGianUBenh());
        dto.setPhuongPhapChanDoan(benh.getPhuongPhapChanDoan());
        dto.setBienPhapPhongNgua(benh.getBienPhapPhongNgua());
        dto.setMucDoBenhs(benh.getMucDoBenhs());
        dto.setCanCongBoDich(benh.getCanCongBoDich());
        dto.setCanPhongBenhBatBuoc(benh.getCanPhongBenhBatBuoc());
        return dto;
    }

    // Chuyển đổi DTO sang Entity
    default Benh convertToEntity(BenhDTO dto) {
        if (dto == null) return null;

        Benh benh = new Benh();
        benh.setId(dto.getId());
        benh.setTenBenh(dto.getTenBenh());
        benh.setMoTa(dto.getMoTa());
        benh.setTacNhanGayBenh(dto.getTacNhanGayBenh());
        benh.setTrieuChung(dto.getTrieuChung());
        benh.setThoiGianUBenh(dto.getThoiGianUBenh());
        benh.setPhuongPhapChanDoan(dto.getPhuongPhapChanDoan());
        benh.setBienPhapPhongNgua(dto.getBienPhapPhongNgua());
        benh.setMucDoBenhs(dto.getMucDoBenhs());
        benh.setCanCongBoDich(dto.getCanCongBoDich());
        benh.setCanPhongBenhBatBuoc(dto.getCanPhongBenhBatBuoc());
        return benh;
    }

    @Transactional(readOnly = true)
    List<BenhDTO> findAllDTO();

    @Transactional(readOnly = true)
    Optional<BenhDTO> findDTOById(Long id);

    Optional<BenhDTO> findDTOByTenBenh(String tenBenh);

    BenhDTO findOrCreateBenhDTO(String tenBenh);

    BenhDTO saveDTO(BenhDTO benhDTO);

    void deleteDTOById(Long id);

    BenhDTO updateDTO(Long id, BenhDTO benhDTO);

    List<Benh> findAll();

    @Transactional(readOnly = true) 
    Optional<Benh> findById(Long id);

    Optional<Benh> findByTenBenh(String tenBenh);

    Benh findOrCreateBenh(String tenBenh);

    Benh save(Benh benh);

    void deleteById(Long id);

    Benh update(Long id, Benh benh);

    public Page<Benh> searchBenh(String keyword, Pageable pageable);
}