package com.webgis.dsws.repository;

import com.webgis.dsws.model.BenhVatNuoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BenhVatNuoiRepository extends JpaRepository<BenhVatNuoi, Long> {
}