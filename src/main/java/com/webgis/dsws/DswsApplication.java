package com.webgis.dsws;

import com.webgis.dsws.ultil.DiaGioiHanhChinhImporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
public class DswsApplication {

//	@Value("${app.farm-path}")
//	private String farmPath;

	@Value("${app.hochiminh-boundary-path}")
	private String hoChiMinhBoundaryPath;

	private final DiaGioiHanhChinhImporter boundaryImporter;

	public DswsApplication(DiaGioiHanhChinhImporter boundaryImporter) {
		this.boundaryImporter = boundaryImporter;
	}

	public static void main(String[] args) {
		SpringApplication.run(DswsApplication.class, args);
	}

	@Bean
	public ApplicationRunner importData() {
		return args -> {
			try (InputStream inputStream = new FileInputStream(hoChiMinhBoundaryPath)) {
				boundaryImporter.importData(inputStream);
			} catch (IOException e) {
				System.err.println("Failed to import data: " + e.getMessage());
			}
		};
	}
}
