package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.LoaiVatNuoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoaiVatNuoiRepository extends JpaRepository<LoaiVatNuoi, Long> {
    Optional<LoaiVatNuoi> findByTenLoai(String tenLoai);
}