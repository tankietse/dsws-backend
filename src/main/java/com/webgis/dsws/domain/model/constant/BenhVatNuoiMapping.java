// package com.webgis.dsws.domain.model.constant;

// import com.webgis.dsws.domain.model.LoaiVatNuoi;
// import java.util.*;

// public class BenhVatNuoiMapping {
//     private static final Map<String, Set<String>> BENH_VAT_NUOI = new HashMap<>();

//     static {
//         // Bệnh gia cầm
//         addMapping("Cúm gia cầm", "Gia cầm");
//         addMapping("Gum bô rô", "Gia cầm");
//         addMapping("Dịch tả vịt", "Vịt");
        
//         // Bệnh lợn
//         addMapping("Dịch tả lợn", "Lợn");
//         addMapping("Suyễn lợn", "Lợn");
//         addMapping("Rối loạn sinh sản và hô hấp ở lợn", "Lợn");

//         // Bệnh trâu bò
//         addMapping("Lở mồm long móng", "Trâu", "Bò", "Lợn", "Dê", "Cừu");
//         addMapping("Tụ huyết trùng trâu bò", "Trâu", "Bò");
//         addMapping("Bò điên", "Bò");

//         // Bệnh dê cừu
//         addMapping("Đậu cừu", "Cừu");
//         addMapping("Đậu dê", "Dê");

//         // Bệnh thỏ
//         addMapping("Xuất huyết ở thỏ", "Thỏ");

//         // Bệnh chung nhiều loại
//         addMapping("Lép tô", "Trâu", "Bò", "Lợn");
//         addMapping("Dại", "Trâu", "Bò", "Lợn", "Dê", "Cừu");
//     }

//     private static void addMapping(String benh, String... vatNuoi) {
//         BENH_VAT_NUOI.put(benh, new HashSet<>(Arrays.asList(vatNuoi)));
//     }

//     public static Set<String> getLoaiVatNuoi(String tenBenh) {
//         String vietnameseName = BenhMapping.getVietnameseName(tenBenh);
//         Set<String> vatNuoi = BENH_VAT_NUOI.get(vietnameseName);
//         return vatNuoi != null ? vatNuoi : new HashSet<>();
//     }
// }