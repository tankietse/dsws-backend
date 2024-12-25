package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.ids.VungDichTrangTraiId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.webgis.dsws.domain.model.VungDichTrangTrai;
import com.webgis.dsws.domain.model.VungDich;
import com.webgis.dsws.domain.model.TrangTrai;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface VungDichTrangTraiRepository extends JpaRepository<VungDichTrangTrai, VungDichTrangTraiId> {
    List<VungDichTrangTrai> findByVungDich(VungDich vungDich);

    List<VungDichTrangTrai> findByTrangTrai(TrangTrai trangTrai);

    List<VungDichTrangTrai> findByTrangTraiAndKhoangCachLessThan(TrangTrai trangTrai, Double radius);

    List<VungDichTrangTrai> findByVungDichId(Long id);

    @Modifying
    @Query("DELETE FROM VungDichTrangTrai vdt WHERE vdt.vungDich = :vungDich AND vdt.trangTrai = :trangTrai")
    void deleteByVungDichAndTrangTrai(
        @Param("vungDich") VungDich vungDich,
        @Param("trangTrai") TrangTrai trangTrai
    );
//
//    @Query("SELECT vdt FROM VungDichTrangTrai vdt " +
//           "LEFT JOIN FETCH vdt.trangTrai tt " +
//           "LEFT JOIN FETCH tt.donViHanhChinh " +
//           "WHERE vdt.vungDich.id = :vungDichId")
//    List<VungDichTrangTrai> findByVungDichId(@Param("vungDichId") Long vungDichId);
}