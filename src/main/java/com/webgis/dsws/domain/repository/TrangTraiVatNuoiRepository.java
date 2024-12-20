package com.webgis.dsws.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.webgis.dsws.domain.model.TrangTraiVatNuoi;
import com.webgis.dsws.domain.model.TrangTrai;

import java.util.List;

@Repository
public interface TrangTraiVatNuoiRepository extends JpaRepository<TrangTraiVatNuoi, Long> {
    List<TrangTraiVatNuoi> findByTrangTrai(TrangTrai trangTrai);
}