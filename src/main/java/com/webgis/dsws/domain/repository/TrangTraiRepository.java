package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.TrangTrai;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface TrangTraiRepository extends JpaRepository<TrangTrai, Long>, JpaSpecificationExecutor<TrangTrai> {
        @EntityGraph(attributePaths = { "donViHanhChinh", "trangTraiVatNuois", "trangTraiVatNuois.loaiVatNuoi" })
        Optional<TrangTrai> findById(Long id);

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

        @Query(value = "SELECT b.ten_benh, COUNT(cb.id) as so_ca " +
                        "FROM ca_benh cb " +
                        "JOIN benh b ON cb.benh_id = b.id " +
                        "JOIN trang_trai_vat_nuoi ttvn ON cb.trang_trai_vat_nuoi_id = ttvn.id " +
                        "JOIN loai_vat_nuoi lvn ON ttvn.loai_vat_nuoi_id = lvn.id " +
                        "WHERE (:loaiVatNuoi IS NULL OR lvn.ten_loai = :loaiVatNuoi) " +
                        "AND cb.ngay_phat_hien BETWEEN :fromDate AND :toDate " +
                        "GROUP BY b.ten_benh", nativeQuery = true)
        List<Object[]> thongKeCaBenhTheoLoai(
                        @Param("loaiVatNuoi") String loaiVatNuoi,
                        @Param("fromDate") Date fromDate,
                        @Param("toDate") Date toDate);

        @Query(value = "SELECT dvhc.ten, COUNT(DISTINCT tt.id) as so_trang_trai, COUNT(cb.id) as so_ca_benh " +
                        "FROM don_vi_hanh_chinh dvhc " +
                        "JOIN trang_trai tt ON ST_Contains(dvhc.ranh_gioi, tt.point) " +
                        "LEFT JOIN trang_trai_vat_nuoi ttvn ON tt.id = ttvn.trang_trai_id " +
                        "LEFT JOIN ca_benh cb ON ttvn.id = cb.trang_trai_vat_nuoi_id " +
                        "LEFT JOIN loai_vat_nuoi lvn ON ttvn.loai_vat_nuoi_id = lvn.id " +
                        "WHERE dvhc.cap_hanh_chinh = 'TINH' " +
                        "AND (:loaiVatNuoi IS NULL OR lvn.ten_loai = :loaiVatNuoi) " +
                        "AND (cb.ngay_phat_hien IS NULL OR cb.ngay_phat_hien BETWEEN :fromDate AND :toDate) " +
                        "GROUP BY dvhc.ten", nativeQuery = true)
        List<Object[]> thongKePhanBoTheoDonViHanhChinh(
                        @Param("loaiVatNuoi") String loaiVatNuoi,
                        @Param("fromDate") Date fromDate,
                        @Param("toDate") Date toDate);

        @Query(value = "SELECT DATE_TRUNC('day', cb.ngay_phat_hien) as ngay, " +
                        "COUNT(CASE WHEN cb.da_ket_thuc = false THEN 1 END) as ca_moi, " +
                        "COUNT(CASE WHEN cb.da_ket_thuc = true THEN 1 END) as ca_ket_thuc " +
                        "FROM ca_benh cb " +
                        "JOIN trang_trai_vat_nuoi ttvn ON cb.trang_trai_vat_nuoi_id = ttvn.id " +
                        "JOIN loai_vat_nuoi lvn ON ttvn.loai_vat_nuoi_id = lvn.id " +
                        "WHERE (:loaiVatNuoi IS NULL OR lvn.ten_loai = :loaiVatNuoi) " +
                        "AND cb.ngay_phat_hien BETWEEN :fromDate AND :toDate " +
                        "GROUP BY DATE_TRUNC('day', cb.ngay_phat_hien) " +
                        "ORDER BY ngay", nativeQuery = true)
        List<Object[]> thongKeXuHuongTheoDonVithoiGian(
                        @Param("loaiVatNuoi") String loaiVatNuoi,
                        @Param("fromDate") Date fromDate,
                        @Param("toDate") Date toDate);

        @Query(value = "SELECT tt.point as location, COUNT(*) as density " +
                        "FROM trang_trai tt " +
                        "JOIN trang_trai_vat_nuoi ttvn ON tt.id = ttvn.trang_trai_id " +
                        "JOIN loai_vat_nuoi lvn ON ttvn.loai_vat_nuoi_id = lvn.id " +
                        "JOIN don_vi_hanh_chinh dvhc ON ST_Contains(dvhc.ranh_gioi, tt.point) " +
                        "WHERE (:loaiVatNuoi IS NULL OR lvn.ten_loai = :loaiVatNuoi) " +
                        "AND (:capHanhChinh IS NULL OR dvhc.cap_hanh_chinh = :capHanhChinh) " +
                        "GROUP BY tt.point", nativeQuery = true)
        List<Object[]> getDensityPoints(
                        @Param("loaiVatNuoi") String loaiVatNuoi,
                        @Param("capHanhChinh") String capHanhChinh);

        @Query(value = "SELECT b.ten_benh, " +
                        "ST_Distance(tt.point::geography, :point::geography) as distance, " +
                        "cb.ngay_phat_hien " +
                        "FROM ca_benh cb " +
                        "JOIN benh b ON cb.benh_id = b.id " +
                        "JOIN trang_trai_vat_nuoi ttvn ON cb.trang_trai_vat_nuoi_id = ttvn.id " +
                        "JOIN trang_trai tt ON ttvn.trang_trai_id = tt.id " +
                        "WHERE cb.da_ket_thuc = false " +
                        "AND cb.ngay_phat_hien > :date " +
                        "AND ST_DWithin(tt.point::geography, :point::geography, :radius) " +
                        "ORDER BY distance", nativeQuery = true)
        List<Object[]> findRecentDiseaseCasesNearby(
                        @Param("point") Point point,
                        @Param("radius") Double radius,
                        @Param("date") Date date);

        @Query("SELECT DISTINCT tt FROM TrangTrai tt " +
                        "JOIN FETCH tt.donViHanhChinh dvhc " +
                        "JOIN FETCH tt.trangTraiVatNuois ttvn " +
                        "JOIN FETCH ttvn.loaiVatNuoi " +
                        "WHERE dvhc.id IN :donViHanhChinhIds")
        List<TrangTrai> findByDonViHanhChinhIds(@Param("donViHanhChinhIds") List<Integer> donViHanhChinhIds);

        // @Query(value = "SELECT tt FROM TrangTrai tt " +
        // "LEFT JOIN FETCH tt.trangTraiVatNuois ttvn " +
        // "LEFT JOIN FETCH tt.vungDichs vdt " +
        // "LEFT JOIN FETCH tt.caBenhs cb " +
        // "WHERE tt.id = :id")
        // Optional<TrangTrai> findByIdWithFullDetails(@Param("id") Long id);

        @Query("SELECT DISTINCT tt FROM TrangTrai tt " +
                        "LEFT JOIN FETCH tt.trangTraiVatNuois ttvn " +
                        "LEFT JOIN FETCH ttvn.loaiVatNuoi " +
                        "LEFT JOIN FETCH tt.caBenhs cb " +
                        "LEFT JOIN FETCH cb.benh " +
                        "LEFT JOIN FETCH tt.vungDichs vdt " +
                        "LEFT JOIN FETCH vdt.vungDich " +
                        "WHERE tt.id = :id")
        Optional<TrangTrai> findByIdWithFullDetails(@Param("id") Long id);
}