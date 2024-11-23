package com.webgis.dsws.domain.model.constant;

import com.webgis.dsws.domain.model.enums.MucDoBenhEnum;
import java.util.*;

public class BenhRegistry {
    private static final Map<String, DiseaseInfo> REGISTRY = new HashMap<>();

    public static class DiseaseInfo {
        private final String vietnameseName;
        private final Set<String> alternativeNames;
        private final Set<String> affectedAnimals;
        private final Set<MucDoBenhEnum> categories;

        private DiseaseInfo(String vietnameseName, Set<String> alternativeNames,
                Set<String> affectedAnimals, Set<MucDoBenhEnum> categories) {
            this.vietnameseName = vietnameseName;
            this.alternativeNames = alternativeNames;
            this.affectedAnimals = affectedAnimals;
            this.categories = categories;
        }

        public String getVietnameseName() {
            return vietnameseName;
        }

        public Set<String> getAlternativeNames() {
            return alternativeNames;
        }

        public Set<String> getAffectedAnimals() {
            return affectedAnimals;
        }

        public Set<MucDoBenhEnum> getCategories() {
            return categories;
        }
    }

    static {
        // Bệnh thuộc danh mục A
        register("Lở mồm long móng",
                Set.of("LMLM"),
                Set.of("Trâu", "Bò", "Lợn", "Dê", "Cừu"),
                Set.of(MucDoBenhEnum.BANG_A, MucDoBenhEnum.NGUY_HIEM, MucDoBenhEnum.PHONG_BENH_BAT_BUOC));

        register("Cúm gia cầm chủng độc lực cao",
                Set.of("CGC", "HPAI"),
                Set.of("Gia cầm"),
                Set.of(MucDoBenhEnum.BANG_A, MucDoBenhEnum.NGUY_HIEM, MucDoBenhEnum.PHONG_BENH_BAT_BUOC));

        register("Dịch tả lợn",
                Set.of("DTL", "ASF", "CSF"),
                Set.of("Lợn"),
                Set.of(MucDoBenhEnum.BANG_A, MucDoBenhEnum.NGUY_HIEM, MucDoBenhEnum.PHONG_BENH_BAT_BUOC));

        register("Dịch tả trâu bò",
                Set.of("DTTB"),
                Set.of("Trâu", "Bò"),
                Set.of(MucDoBenhEnum.BANG_A, MucDoBenhEnum.NGUY_HIEM));

        register("Lưỡi xanh",
                Set.of("LX"),
                Set.of("Trâu", "Bò", "Dê", "Cừu"),
                Set.of(MucDoBenhEnum.BANG_A, MucDoBenhEnum.NGUY_HIEM));

        register("Niu cát xơn",
                Set.of("Newcastle"),
                Set.of("Gia cầm"),
                Set.of(MucDoBenhEnum.BANG_A, MucDoBenhEnum.NGUY_HIEM, MucDoBenhEnum.PHONG_BENH_BAT_BUOC));

        register("Đậu cừu, Đậu dê",
                Set.of("DDCD"),
                Set.of("Dê", "Cừu"),
                Set.of(MucDoBenhEnum.BANG_A, MucDoBenhEnum.NGUY_HIEM));

        // Bệnh thuộc danh mục B
        register("Nhiệt thán",
                Set.of("NT"),
                Set.of("Trâu", "Bò", "Dê", "Cừu"),
                Set.of(MucDoBenhEnum.BANG_B, MucDoBenhEnum.NGUY_HIEM, MucDoBenhEnum.PHONG_BENH_BAT_BUOC));

        register("Dại",
                Set.of("D"),
                Set.of("Chó", "Mèo", "Các loài thú"),
                Set.of(MucDoBenhEnum.BANG_B, MucDoBenhEnum.NGUY_HIEM, MucDoBenhEnum.PHONG_BENH_BAT_BUOC));

        register("Tụ huyết trùng trâu bò",
                Set.of("THT"),
                Set.of("Trâu", "Bò"),
                Set.of(MucDoBenhEnum.BANG_B, MucDoBenhEnum.NGUY_HIEM, MucDoBenhEnum.PHONG_BENH_BAT_BUOC));

        register("Bò điên",
                Set.of("BD"),
                Set.of("Bò"),
                Set.of(MucDoBenhEnum.BANG_B, MucDoBenhEnum.NGUY_HIEM));

        // Bệnh nguy hiểm
        register("Gum bô rô",
                Set.of("GBR"),
                Set.of("Gia cầm"),
                Set.of(MucDoBenhEnum.NGUY_HIEM));

        register("Lép tô",
                Set.of("Xoắn khuẩn", "LT"),
                Set.of("Lợn", "Chó", "Bò"),
                Set.of(MucDoBenhEnum.NGUY_HIEM));

        register("Tiên mao trùng",
                Set.of("TMT"),
                Set.of("Gia súc", "Gia cầm"),
                Set.of(MucDoBenhEnum.NGUY_HIEM));

        register("Biên trùng",
                Set.of("BT"),
                Set.of("Gia súc"),
                Set.of(MucDoBenhEnum.NGUY_HIEM));

        register("Lê dạng trùng",
                Set.of("LDT"),
                Set.of("Gia súc"),
                Set.of(MucDoBenhEnum.NGUY_HIEM));

        register("Giả dại",
                Set.of("GD"),
                Set.of("Chó", "Mèo"),
                Set.of(MucDoBenhEnum.NGUY_HIEM));

        register("Ung khí thán",
                Set.of("UKT"),
                Set.of("Gia súc"),
                Set.of(MucDoBenhEnum.NGUY_HIEM));

        register("Giun bao",
                Set.of("GB"),
                Set.of("Lợn"),
                Set.of(MucDoBenhEnum.NGUY_HIEM));

        register("Suyễn lợn",
                Set.of("SL"),
                Set.of("Lợn"),
                Set.of(MucDoBenhEnum.NGUY_HIEM));

        register("Rối loạn sinh sản và hô hấp ở lợn",
                Set.of("PRRS"),
                Set.of("Lợn"),
                Set.of(MucDoBenhEnum.NGUY_HIEM));

        register("Dịch tả vịt",
                Set.of("DTV"),
                Set.of("Vịt"),
                Set.of(MucDoBenhEnum.NGUY_HIEM, MucDoBenhEnum.PHONG_BENH_BAT_BUOC));

        register("Viêm gan vịt",
                Set.of("VGV"),
                Set.of("Vịt"),
                Set.of(MucDoBenhEnum.NGUY_HIEM));

        register("Xuất huyết ở thỏ",
                Set.of("XHT"),
                Set.of("Thỏ"),
                Set.of(MucDoBenhEnum.NGUY_HIEM));

        // Cập nhật lại một số bệnh đã đăng ký để thêm PHONG_BENH_BAT_BUOC nếu chưa có
        register("Cúm gia cầm",
                Set.of("CGC"),
                Set.of("Gia cầm"),
                Set.of(MucDoBenhEnum.PHONG_BENH_BAT_BUOC));
    }

