package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {
    boolean existsByTenDangNhap(String tenDangNhap);
    boolean existsByEmail(String email);
    Optional<NguoiDung> findByTenDangNhap(String tenDangNhap);
    Optional<NguoiDung> findByEmail(String email);
    List<NguoiDung> findByHoTenContaining(String hoTen);
}