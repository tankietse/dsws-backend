package com.webgis.dsws.common;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class StringUtils {
    public static String normalize(String input) {
        if (input == null) return null;
        
        // Chuyển về chữ thường và bỏ dấu
        String normalized = Normalizer.normalize(input.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        
        // Thay thế các ký tự không phải chữ cái hoặc số bằng khoảng trắng
        normalized = normalized.replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        
        return normalized;
    }

    public static Set<String> splitAndClean(String input) {
        if (input == null || input.trim().isEmpty()) {
            return Set.of();
        }
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}