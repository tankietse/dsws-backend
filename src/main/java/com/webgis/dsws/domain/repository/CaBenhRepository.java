package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.CaBenh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaBenhRepository extends JpaRepository<CaBenh, Long> {
}