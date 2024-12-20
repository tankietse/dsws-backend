package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.model.Benh;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public interface BenhService {
    @Transactional(readOnly = true)
    List<Benh> findAll();

    @Transactional(readOnly = true)
    Optional<Benh> findById(Long id);

    Optional<Benh> findByTenBenh(String tenBenh);

    Benh findOrCreateBenh(String tenBenh);

    Benh save(Benh benh);

    void deleteById(Long id);

    Benh update(Long id, Benh benh);

    List<Benh> findByLoaiVatNuoiId(Long loaiVatNuoiId);

    List<Benh> findByMucDoBenh(String mucDoBenh);

    List<Benh> searchByKeyword(String keyword);
}