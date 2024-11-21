package com.webgis.dsws.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.webgis.dsws.model.TrangTraiVatNuoi;
import com.webgis.dsws.model.TrangTrai;

import java.util.List;

@Repository
public interface TrangTraiVatNuoiRepository extends JpaRepository<TrangTraiVatNuoi, Long> {
    List<TrangTraiVatNuoi> findByTrangTrai(TrangTrai trangTrai);
}