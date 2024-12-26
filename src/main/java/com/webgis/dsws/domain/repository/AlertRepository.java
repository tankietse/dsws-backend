package com.webgis.dsws.domain.repository;

import com.webgis.dsws.domain.model.CaBenh;
import com.webgis.dsws.domain.model.VungDich;
import com.webgis.dsws.domain.model.alert.Alert;
import com.webgis.dsws.domain.model.alert.AlertConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    boolean existsByConfigurationAndCaBenh(AlertConfiguration config, CaBenh caBenh);
    boolean existsByConfigurationAndVungDich(AlertConfiguration config, VungDich vungDich);
}
