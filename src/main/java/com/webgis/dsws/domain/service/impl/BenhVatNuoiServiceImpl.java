package com.webgis.dsws.domain.service.impl;

import com.google.common.base.Preconditions;
import com.webgis.dsws.domain.model.BenhVatNuoi;
import com.webgis.dsws.domain.model.ids.BenhVatNuoiId;
import com.webgis.dsws.domain.repository.BenhVatNuoiRepository;
import com.webgis.dsws.domain.service.BenhVatNuoiService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
@Validated
public class BenhVatNuoiServiceImpl implements BenhVatNuoiService {
    private final BenhVatNuoiRepository benhVatNuoiRepository;

    public BenhVatNuoiServiceImpl(BenhVatNuoiRepository benhVatNuoiRepository) {
        this.benhVatNuoiRepository = benhVatNuoiRepository;
    }

    @Override
    public List<BenhVatNuoi> findAll() {
        return benhVatNuoiRepository.findAll();
    }

    @Override
    public Optional<BenhVatNuoi> findById(BenhVatNuoiId id) {
        return benhVatNuoiRepository.findById(id);
    }

    @Override
    public BenhVatNuoi save(@Valid BenhVatNuoi benhVatNuoi) {
        Preconditions.checkNotNull(benhVatNuoi, "Bệnh vật nuôi không được null");
        return benhVatNuoiRepository.save(benhVatNuoi);
    }

    @Override
    public BenhVatNuoi update(BenhVatNuoiId id, BenhVatNuoi benhVatNuoiDetails) {
        BenhVatNuoi existing = findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bệnh vật nuôi với ID: " + id));

        existing.setBenh(benhVatNuoiDetails.getBenh());
        existing.setLoaiVatNuoi(benhVatNuoiDetails.getLoaiVatNuoi());
        existing.setTiLeLayNhiem(benhVatNuoiDetails.getTiLeLayNhiem());
        existing.setTiLeChet(benhVatNuoiDetails.getTiLeChet());
        existing.setTiLeHoiPhuc(benhVatNuoiDetails.getTiLeHoiPhuc());
        existing.setDatDiemRieng(benhVatNuoiDetails.getDatDiemRieng());

        return benhVatNuoiRepository.save(existing);
    }

    @Override
    public void deleteById(BenhVatNuoiId id) {
        if (!benhVatNuoiRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy bệnh vật nuôi với ID: " + id);
        }
        benhVatNuoiRepository.deleteById(id);
    }

    public Optional<BenhVatNuoi> findByBenhAndLoaiVatNuoi(Long benhId, Long loaiVatNuoiId) {
        return benhVatNuoiRepository.findByBenh_IdAndLoaiVatNuoi_Id(benhId, loaiVatNuoiId);
    }

    public List<BenhVatNuoi> findByBenhId(Long benhId) {
        return benhVatNuoiRepository.findByBenhId(benhId);
    }

    public List<BenhVatNuoi> findByLoaiVatNuoiId(Long loaiVatNuoiId) {
        return benhVatNuoiRepository.findByLoaiVatNuoiId(loaiVatNuoiId);
    }
}