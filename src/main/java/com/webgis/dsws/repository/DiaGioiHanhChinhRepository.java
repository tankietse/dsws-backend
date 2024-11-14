package com.webgis.dsws.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.webgis.dsws.model.DiaGioiHanhChinh;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
// JTS thay vì AWT
import org.locationtech.jts.geom.Polygon;

public interface DiaGioiHanhChinhRepository extends JpaRepository<DiaGioiHanhChinh, Long> {

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DiaGioiHanhChinh d WHERE d.tenDiaGioi = :tenDiaGioi AND d.loai = :loai AND d.geom = :geom")
    boolean existsByTenDiaGioiAndLoaiAndGeom(@Param("tenDiaGioi") String tenDiaGioi, @Param("loai") String loai, @Param("geom") Polygon geom);


    DiaGioiHanhChinh findByTenDiaGioi(String hoChiMinhCity);
}