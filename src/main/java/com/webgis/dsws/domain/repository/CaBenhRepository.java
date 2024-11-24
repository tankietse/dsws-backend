package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.CaBenh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Date;
import java.util.List;

@Repository
public interface CaBenhRepository extends JpaRepository<CaBenh, Long>, JpaSpecificationExecutor<CaBenh> {

    @Query("SELECT cb FROM CaBenh cb " +
           "WHERE (:fromDate IS NULL OR cb.ngayPhatHien >= :fromDate) " +
           "AND (:toDate IS NULL OR cb.ngayPhatHien <= :toDate) " +
           "AND (:maTinhThanh IS NULL OR cb.trangTrai.donViHanhChinh.id = :maTinhThanh) " +
           "AND (:loaiBenh IS NULL OR cb.benh.tenBenh = :loaiBenh)")
    List<CaBenh> findByFilters(
        @Param("fromDate") Date fromDate,
        @Param("toDate") Date toDate, 
        @Param("maTinhThanh") String maTinhThanh,
        @Param("loaiBenh") String loaiBenh
    );
}