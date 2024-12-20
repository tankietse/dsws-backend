package com.webgis.dsws.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webgis.dsws.domain.model.VungDich;
import com.webgis.dsws.domain.model.enums.MucDoVungDichEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VungDichRepository extends JpaRepository<VungDich, Long>, JpaSpecificationExecutor<VungDich> {
    List<VungDich> findByMucDo(MucDoVungDichEnum mucDo);

    @Query("SELECT v FROM VungDich v LEFT JOIN FETCH v.trangTrais WHERE v.ngayKetThuc IS NULL")
    List<VungDich> findByNgayKetThucIsNullWithTrangTrais();

    List<VungDich> findByMucDoAndNgayBatDauBetween(MucDoVungDichEnum mucDo, LocalDate startDate, LocalDate endDate);

    @Query("SELECT DISTINCT vd FROM VungDich vd " +
           "LEFT JOIN FETCH vd.benh b " +
           "LEFT JOIN FETCH b.benhVatNuois bvn " +
           "LEFT JOIN FETCH bvn.loaiVatNuoi lvn " +
           "LEFT JOIN FETCH vd.trangTrais vt " +
           "LEFT JOIN FETCH vt.trangTrai tt " +
           "LEFT JOIN FETCH tt.donViHanhChinh " +
           "WHERE lvn.id = :loaiVatNuoiId " +
           "AND (:benhId IS NULL OR b.id = :benhId)")
    List<VungDich> findByAnimalTypeAndDisease(@Param("loaiVatNuoiId") Long loaiVatNuoiId, @Param("benhId") Long benhId);

    @Query("SELECT DISTINCT v FROM VungDich v " +
            "LEFT JOIN FETCH v.trangTrais vt " +
            "LEFT JOIN FETCH vt.trangTrai")
    List<VungDich> findAllWithTrangTrais();

    @Override
    @Query("SELECT DISTINCT v FROM VungDich v " +
            "LEFT JOIN FETCH v.trangTrais vt " +
            "LEFT JOIN FETCH vt.trangTrai " +
            "WHERE v.id = :id")
    Optional<VungDich> findById(@Param("id") Long id);
}