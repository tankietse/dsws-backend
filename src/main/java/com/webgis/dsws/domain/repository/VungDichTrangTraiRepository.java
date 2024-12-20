package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.ids.VungDichTrangTraiId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.webgis.dsws.domain.model.VungDichTrangTrai;
import com.webgis.dsws.domain.model.VungDich;
import com.webgis.dsws.domain.model.TrangTrai;

import java.util.List;

@Repository
public interface VungDichTrangTraiRepository extends JpaRepository<VungDichTrangTrai, VungDichTrangTraiId> {
    List<VungDichTrangTrai> findByVungDich(VungDich vungDich);

    List<VungDichTrangTrai> findByTrangTrai(TrangTrai trangTrai);

    List<VungDichTrangTrai> findByTrangTraiAndKhoangCachLessThan(TrangTrai trangTrai, Double radius);

    List<VungDichTrangTrai> findByVungDichId(Long id);
}