package com.webgis.dsws.repository;

import com.webgis.dsws.model.CaBenh;
import com.webgis.dsws.model.enums.TrangThaiEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaBenhRepository extends JpaRepository<CaBenh, Long> {
    Optional<CaBenh> findById(Long id);

//    @Query("SELECT cb FROM CaBenh cb WHERE cb.nguoiTao.id = :nguoiDung.id")
//    List<CaBenh> findByNguoiTaoid(@Param("nguoiDungId") Long nguoiDungId);
//
//    // Tìm ca bệnh để chỉnh sửa, đảm bảo chỉ được sửa ca bệnh do mình tạo
//    @Query("SELECT cb FROM CaBenh cb WHERE cb.id = :id AND cb.nguoiTaoId = :nguoiDungId")
//    Optional<CaBenh> findByIdAndNguoiTaoid(@Param("id") Long id, @Param("nguoiDungId") Long nguoiDungId);
//
    // Tìm các ca bệnh ở trạng thái chờ duyệt
    List<CaBenh> findByTrangThai(TrangThaiEnum trangThai);
}