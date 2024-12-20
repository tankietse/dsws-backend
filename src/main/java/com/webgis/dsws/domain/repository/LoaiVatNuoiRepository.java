package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.LoaiVatNuoi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface LoaiVatNuoiRepository extends JpaRepository<LoaiVatNuoi, Long> {
        Optional<LoaiVatNuoi> findByTenLoai(String tenLoai);

        List<LoaiVatNuoi> findAllByTenLoaiIn(Collection<String> names);

        @RestResource(path = "search")
        List<LoaiVatNuoi> findByTenLoaiContainingIgnoreCase(String tenLoai);

        @RestResource(path = "searchPaged")
        Page<LoaiVatNuoi> findByTenLoaiContainingIgnoreCase(String tenLoai, Pageable pageable);

        @Query(value = "SELECT l.ten_loai as tenLoai, COUNT(ttv.id) as soLuong " +
                        "FROM loai_vat_nuoi l " +
                        "LEFT JOIN trang_trai_vat_nuoi ttv ON l.id = ttv.id_loai_vat_nuoi " +
                        "GROUP BY l.ten_loai", nativeQuery = true)
        List<Map<String, Object>> getThongKeTheoLoai();

        @Query(value = "SELECT l.ten_loai as tenLoai, COUNT(ttv.id) as soLuong " +
                        "FROM loai_vat_nuoi l " +
                        "LEFT JOIN trang_trai_vat_nuoi ttv ON l.id = ttv.id_loai_vat_nuoi " +
                        "GROUP BY l.ten_loai " +
                        "HAVING COUNT(ttv.id) > 0", nativeQuery = true)
        List<Map<String, Object>> getThongKeTheoLoaiDangNuoi();

        boolean existsByTenLoaiIgnoreCase(String tenLoai);
}