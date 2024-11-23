package com.webgis.dsws.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.webgis.dsws.domain.model.VungDichBienPhap;
import com.webgis.dsws.domain.model.VungDich;

import java.util.List;

@Repository
public interface VungDichBienPhapRepository extends JpaRepository<VungDichBienPhap, Long> {
    List<VungDichBienPhap> findByVungDich(VungDich vungDich);
}