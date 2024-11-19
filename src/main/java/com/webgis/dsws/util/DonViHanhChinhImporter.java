package com.webgis.dsws.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webgis.dsws.model.AdminLevel;
import com.webgis.dsws.model.DonViHanhChinh;
import com.webgis.dsws.repository.DonViHanhChinhRepository;
import com.webgis.dsws.service.geojson.GeoJsonDataImporter;
import com.webgis.dsws.service.geojson.GeometryService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonParseException;

@Component
public class DonViHanhChinhImporter implements GeoJsonDataImporter {
    private static final Logger logger = LoggerFactory.getLogger(DonViHanhChinhImporter.class);

    private final DonViHanhChinhRepository repository;
    private final GeometryService geometryService;
    private final ObjectMapper mapper;

    public DonViHanhChinhImporter(
            DonViHanhChinhRepository repository,
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
        // Tách riêng các feature điểm
        List<JsonNode> pointFeatures = new ArrayList<>();
        // Thu thập các feature theo cấp hành chính
        Map<Integer, List<JsonNode>> featuresByLevel = new TreeMap<>();

        features.elements().forEachRemaining(feature -> {
            if (isPointWithRelations(feature)) {
                pointFeatures.add(feature);
            } else {
                int adminLevel = getAdminLevel(feature);
                featuresByLevel.computeIfAbsent(adminLevel, k -> new ArrayList<>())
                        .add(feature);
            }
        });

        // Xử lý các feature theo thứ tự cấp hành chính
        featuresByLevel.forEach((level, levelFeatures) -> {
            logger.info("Đang xử lý các feature cấp {}", level);
            levelFeatures.forEach(this::processFeature);
        });

        // Xử lý các điểm trung tâm cuối cùng
        logger.info("Đang xử lý các điểm trung tâm");
        pointFeatures.forEach(this::processFeature);
    }

    private int getAdminLevel(JsonNode feature) {
        JsonNode properties = feature.get("properties");
        String adminLevelStr = getJsonNodeText(properties, "admin_level");
        try {
            return adminLevelStr != null ? Integer.parseInt(adminLevelStr) : Integer.MAX_VALUE;
        } catch (NumberFormatException e) {
            logger.warn("Giá trị admin_level không hợp lệ: {}, xử lý như cấp cao nhất", adminLevelStr);
            return Integer.MAX_VALUE;
        }
    }

    private void processFeature(JsonNode feature) {
        try {
            JsonNode properties = feature.get("properties");
            JsonNode geometryNode = feature.get("geometry");

            // Kiểm tra nếu là điểm và có relations
            if (isPointWithRelations(feature)) {
                processCenterPoint(feature);
                return;
            }

            DonViHanhChinh entity = createEntityFromFeature(feature);

            // Kiểm tra tên địa giới không null
            if (entity.getTen() == null) {
                logger.warn("Bo qua feature vi thieu ten dia gioi");
                return;
            }

            // Tìm entity đã tồn tại
            DonViHanhChinh existingEntity = repository.findByTen(entity.getTen());

            if (existingEntity != null) {
                // Cập nhật thông tin cho entity đã tồn tại
                existingEntity.setTenTiengAnh(entity.getTenTiengAnh());
                existingEntity.setDonViCha(entity.getDonViCha());
                existingEntity.setAdminLevel(entity.getAdminLevel());
                existingEntity.setRanhGioi(entity.getRanhGioi());

                repository.save(existingEntity);
            } else {
                // Kiểm tra trùng lặp trước khi lưu entity mới
                if (!isDuplicate(entity)) {
                    entity = repository.save(entity);
                }
            }
        } catch (Exception e) {
            logger.error("Loi xu ly feature: {}", e.getMessage());
        }
    }

    private boolean isPointWithRelations(JsonNode feature) {
        JsonNode geometry = feature.get("geometry");
        JsonNode properties = feature.get("properties");
        JsonNode relations = properties.get("@relations");

        return geometry != null
                && "Point".equals(geometry.get("type").asText())
                && relations != null
                && relations.isArray()
                && !relations.isEmpty();
    }

