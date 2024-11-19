package com.webgis.dsws.repository;

import com.webgis.dsws.model.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {
    boolean existsByTenDangNhap(String tenDangNhap);
    boolean existsByEmail(String email);
}