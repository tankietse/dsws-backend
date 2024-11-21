package com.webgis.dsws.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.webgis.dsws.model.VungDichBienPhap;
import com.webgis.dsws.model.VungDich;

import java.util.List;

@Repository
public interface VungDichBienPhapRepository extends JpaRepository<VungDichBienPhap, Long> {
    List<VungDichBienPhap> findByVungDich(VungDich vungDich);
}