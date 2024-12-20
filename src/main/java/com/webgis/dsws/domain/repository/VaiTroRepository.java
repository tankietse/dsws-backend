package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VaiTroRepository extends JpaRepository<VaiTro, Long> {
    VaiTro findVaiTroById(Long id);

    VaiTro findVaiTroByTenVaiTro(String name);
}