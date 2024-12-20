package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.DonVi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DonViRepository extends JpaRepository<DonVi, Long> {
    Optional<DonVi> findByTenDonVi(String tenDonVi);
    boolean existsByMaDonVi(String maDonVi);
}