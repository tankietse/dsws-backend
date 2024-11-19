package com.webgis.dsws.repository;

import com.webgis.dsws.model.TrangTrai;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TrangTraiRepository extends JpaRepository<TrangTrai, Long> {
    @Query(value = "SELECT t FROM TrangTrai t LEFT JOIN FETCH t.donViHanhChinh WHERE t.id = ?1")
    TrangTrai findByIdWithDonViHanhChinh(Long id);

    @Query("SELECT t.maSo FROM TrangTrai t WHERE t.maSo IN :maSoList")
    List<String> findExistingMaSo(List<String> maSoList);
}