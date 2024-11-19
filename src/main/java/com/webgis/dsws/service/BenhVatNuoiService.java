package com.webgis.dsws.service;

import com.webgis.dsws.model.BenhVatNuoi;
import java.util.List;
import java.util.Optional;

public interface BenhVatNuoiService {
    List<BenhVatNuoi> findAll();

    Optional<BenhVatNuoi> findById(Long id);

    BenhVatNuoi save(BenhVatNuoi benhVatNuoi);

    void deleteById(Long id);
}