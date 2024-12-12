package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.CaBenh;
import com.webgis.dsws.domain.model.enums.TrangThaiEnum;

import org.geolatte.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Date;
import java.util.List;

@Repository
public interface CaBenhRepository extends JpaRepository<CaBenh, Long>, JpaSpecificationExecutor<CaBenh> {

    @Query(value = "SELECT cb.* FROM ca_benh cb " +
            "JOIN trang_trai tt ON cb.trang_trai_id = tt.id " +
            "WHERE ST_DWithin(tt.point, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radiusInMeters)", nativeQuery = true)
    List<CaBenh> findCaBenhsWithinRadius(
            @Param("longitude") double longitude,
            @Param("latitude") double latitude,
            @Param("radiusInMeters") double radiusInMeters);

    @Query(value = "SELECT cb.* FROM ca_benh cb " +
            "JOIN trang_trai tt ON cb.trang_trai_id = tt.id " +
            "WHERE ST_Contains(:geometry, tt.point) = true", nativeQuery = true)
    List<CaBenh> findCaBenhsWithinGeometry(@SuppressWarnings("rawtypes") @Param("geometry") Geometry geometry);

    @Query("SELECT DISTINCT cb FROM CaBenh cb " +
           "LEFT JOIN FETCH cb.benh b " +
           "LEFT JOIN FETCH b.mucDoBenhs " +
           "JOIN FETCH cb.trangTrai tt " +
           "JOIN FETCH tt.donViHanhChinh dvh " +
           "LEFT JOIN FETCH dvh.donViCha dvh_cha " +
           "LEFT JOIN FETCH dvh_cha.donViCha dvh_ong " +
           "WHERE dvh.ranhGioi IS NOT NULL")
    List<CaBenh> findAllWithRegions();

    List<CaBenh> findByTrangThai(TrangThaiEnum trangThai);
}