package com.webgis.dsws.repository;

import com.webgis.dsws.model.LichSuCapNhat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LichSuCapNhatRepository extends JpaRepository<LichSuCapNhat, Long> {
}