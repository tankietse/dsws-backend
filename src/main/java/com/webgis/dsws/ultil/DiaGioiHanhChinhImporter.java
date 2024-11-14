package com.webgis.dsws.ultil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webgis.dsws.model.DiaGioiHanhChinh;
import com.webgis.dsws.repository.DiaGioiHanhChinhRepository;
import com.webgis.dsws.service.geojson.GeoJsonDataImporter;
import com.webgis.dsws.service.geojson.GeometryService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonParseException;

@Component
public class DiaGioiHanhChinhImporter implements GeoJsonDataImporter {
    private static final Logger logger = LoggerFactory.getLogger(DiaGioiHanhChinhImporter.class);

    private final DiaGioiHanhChinhRepository repository;
    private final GeometryService geometryService;
    private final ObjectMapper mapper;

    public DiaGioiHanhChinhImporter(
            DiaGioiHanhChinhRepository repository,
            GeometryService geometryService,
            ObjectMapper mapper) {
        this.repository = repository;
        this.geometryService = geometryService;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void importData(String filePath) throws IOException {
        try {
            JsonNode rootNode = mapper.readTree(new File(filePath));
            processFeatures(rootNode.get("features"));
        } catch (Exception e) {
            logger.error("Error importing data: ", e);
            throw e;
        }
    }

    @Transactional
    public void importData(InputStream inputStream) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(inputStream);
            processFeatures(jsonNode.get("features"));
        } catch (JsonParseException e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        }
    }

    private void processFeatures(JsonNode features) {
        features.elements().forEachRemaining(this::processFeature);
    }

    private void processFeature(JsonNode feature) {
        try {
            DiaGioiHanhChinh entity = createEntityFromFeature(feature);

            // Kiểm tra tên địa giới không null
            if (entity.getTenDiaGioi() == null) {
                logger.warn("Bo qua feature vi thieu ten dia gioi");
                return;
            }

            // Tìm entity đã tồn tại
            DiaGioiHanhChinh existingEntity = repository.findByTenDiaGioi(entity.getTenDiaGioi());

            if (existingEntity != null) {
                // Cập nhật thông tin cho entity đã tồn tại
                existingEntity.setGeom(entity.getGeom());
                existingEntity.setCenterPoint(entity.getCenterPoint());
                existingEntity.setAdminLevel(entity.getAdminLevel());
                existingEntity.setPhanLoai();
                existingEntity.setCapHanhChinh();
                repository.save(existingEntity);
            } else {
                // Kiểm tra trùng lặp trước khi lưu entity mới
                if (!isDuplicate(entity)) {
                    entity = repository.save(entity);
                }
            }
        } catch (Exception e) {
            logger.error("Loi xu ly feature: " + e.getMessage());
        }
    }

    private DiaGioiHanhChinh createEntityFromFeature(JsonNode feature)
            throws ParseException, org.locationtech.jts.io.ParseException {
        JsonNode properties = feature.get("properties");
        JsonNode geometry = feature.get("geometry");

        DiaGioiHanhChinh entity = new DiaGioiHanhChinh();
        entity.setAdminLevel(getJsonNodeNumber(properties, "admin_level"));
        entity.setPhanLoai();
        entity.setCapHanhChinh();
        entity.setTenDiaGioi(getJsonNodeText(properties, "name"));
        entity.setTenDiaGioiEn(getJsonNodeText(properties, "name:en"));
        entity.setLoai(getJsonNodeText(properties, "type"));
        entity.setWikidata(getJsonNodeText(properties, "wikidata"));
        entity.setWikipedia(getJsonNodeText(properties, "wikipedia"));

        String tenDonViCha = getJsonNodeText(properties, "is_in:town");
        if (tenDonViCha == null) {
            tenDonViCha = getJsonNodeText(properties, "is_in:city");
        }
        if (tenDonViCha == null) {
            tenDonViCha = getJsonNodeText(properties, "is_in:country_code");
        }
        if (tenDonViCha != null) {
            DiaGioiHanhChinh parentEntity = repository.findByTenDiaGioi(tenDonViCha);
            entity.setDonViCha(parentEntity);
        }

        if (geometry != null) {
            String geometryType = geometry.get("type").asText();
            if ("Polygon".equalsIgnoreCase(geometryType)) {
                Polygon polygon = geometryService.convertToPolygon(geometry.toString());
                if (polygon != null) {
                    entity.setGeom(polygon);
                }
            } else if ("Point".equalsIgnoreCase(geometryType)) {
                Point centerPoint = geometryService.convertToPoint(geometry.toString());
                entity.setCenterPoint(centerPoint);
            }
        }

        return entity;
    }

    private boolean isDuplicate(DiaGioiHanhChinh entity) {
        return repository.existsByTenDiaGioiAndLoaiAndGeom(
                entity.getTenDiaGioi(),
                entity.getLoai(),
                entity.getGeom());
    }

    private String getJsonNodeText(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null ? fieldNode.asText() : null;
    }

    private Integer getJsonNodeNumber(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null ? fieldNode.asInt() : null;
    }
}