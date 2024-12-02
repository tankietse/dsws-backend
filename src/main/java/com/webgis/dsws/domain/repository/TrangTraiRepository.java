package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.TrangTrai;

import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface TrangTraiRepository extends JpaRepository<TrangTrai, Long>, JpaSpecificationExecutor<TrangTrai> {
        @Query(value = "SELECT t FROM TrangTrai t LEFT JOIN FETCH t.donViHanhChinh WHERE t.id = ?1")
        TrangTrai findByIdWithDonViHanhChinh(Long id);

        @Query("SELECT t.maTrangTrai FROM TrangTrai t WHERE t.maTrangTrai IN :maSoList")
        List<String> findExistingMaTrangTrai(List<String> maSoList);

        /**
         * Tìm các trang trại trong khoảng cách nhất định từ một hình học
         * Hỗ trợ cả Point và các loại Geometry khác
         * 
         * @param geometry Hình học tham chiếu (Point hoặc Geometry khác)
         * @param distance Khoảng cách tối đa (meters)
         * @return Danh sách các trang trại tìm được
         */
        @Query(value = "SELECT t.* FROM trang_trai t " +
                        "WHERE ST_DWithin(geography(t.point), geography(:geometry), :distance)", nativeQuery = true)
        List<TrangTrai> findFarmsWithinDistance(
                        @Param("geometry") Geometry geometry,
                        @Param("distance") double distance);

        Optional<TrangTrai> findByTenTrangTrai(String tenTrangTrai);

        @Query(value = "SELECT t FROM TrangTrai t " +
                        "JOIN t.trangTraiVatNuois ttv " +
                        "JOIN ttv.loaiVatNuoi lvn " +
                        "WHERE lvn.tenLoai = :loaiVatNuoi")
        List<TrangTrai> findByLoaiVatNuoi(@Param("loaiVatNuoi") String loaiVatNuoi);

        @Query(value = "SELECT dvhc.ten as ten, COUNT(tt) as soLuong " +
                        "FROM TrangTrai tt " +
                        "JOIN DonViHanhChinh dvhc ON ST_Contains(dvhc.ranhGioi, tt.point) = true " +
                        "WHERE dvhc.adminLevel = 'TINH' " +
                        "GROUP BY dvhc.ten")
        Map<String, Object> thongKeTheoTinh();

        @Query(value = "SELECT dvhc.ten as ten, COUNT(tt) as soLuong " +
                        "FROM TrangTrai tt " +
                        "JOIN DonViHanhChinh dvhc ON ST_Contains(dvhc.ranhGioi, tt.point) = true " +
                        "WHERE dvhc.adminLevel = 'HUYEN' " +
                        "GROUP BY dvhc.ten")
        Map<String, Object> thongKeTheoHuyen();

        @Query(value = "SELECT dvhc.ten as ten, COUNT(tt) as soLuong " +
                        "FROM TrangTrai tt " +
                        "JOIN DonViHanhChinh dvhc ON ST_Contains(dvhc.ranhGioi, tt.point) = true " +
                        "WHERE dvhc.adminLevel = 'XA' " +
                        "GROUP BY dvhc.ten")
        Map<String, Object> thongKeTheoXa();

        @Query(value = "SELECT tt FROM TrangTrai tt " +
                        "JOIN DonViHanhChinh dvhc ON ST_Contains(dvhc.ranhGioi, tt.point) = true " +
                        "WHERE dvhc.id = :donViHanhChinhId")
        List<TrangTrai> findByDonViHanhChinh(@Param("donViHanhChinhId") Integer donViHanhChinhId);

        // @Query(value = "SELECT COUNT(tt) FROM TrangTrai tt WHERE ST_DWithin(tt.point,
        // :point, :radius) = true")
        // long countFarmsWithinRadius(@Param("point") Point point, @Param("radius")
        // double radius);
}