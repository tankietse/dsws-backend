package com.webgis.dsws.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.webgis.dsws.model.VungDich;

@Repository
public interface VungDichRepository extends JpaRepository<VungDich, Long> {
    Optional<VungDich> findById(Long maVung);
}