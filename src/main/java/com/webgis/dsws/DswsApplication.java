package com.webgis.dsws;

import com.webgis.dsws.exception.DataImportException;
import com.webgis.dsws.importer.DonViHanhChinhImporter;
import com.webgis.dsws.importer.TrangtraiDataImporter;
import com.webgis.dsws.domain.service.VungDichAutoImportService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@EntityScan(basePackages = { "com.webgis.dsws.domain.model" })
@EnableJpaRepositories(basePackages = { "com.webgis.dsws.domain.repository" })
public class DswsApplication {
    private static final Logger log = LoggerFactory.getLogger(DswsApplication.class);

    private final DonViHanhChinhImporter boundaryImporter;
    private final TrangtraiDataImporter farmImporter;
    private final VungDichAutoImportService vungDichAutoImportService;

    @Value("${app.hochiminh-boundary-path:}")
    private String hoChiMinhBoundaryPath;

    @Value("${app.farm-path:}")
    private String farmPath;

    public DswsApplication(DonViHanhChinhImporter boundaryImporter,
            TrangtraiDataImporter farmImporter,
            VungDichAutoImportService vungDichAutoImportService) {
        this.boundaryImporter = boundaryImporter;
        this.farmImporter = farmImporter;
        this.vungDichAutoImportService = vungDichAutoImportService;
    }

    public static void main(String[] args) {
        SpringApplication.run(DswsApplication.class, args);
    }

    @Bean
    @Profile("!prod")
    public ApplicationRunner dataImporter() {
        return args -> {
            if (!hoChiMinhBoundaryPath.isEmpty() && !farmPath.isEmpty()) {
                importBoundaryData();
                importFarmData();
                importVirusZones();
            }
        };
    }

    private void importBoundaryData() {
        try (InputStream inputStream = new FileInputStream(hoChiMinhBoundaryPath)) {
            boundaryImporter.importData(inputStream);
            log.info("Đã import thành công dữ liệu ranh giới hành chính");
        } catch (IOException e) {
            log.error("Lỗi khi import dữ liệu ranh giới: {}", e.getMessage(), e);
        }
    }

    private void importFarmData() {
        try {
            farmImporter.importData(farmPath);
            log.info("Đã import thành công dữ liệu trang trại");
        } catch (DataImportException e) {
            log.error("Lỗi khi import dữ liệu trang trại: {}", e.getMessage(), e);
        }
    }

    private void importVirusZones() {

        // Import cho cấp phường/xã (8)
        vungDichAutoImportService.autoCreateFromData(8, 1); // Chỉ cần 1 ca để tạo vùng cấp xã

        // Import cho cấp quận/huyện (6)
        vungDichAutoImportService.autoCreateFromData(6, 1); // Cần ít nhất 3 ca để tạo vùng cấp huyện
        // Import cho cấp tỉnh (4)
        vungDichAutoImportService.autoCreateFromData(4, 1); // Cần ít nhất 5 ca để tạo vùng cấp tỉnh

        log.info("Đã import thành công vùng dịch cho các cấp hành chính");
    }
}
