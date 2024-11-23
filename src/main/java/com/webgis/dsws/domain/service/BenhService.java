package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.model.Benh;
import java.util.List;
import java.util.Optional;

public interface BenhService {
    List<Benh> findAll();

    Optional<Benh> findById(Long id);

    Benh save(Benh benh);

    void deleteById(Long id);

    Benh update(Long id, Benh benh);
}