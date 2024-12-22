package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.Benh;
import com.webgis.dsws.domain.model.enums.MucDoBenhEnum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.util.Set;

@Repository
public interface BenhRepository extends JpaRepository<Benh, Long> {

       Page<Benh> findAll(Pageable pageable);

       Optional<Benh> findById(Long maBenh);

       Optional<Benh> findByTenBenh(String tenBenh);

       List<Benh> findByTenBenhIn(Set<String> tenBenhs);

       List<Benh> findByMucDoBenhsIn(Set<MucDoBenhEnum> mucDoBenhs);

       @Query("SELECT DISTINCT b FROM Benh b " +
                     "LEFT JOIN FETCH b.loaiVatNuoi lvn " +
                     "LEFT JOIN FETCH b.mucDoBenhs " +
                     "WHERE b.id = :id")
       Optional<Benh> findByIdWithCollections(@Param("id") Long id);

       @Query("SELECT DISTINCT b FROM Benh b " +
                     "LEFT JOIN FETCH b.loaiVatNuoi " +
                     "LEFT JOIN FETCH b.mucDoBenhs")
       List<Benh> findAllWithCollections();

       @Query("SELECT DISTINCT b FROM Benh b JOIN b.loaiVatNuoi lvn WHERE lvn.id = :loaiVatNuoiId")
       List<Benh> findByLoaiVatNuoiId(@Param("loaiVatNuoiId") Long loaiVatNuoiId);

       @Query("SELECT DISTINCT b FROM Benh b WHERE LOWER(b.tenBenh) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.moTa) LIKE LOWER(CONCAT('%', :keyword, '%'))")
       List<Benh> searchByKeyword(@Param("keyword") String keyword);

       @Query("SELECT b FROM Benh b WHERE " +
                     "LOWER(b.tenBenh) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(b.moTa) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(b.tacNhanGayBenh) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(b.trieuChung) LIKE LOWER(CONCAT('%', :keyword, '%'))")
       Page<Benh> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

       @Query("SELECT DISTINCT b FROM Benh b JOIN b.benhVatNuois bv WHERE bv.loaiVatNuoi.id = :loaiVatNuoiId")
       List<Benh> findBenhsByLoaiVatNuoiIdViaBenhVatNuoi(@Param("loaiVatNuoiId") Long loaiVatNuoiId);
}