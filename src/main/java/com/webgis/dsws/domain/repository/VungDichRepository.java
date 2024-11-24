package com.webgis.dsws.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.webgis.dsws.domain.model.VungDich;
import com.webgis.dsws.domain.model.enums.MucDoVungDichEnum;

import java.util.List;

public interface VungDichRepository extends JpaRepository<VungDich, Long>, JpaSpecificationExecutor<VungDich> {
    List<VungDich> findByMucDo(MucDoVungDichEnum mucDo);
}