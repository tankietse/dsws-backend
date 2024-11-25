package com.webgis.dsws.domain.service.impl;

import com.google.common.base.Preconditions;
import com.webgis.dsws.domain.model.BenhVatNuoi;
import com.webgis.dsws.domain.repository.BenhVatNuoiRepository;
import com.webgis.dsws.domain.service.BenhVatNuoiService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public BenhVatNuoiServiceImpl(BenhVatNuoiRepository benhVatNuoiRepository) {
        this.benhVatNuoiRepository = benhVatNuoiRepository;
    }

    @Override
    public List<BenhVatNuoi> findAll() {
        return benhVatNuoiRepository.findAll();
    }

    @Override
    public Optional<BenhVatNuoi> findById(Long id) {
        return benhVatNuoiRepository.findById(id);
    }

    @Override
    public BenhVatNuoi save(@Valid BenhVatNuoi benhVatNuoi) {
        Preconditions.checkNotNull(benhVatNuoi, "Bệnh vật nuôi không được null");
        return benhVatNuoiRepository.save(benhVatNuoi);
    }

    @Override
    public BenhVatNuoi update(Long id, BenhVatNuoi benhVatNuoiDetails) {
        BenhVatNuoi existing = findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bệnh vật nuôi với ID: " + id));

        existing.setBenh(benhVatNuoiDetails.getBenh());
        existing.setLoaiVatNuoi(benhVatNuoiDetails.getLoaiVatNuoi());
        // TODO: Update other fields
        return benhVatNuoiRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        benhVatNuoiRepository.deleteById(id);
    }
}