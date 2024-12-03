package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.NguoiDungVaiTro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NguoiDungVaiTroRepository extends JpaRepository<NguoiDungVaiTro, Long> {
    List<NguoiDungVaiTro> findByNguoiDung_Id(Long nguoiDungId);
}