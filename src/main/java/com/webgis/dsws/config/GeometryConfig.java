package com.webgis.dsws.config;

import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Lớp cấu hình các thành phần xử lý Geometry cho ứng dụng GIS.
 * Cung cấp các bean để đọc dữ liệu địa lý ở các định dạng khác nhau.
 */
@Configuration
public class GeometryConfig {
    /**
     * Tạo WKTReader bean để đọc dữ liệu geometry ở định dạng WKT (Well-Known Text).
     * @return WKTReader instance mới
     */
    @Bean
    public WKTReader wktReader() {
        return new WKTReader();
    }

    /**
     * Tạo WKBReader bean để đọc dữ liệu geometry ở định dạng WKB (Well-Known Binary).
     * @return WKBReader instance mới
     */
    @Bean 
    public WKBReader wkbReader() {
        return new WKBReader();
    }
}