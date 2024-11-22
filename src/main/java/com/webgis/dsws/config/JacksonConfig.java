package com.webgis.dsws.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.webgis.dsws.config.serializer.GeometrySerializer;
import org.locationtech.jts.geom.*;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Configuration
public class JacksonConfig {
    @Bean("customObjectMapper")
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        // Register JTS module
        mapper.registerModule(new JtsModule());

        // Register Hibernate module
        Hibernate6Module hibernateModule = new Hibernate6Module();
        hibernateModule.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false);
        hibernateModule.configure(Hibernate6Module.Feature.USE_TRANSIENT_ANNOTATION, true);
        mapper.registerModule(hibernateModule);

        // Custom module for geometry serialization
        SimpleModule geometryModule = new SimpleModule();
        geometryModule.addSerializer(Point.class, new GeometrySerializer());
        geometryModule.addSerializer(Polygon.class, new GeometrySerializer());
        geometryModule.addSerializer(MultiPolygon.class, new GeometrySerializer());
        geometryModule.addSerializer(LineString.class, new GeometrySerializer());
        geometryModule.addSerializer(MultiLineString.class, new GeometrySerializer());
        mapper.registerModule(geometryModule);

        // Configure other settings
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }
}