    private static void register(String vietnameseName, Set<String> alternativeNames,
            Set<String> affectedAnimals, Set<MucDoBenhEnum> categories) {
        DiseaseInfo info = new DiseaseInfo(vietnameseName, alternativeNames, affectedAnimals, categories);
        REGISTRY.put(vietnameseName.toLowerCase(), info);
        for (String alt : alternativeNames) {
            REGISTRY.put(alt.toLowerCase(), info);
        }
    }

    public static String getVietnameseName(String name) {
        DiseaseInfo info = REGISTRY.get(name.toLowerCase());
        return info != null ? info.getVietnameseName() : name;
    }

    public static boolean requiresAnnouncement(String name) {
        DiseaseInfo info = REGISTRY.get(name.toLowerCase());
        if (info == null)
            return false;
        Set<MucDoBenhEnum> categories = info.getCategories();
        return categories.contains(MucDoBenhEnum.BANG_A) ||
                categories.contains(MucDoBenhEnum.BANG_B);
    }

    public static boolean requiresMandatoryPrevention(String name) {
        DiseaseInfo info = REGISTRY.get(name.toLowerCase());
        if (info == null)
            return false;
        return info.getCategories().contains(MucDoBenhEnum.PHONG_BENH_BAT_BUOC);
    }

    // Add getter method for DiseaseInfo
    public static DiseaseInfo getInfo(String name) {
        return REGISTRY.get(name.toLowerCase());
    }
}