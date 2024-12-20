package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.LichSuCapNhat;
import com.webgis.dsws.domain.model.NguoiDung;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface LichSuCapNhatRepository extends JpaRepository<LichSuCapNhat, Long> {
    List<LichSuCapNhat> findByThoiGianCapNhat(Date thoiGianCapNhat);

    List<LichSuCapNhat> findByNguoiCapNhat(NguoiDung nguoiCapNhat);
}