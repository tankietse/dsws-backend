package com.webgis.dsws.repository;

import com.webgis.dsws.model.CaBenh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrangTraiCaBenhRepository extends JpaRepository<CaBenh, Long> {
}