package com.webgis.dsws;

import com.webgis.dsws.exception.DataImportException;
import com.webgis.dsws.util.DonViHanhChinhImporter;
import com.webgis.dsws.util.TrangtraiDataImporter;

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

@SpringBootApplication
@EntityScan(basePackages = { "com.webgis.dsws.model" })
@EnableJpaRepositories(basePackages = { "com.webgis.dsws.repository" })
public class DswsApplication {

    @Value("${app.hochiminh-boundary-path}")
    private String hoChiMinhBoundaryPath;

    @Value("${app.farm-path}")
    private String farmPath;

    private final DonViHanhChinhImporter boundaryImporter;
    private final TrangtraiDataImporter farmImporter; // Add this

    public DswsApplication(DonViHanhChinhImporter boundaryImporter,
            TrangtraiDataImporter farmImporter) { // Update constructor
        this.boundaryImporter = boundaryImporter;
        this.farmImporter = farmImporter;
    }

    public static void main(String[] args) {
        SpringApplication.run(DswsApplication.class, args);
    }

    @Bean
    public ApplicationRunner importData() {
        return args -> {
            try {
                // Import dữ liệu ranh giới hành chính
                try (InputStream inputStream = new FileInputStream(hoChiMinhBoundaryPath)) {
                    boundaryImporter.importData(inputStream);
                    System.out.println("Đã import thành công dữ liệu ranh giới hành chính");
                }

                // Import dữ liệu trang trại
                try {
                    farmImporter.importData(farmPath);
                    System.out.println("Đã import thành công dữ liệu trang trại");
                } catch (DataImportException e) {
                    System.err.println("Chi tiết lỗi import dữ liệu trang trại:");
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            } catch (IOException e) {
                System.err.println("Lỗi khi đọc file: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
