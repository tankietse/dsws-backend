package com.webgis.dsws.repository;

import com.webgis.dsws.model.Benh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BenhRepository extends JpaRepository<Benh, Long> {

    Optional<Benh> findById(Long maBenh);

    Optional<Benh> findByTenBenh(String tenBenh);
}