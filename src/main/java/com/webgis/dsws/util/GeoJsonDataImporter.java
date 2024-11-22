package com.webgis.dsws.util;

import java.io.IOException;
import java.io.InputStream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParseException;

public interface GeoJsonDataImporter {
    void importData(String filePath) throws IOException;

    default void importGeoJson(InputStream inputStream) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(inputStream);

            if (!jsonNode.has("type") || !jsonNode.get("type").asText().equals("FeatureCollection")) {
                throw new IllegalArgumentException("GeoJSON khong hop le: Phai la FeatureCollection");
            }

            JsonNode features = jsonNode.get("features");
            if (features == null || !features.isArray()) {
                throw new IllegalArgumentException("GeoJSON khong hop le: Thieu mang features");
            }

            for (JsonNode feature : features) {
                JsonNode geometry = feature.get("geometry");
                JsonNode properties = feature.get("properties");

                if (geometry == null || properties == null) {
                    continue; // Skip invalid features
                }

                processFeature(geometry, properties);
            }
        } catch (JsonParseException e) {
            System.err.println("Loi xu ly GeoJSON: " + e.getMessage());
            throw new RuntimeException("Khong the xu ly GeoJSON", e);
        } catch (IOException e) {
            System.err.println("Loi doc file: " + e.getMessage());
            throw new RuntimeException("Khong the doc file GeoJSON", e);
        }
    }

    default void processFeature(JsonNode geometry, JsonNode properties) {
        throw new UnsupportedOperationException("Phuong thuc processFeature chua duoc cai dat");
    }
}