package com.webgis.dsws.service.impl;

import com.webgis.dsws.model.BenhVatNuoi;
import com.webgis.dsws.repository.BenhVatNuoiRepository;
import com.webgis.dsws.service.BenhVatNuoiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BenhVatNuoiServiceImpl implements BenhVatNuoiService {
    @Autowired
    private BenhVatNuoiRepository benhVatNuoiRepository;

    @Override
    public List<BenhVatNuoi> findAll() {
        return benhVatNuoiRepository.findAll();
    }

    @Override
    public Optional<BenhVatNuoi> findById(Long id) {
        return benhVatNuoiRepository.findById(id);
    }

    @Override
    public BenhVatNuoi save(BenhVatNuoi benhVatNuoi) {
        return benhVatNuoiRepository.save(benhVatNuoi);
    }

    @Override
    public void deleteById(Long id) {
        benhVatNuoiRepository.deleteById(id);
    }
}