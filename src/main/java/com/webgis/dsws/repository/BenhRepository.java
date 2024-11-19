package com.webgis.dsws.repository;

import com.webgis.dsws.model.Benh;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BenhRepository extends JpaRepository<Benh, Long> {

    Optional<Benh> findByTenBenh(String name);
}