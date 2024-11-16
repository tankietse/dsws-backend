package com.webgis.dsws.repository;

import com.webgis.dsws.model.AdminLevel;
import com.webgis.dsws.model.DonViHanhChinh;

import org.locationtech.jts.geom.Geometry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonViHanhChinhRepository extends JpaRepository<DonViHanhChinh, Integer> {
    List<DonViHanhChinh> findByDonViCha(DonViHanhChinh donViCha);

    // Thay đổi findByType thành findByCapHanhChinh vì trong entity là capHanhChinh
    List<DonViHanhChinh> findByCapHanhChinh(String capHanhChinh);

    // @Query("SELECT d FROM DonViHanhChinh d WHERE LOWER(d.ten) LIKE
    // LOWER(CONCAT('%', :pattern, '%'))")
    // List<DonViHanhChinh> findByNameContainingIgnoreCase(String pattern);

    boolean existsById(Integer id);

    boolean existsByTenAndAdminLevelAndRanhGioi(String ten, AdminLevel adminLevel, Geometry ranhGioi);

    DonViHanhChinh findByTen(String tenDonViCha);

    DonViHanhChinh findByTenTiengAnh(String tenTiengAnh);
}