    private void processCenterPoint(JsonNode feature) throws org.locationtech.jts.io.ParseException {
        JsonNode properties = feature.get("properties");
        JsonNode relations = properties.get("@relations");
        JsonNode geometryNode = feature.get("geometry");

        // Lấy thông tin từ relation đầu tiên
        JsonNode firstRelation = relations.get(0);
        JsonNode relTags = firstRelation.get("reltags");

        if (relTags != null) {
            String name = relTags.get("name").asText();
            // Tìm đơn vị hành chính tương ứng
            DonViHanhChinh existingEntity = repository.findByTen(name);

            if (existingEntity != null && geometryNode != null) {
                // Chuyển đổi và set điểm trung tâm
                Geometry geometry = geometryService.convertToGeometry(geometryNode.toString());
                if (geometry instanceof Point) {
                    existingEntity.setCenterPoint((Point) geometry);
                    repository.save(existingEntity);
                    logger.info("Updated center point for: {}", name);
                }
            } else {
                logger.warn("Cannot find matching administrative unit for center point: {}", name);
            }
        }
    }

    private DonViHanhChinh createEntityFromFeature(JsonNode feature)
            throws ParseException, org.locationtech.jts.io.ParseException {
        JsonNode properties = feature.get("properties");
        JsonNode geometryNode = feature.get("geometry");

        DonViHanhChinh entity = new DonViHanhChinh();

        // Lấy và xử lý adminLevel
        String adminLevelStr = getJsonNodeText(properties, "admin_level");
        entity.setCapHanhChinh(adminLevelStr);

        if (adminLevelStr != null) {
            try {
                int adminLevelValue = Integer.parseInt(adminLevelStr);
                AdminLevel adminLevel = AdminLevel.fromLevel(adminLevelValue);
                entity.setAdminLevel(adminLevel);
            } catch (NumberFormatException e) {
                logger.warn("Invalid admin_level value: {}", adminLevelStr);
            }
        }

        entity.setTen(getJsonNodeText(properties, "name"));
        entity.setTenTiengAnh(getJsonNodeText(properties, "name:en"));

        // Tìm đơn vị cha từ relations
        JsonNode relations = properties.get("@relations");
        if (relations != null && relations.isArray() && !relations.isEmpty()) {
            // Lấy relation đầu tiên
            JsonNode firstRelation = relations.get(0);
            if (firstRelation.has("reltags")) {
                JsonNode relTags = firstRelation.get("reltags");
                String parentName = getJsonNodeText(relTags, "name");
                if (parentName != null) {
                    DonViHanhChinh parentEntity = repository.findByTen(parentName);
                    if (parentEntity == null) {
                        // Thử tìm bằng tên tiếng Anh
                        String parentNameEn = getJsonNodeText(relTags, "name:en");
                        if (parentNameEn != null) {
                            parentEntity = repository.findByTenTiengAnh(parentNameEn);
                        }
                    }
                    entity.setDonViCha(parentEntity);
                }
            }
        } else {
            // Fallback: Nếu không có relations, thử dùng is_in:city
            String tenDonViCha = getJsonNodeText(properties, "is_in:city");
            if (tenDonViCha != null) {
                DonViHanhChinh parentEntity = repository.findByTenTiengAnh(tenDonViCha);
                if (parentEntity == null) {
                    parentEntity = repository.findByTen(tenDonViCha);
                }
                entity.setDonViCha(parentEntity);
            }
        }

        if (geometryNode != null) {
            String geometryType = geometryNode.get("type").asText();
            Geometry geometry = geometryService.convertToGeometry(geometryNode.toString());
            if (geometryType.equals("Polygon") || geometryType.equals("MultiPolygon")) {
                entity.setRanhGioi(geometry);
            } else if (geometryType.equals("Point")) {
                if (geometry instanceof Point) {
                    entity.setCenterPoint((Point) geometry);
                }
            }
        }

        return entity;
    }

    private Double getJsonNodeDouble(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null ? fieldNode.asDouble() : null;
    }

    private boolean isDuplicate(DonViHanhChinh entity) {
        return repository.existsByTenAndAdminLevelAndRanhGioi(
                entity.getTen(),
                entity.getAdminLevel(),
                entity.getRanhGioi());
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