package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.DienBienCaBenh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DienBienCaBenhRepository extends JpaRepository<DienBienCaBenh, Long> {
    List<DienBienCaBenh> findByCaBenhId(Long caBenhId);

    @Query("SELECT d FROM DienBienCaBenh d WHERE d.caBenh.id = :caBenhId ORDER BY d.ngayCapNhat DESC")
    List<DienBienCaBenh> findLatestByCaBenhId(Long caBenhId);
}
