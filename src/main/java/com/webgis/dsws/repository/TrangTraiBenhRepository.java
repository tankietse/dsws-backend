package com.webgis.dsws.repository;

import com.webgis.dsws.model.TrangTraiBenh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrangTraiBenhRepository extends JpaRepository<TrangTraiBenh, Long> {
}