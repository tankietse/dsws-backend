package com.webgis.dsws.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class CaBenhStatisticsDTO {
    private Map<String, Object> generalStats;
    private List<Map<String, Object>> diseaseDistribution;
    private List<Map<String, Object>> caseTrend;
    private List<Map<String, Object>> recentCases;
    private Map<String, Object> statusDistribution;
    private Map<String, Object> severitySummary;
    private List<Map<String, Object>> regionStats;
    private Map<String, Object> monthlyComparison;
    private List<Map<String, Object>> topFarms;
    private Map<String, Object> animalTypeStats;
}
