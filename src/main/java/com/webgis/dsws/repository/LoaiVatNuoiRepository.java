package com.webgis.dsws.repository;

import com.webgis.dsws.model.LoaiVatNuoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoaiVatNuoiRepository extends JpaRepository<LoaiVatNuoi, Long> {
    Optional<LoaiVatNuoi> findByTenLoai(String tenLoai);

    boolean existsByTenLoai(String tenLoai);

    boolean existsById(Long maLoai);
}