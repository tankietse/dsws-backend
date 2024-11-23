package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.CanhBao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CanhBaoRepository extends JpaRepository<CanhBao, Long> {
    Optional<CanhBao> findByMucDoKhanCap(String mucDo);
}