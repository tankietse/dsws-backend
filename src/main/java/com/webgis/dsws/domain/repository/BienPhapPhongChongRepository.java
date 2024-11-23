package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.BienPhapPhongChong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BienPhapPhongChongRepository extends JpaRepository<BienPhapPhongChong, Long> {
    List<BienPhapPhongChong> findByTenBienPhap(String tenBienPhap);

    Optional<BienPhapPhongChong> findById(Long id);
}