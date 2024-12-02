package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.Benh;
import com.webgis.dsws.domain.model.enums.MucDoBenhEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.Set;

@Repository
public interface BenhRepository extends JpaRepository<Benh, Long> {

    Optional<Benh> findById(Long maBenh);

    Optional<Benh> findByTenBenh(String tenBenh);

    List<Benh> findByTenBenhIn(Set<String> tenBenhs);

    List<Benh> findByMucDoBenhsIn(Set<MucDoBenhEnum> mucDoBenhs);
}