package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.BenhVatNuoi;
import com.webgis.dsws.domain.model.Benh;
import com.webgis.dsws.domain.model.LoaiVatNuoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface BenhVatNuoiRepository extends JpaRepository<BenhVatNuoi, Long> {
    Optional<BenhVatNuoi> findByBenh_IdAndLoaiVatNuoi_Id(Long benhId, Long loaiVatNuoiId);

    List<BenhVatNuoi> findByBenh_TenBenh(String tenBenh);

    List<BenhVatNuoi> findByLoaiVatNuoi_TenLoai(String tenLoai);

    List<BenhVatNuoi> findByBenhId(Long benhId);

    List<BenhVatNuoi> findByLoaiVatNuoiId(Long loaiVatNuoiId);

    boolean existsByBenhAndLoaiVatNuoi(Benh benh, LoaiVatNuoi loaiVatNuoi);
}