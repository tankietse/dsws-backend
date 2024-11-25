package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.DonViHanhChinh;
import com.webgis.dsws.domain.model.enums.AdminLevelEnum;

import org.locationtech.jts.geom.Geometry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonViHanhChinhRepository extends JpaRepository<DonViHanhChinh, Integer> {
    List<DonViHanhChinh> findByDonViCha(DonViHanhChinh donViCha);

    List<DonViHanhChinh> findByCapHanhChinh(String capHanhChinh);

    // @Query("SELECT d FROM DonViHanhChinh d WHERE LOWER(d.ten) LIKE
    // LOWER(CONCAT('%', :pattern, '%'))")
    // List<DonViHanhChinh> findByNameContainingIgnoreCase(String pattern);

    boolean existsById(Integer id);

    boolean existsByTenAndAdminLevelAndRanhGioi(String ten, AdminLevelEnum adminLevel, Geometry ranhGioi);

    DonViHanhChinh findByTen(String tenDonViCha);

    DonViHanhChinh findByTenTiengAnh(String tenTiengAnh);

    @Query("SELECT d FROM DonViHanhChinh d WHERE LOWER(FUNCTION('unaccent', d.ten)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :ten, '%')))")
    List<DonViHanhChinh> findByTenContainingIgnoreCaseAndDiacritics(String ten);

    Optional<DonViHanhChinh> findById(Integer maDonVi);

    List<DonViHanhChinh> findByTenAndCapHanhChinh(String ten, String capHanhChinh);

    @Query("SELECT d.id FROM DonViHanhChinh d WHERE d.id = :rootId OR d.donViCha.id = :rootId OR EXISTS (SELECT 1 FROM DonViHanhChinh p WHERE p.donViCha.id = :rootId AND d.donViCha.id = p.id)")
    List<Integer> findAllChildrenIds(@Param("rootId") Integer rootId);
}