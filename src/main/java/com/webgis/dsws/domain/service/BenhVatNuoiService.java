package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.model.BenhVatNuoi;
import java.util.List;
import java.util.Optional;

public interface BenhVatNuoiService {
    List<BenhVatNuoi> findAll();

    Optional<BenhVatNuoi> findById(Long id);

    BenhVatNuoi save(BenhVatNuoi benhVatNuoi);

    void deleteById(Long id);

    BenhVatNuoi update(Long id, BenhVatNuoi benhVatNuoi);
}