package com.webgis.dsws.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.webgis.dsws.domain.model.VungDichBienPhap;

@Repository
public interface VungDichBienPhapRepository extends JpaRepository<VungDichBienPhap, Long> {
}