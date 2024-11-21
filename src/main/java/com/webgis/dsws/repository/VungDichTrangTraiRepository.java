package com.webgis.dsws.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.webgis.dsws.model.VungDichTrangTrai;
import com.webgis.dsws.model.VungDichTrangTraiId;
import com.webgis.dsws.model.VungDich;
import com.webgis.dsws.model.TrangTrai;

import java.util.List;

@Repository
public interface VungDichTrangTraiRepository extends JpaRepository<VungDichTrangTrai, VungDichTrangTraiId> {
    List<VungDichTrangTrai> findByVungDich(VungDich vungDich);

    List<VungDichTrangTrai> findByTrangTrai(TrangTrai trangTrai);
}