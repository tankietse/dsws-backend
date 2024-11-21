package com.webgis.dsws.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.webgis.dsws.model.TrangTraiBenh;
import com.webgis.dsws.model.Benh;
import com.webgis.dsws.model.TrangTrai;

import java.util.List;

@Repository
public interface TrangTraiBenhRepository extends JpaRepository<TrangTraiBenh, Long> {
    List<TrangTraiBenh> findByTrangTrai(TrangTrai trangTrai);

    List<TrangTraiBenh> findByBenh(Benh benh);

}