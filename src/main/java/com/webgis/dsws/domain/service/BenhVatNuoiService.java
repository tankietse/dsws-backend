package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.model.BenhVatNuoi;
import com.webgis.dsws.domain.model.ids.BenhVatNuoiId;

import java.util.List;
import java.util.Optional;

public interface BenhVatNuoiService {
    List<BenhVatNuoi> findAll();

    Optional<BenhVatNuoi> findById(BenhVatNuoiId id);

    BenhVatNuoi save(BenhVatNuoi benhVatNuoi);

    void deleteById(BenhVatNuoiId id);

    BenhVatNuoi update(BenhVatNuoiId id, BenhVatNuoi benhVatNuoi);
}