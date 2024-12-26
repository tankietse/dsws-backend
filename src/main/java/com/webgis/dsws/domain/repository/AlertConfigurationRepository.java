package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.alert.AlertConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlertConfigurationRepository extends JpaRepository<AlertConfiguration, Long> {
    List<AlertConfiguration> findByEnabled(Boolean enabled);

    @Query("SELECT ac FROM AlertConfiguration ac LEFT JOIN FETCH ac.mucDoVungDichs WHERE ac.enabled = :enabled")
    List<AlertConfiguration> findByEnabledWithMucDoVungDichs(@Param("enabled") Boolean enabled);

}
