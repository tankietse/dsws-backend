package com.webgis.dsws.repository;

import com.webgis.dsws.model.NguoiDungVaiTro;
import com.webgis.dsws.model.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VaiTroRepository extends JpaRepository<VaiTro, Long> {
    VaiTro findVaiTroById(Long id);
    VaiTro findVaiTroByTenVaiTro(String name);
}