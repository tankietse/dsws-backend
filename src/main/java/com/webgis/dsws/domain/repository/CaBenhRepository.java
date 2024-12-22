package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.CaBenh;
import com.webgis.dsws.domain.model.enums.MucDoBenhEnum;
import com.webgis.dsws.domain.model.enums.TrangThaiEnum;

import org.geolatte.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

        Page<CaBenh> findByTrangThai(TrangThaiEnum trangThai, Pageable pageable);

        @Query("SELECT DISTINCT cb FROM CaBenh cb " +
                        "LEFT JOIN FETCH cb.benh b " +
                        "LEFT JOIN FETCH cb.trangTrai tt " +
                        "LEFT JOIN FETCH cb.nguoiTao " +
                        "LEFT JOIN FETCH cb.nguoiDuyet " +
                        "LEFT JOIN FETCH tt.donViHanhChinh dvh " +
                        "WHERE cb.id = :id")
        Optional<CaBenh> findByIdWithDetails(@Param("id") Long id);

        @Query("SELECT DISTINCT cb FROM CaBenh cb " +
                        "LEFT JOIN FETCH cb.benh b " +
                        "LEFT JOIN FETCH cb.trangTrai tt " +
                        "LEFT JOIN FETCH tt.donViHanhChinh " +
                        "ORDER BY cb.ngayPhatHien DESC")
        List<CaBenh> findAllWithDetails();

        @Query("SELECT DISTINCT cb FROM CaBenh cb " +
                        "LEFT JOIN FETCH cb.benh b " +
                        "LEFT JOIN FETCH cb.trangTrai tt " +
                        "LEFT JOIN FETCH tt.donViHanhChinh dvh " +
                        "WHERE cb.daKetThuc = :daKetThuc")
        List<CaBenh> findByDaKetThuc(@Param("daKetThuc") boolean daKetThuc);

        @Query("SELECT DISTINCT cb FROM CaBenh cb " +
                        "LEFT JOIN FETCH cb.benh b " +
                        "LEFT JOIN FETCH cb.trangTrai tt " +
                        "LEFT JOIN FETCH tt.donViHanhChinh dvh " +
                        "WHERE tt.donViHanhChinh.id = :donViHanhChinhId")
        List<CaBenh> findByDonViHanhChinhId(@Param("donViHanhChinhId") Integer donViHanhChinhId);

        @Query("SELECT DISTINCT cb FROM CaBenh cb " +
                        "LEFT JOIN FETCH cb.benh b " +
                        "LEFT JOIN FETCH cb.trangTrai tt " +
                        "LEFT JOIN FETCH tt.donViHanhChinh dvh " +
                        "WHERE b.id = :benhId")
        List<CaBenh> findByBenhId(@Param("benhId") Long benhId);

        @Query(value = "SELECT DISTINCT cb FROM CaBenh cb " +
                        "LEFT JOIN FETCH cb.benh b " +
                        "LEFT JOIN FETCH cb.trangTrai tt " +
                        "LEFT JOIN FETCH tt.donViHanhChinh dvh " +
                        "WHERE cb.trangThai = :trangThai", countQuery = "SELECT COUNT(cb) FROM CaBenh cb WHERE cb.trangThai = :trangThai")
        Page<CaBenh> findByTrangThaiWithDetails(@Param("trangThai") TrangThaiEnum trangThai, Pageable pageable);

        @Query("SELECT COUNT(cb) > 0 FROM CaBenh cb " +
                        "WHERE cb.trangTrai.id = :trangTraiId " +
                        "AND cb.benh.id = :benhId " +
                        "AND cb.daKetThuc = false")
        boolean existsActiveCaseByTrangTraiAndBenh(
                        @Param("trangTraiId") Long trangTraiId,
                        @Param("benhId") Long benhId);

        @Query("SELECT cb FROM CaBenh cb " +
                        "LEFT JOIN FETCH cb.benh b " +
                        "LEFT JOIN FETCH cb.trangTrai tt " +
                        "WHERE cb.ngayPhatHien BETWEEN :startDate AND :endDate")
        List<CaBenh> findByDateRange(
                        @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate);

        @Query("SELECT DISTINCT cb FROM CaBenh cb " +
                        "LEFT JOIN FETCH cb.benh b " +
                        "LEFT JOIN FETCH cb.trangTrai tt " +
                        "LEFT JOIN FETCH tt.donViHanhChinh dvh " +
                        "WHERE :mucDoBenh MEMBER OF b.mucDoBenhs")
        List<CaBenh> findByMucDoBenh(@Param("mucDoBenh") MucDoBenhEnum mucDoBenh);

        @Query("SELECT DISTINCT cb FROM CaBenh cb " +
                        "LEFT JOIN FETCH cb.benh b " +
                        "LEFT JOIN FETCH cb.trangTrai tt " +
                        "LEFT JOIN FETCH tt.donViHanhChinh dvh " +
                        "LEFT JOIN b.mucDoBenhs md " +
                        "WHERE md IN :mucDoBenhs")
        List<CaBenh> findByMucDoBenhs(@Param("mucDoBenhs") Set<MucDoBenhEnum> mucDoBenhs);
}