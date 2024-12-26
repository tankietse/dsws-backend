package com.webgis.dsws;

import com.webgis.dsws.exception.DataImportException;
import com.webgis.dsws.importer.DonViHanhChinhImporter;
import com.webgis.dsws.importer.TrangtraiDataImporter;
import com.webgis.dsws.domain.service.VungDichAutoImportService;
import com.webgis.dsws.domain.service.DienBienCaBenhService;

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
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = { "com.webgis.dsws.domain.model" })
@EnableJpaRepositories(basePackages = { "com.webgis.dsws.domain.repository" })
@EnableScheduling
public class DswsApplication {
    private static final Logger log = LoggerFactory.getLogger(DswsApplication.class);

    private final DonViHanhChinhImporter boundaryImporter;
    private final TrangtraiDataImporter farmImporter;
    private final VungDichAutoImportService vungDichAutoImportService;
    private final DienBienCaBenhService dienBienCaBenhService;

    @Value("${app.hochiminh-boundary-path:}")
    private String hoChiMinhBoundaryPath;

    @Value("${app.farm-path:}")
    private String farmPath;

    public DswsApplication(DonViHanhChinhImporter boundaryImporter,
            TrangtraiDataImporter farmImporter,
            VungDichAutoImportService vungDichAutoImportService,
            DienBienCaBenhService dienBienCaBenhService) {
        this.boundaryImporter = boundaryImporter;
        this.farmImporter = farmImporter;
        this.vungDichAutoImportService = vungDichAutoImportService;
        this.dienBienCaBenhService = dienBienCaBenhService;
    }

    public static void main(String[] args) {
        SpringApplication.run(DswsApplication.class, args);
    }

    @Bean
    @Profile("!prod")
    public ApplicationRunner dataImporter() {
        return args -> {
            if (!hoChiMinhBoundaryPath.isEmpty() && !farmPath.isEmpty()) {
                // importBoundaryData();
                // importFarmData();
//                 importVirusZones();

                System.out.println(
                        "Tất cả các dữ liệu cho việc import đã được cung cấp. Bạn có thể import dữ liệu bằng cách bỏ comment ở các hàm importBoundaryData(), importFarmData(), importVirusZones() trong hàm dataImporter() trong file DswsApplication.java");
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
            log.info("Đã import thành công dữ liệu trang trại và ca bệnh");
        } catch (DataImportException e) {
            log.error("Lỗi khi import dữ liệu trang trại: {}", e.getMessage(), e);
        }
    }

    private void importVirusZones() {

        // Import cho cấp phường/xã (8)
        vungDichAutoImportService.autoCreateFromData(8); // Chỉ cần 1 ca để tạo vùng cấp xã

        log.info("Đã import thành công vùng dịch cho các cấp hành chính");
    }
}
