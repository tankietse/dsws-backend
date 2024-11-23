package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.TrangTrai;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.locationtech.jts.geom.Geometry;

@Repository
public interface TrangTraiRepository extends JpaRepository<TrangTrai, Long> {
    @Query(value = "SELECT t FROM TrangTrai t LEFT JOIN FETCH t.donViHanhChinh WHERE t.id = ?1")
    TrangTrai findByIdWithDonViHanhChinh(Long id);

    @Query("SELECT t.maTrangTrai FROM TrangTrai t WHERE t.maTrangTrai IN :maSoList")
    List<String> findExistingMaTrangTrai(List<String> maSoList);

    /**
     * Tìm các trang trại trong khoảng cách nhất định từ một hình học
     * 
     * @param geom     Hình học tham chiếu
     * @param distance Khoảng cách tối đa (meters)
     * @return Danh sách các trang trại tìm được
     */
    @Query(value = "SELECT t.* FROM trang_trai t WHERE ST_DWithin(geography(t.point), geography(?1), ?2)", nativeQuery = true)
    List<TrangTrai> findFarmsWithinDistance(Geometry geom, double distance);

    Optional<TrangTrai> findByTenTrangTrai(String tenTrangTrai);
}