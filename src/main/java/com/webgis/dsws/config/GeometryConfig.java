package com.webgis.dsws.config;

import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeometryConfig {
    @Bean
    public WKTReader wktReader() {
        return new WKTReader();
    }

    @Bean
    public WKBReader wkbReader() {
        return new WKBReader();
    }
}