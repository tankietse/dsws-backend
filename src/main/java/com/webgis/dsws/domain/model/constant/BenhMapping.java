// package com.webgis.dsws.domain.model.constant;

// import java.util.*;

// public class BenhMapping {
//     private static final Map<String, String> NAME_MAPPINGS = new HashMap<>();

//     static {
//         // Map tên tiếng Anh sang tiếng Việt
//         NAME_MAPPINGS.put("LMLM", "Lở mồm long móng");
//         NAME_MAPPINGS.put("CGC", "Cúm gia cầm chủng độc lực cao");
//         NAME_MAPPINGS.put("Newcastle", "Niu cát xơn");
//         NAME_MAPPINGS.put("PRRS", "Rối loạn sinh sản và hô hấp ở lợn");
//         NAME_MAPPINGS.put("ASF", "Dịch tả lợn");
//         NAME_MAPPINGS.put("CSF", "Dịch tả lợn");
//         NAME_MAPPINGS.put("Dịch tả heo châu Phi", "Dịch tả lợn");
//         NAME_MAPPINGS.put("Dịch tả heo cổ điển", "Dịch tả lợn");

//         // Thêm tên tiếng Việt tự map chính nó
//         Set<String> vietnameseNames = new HashSet<>(NAME_MAPPINGS.values());
//         for (String vietnameseName : vietnameseNames) {
//             NAME_MAPPINGS.put(vietnameseName, vietnameseName);
//         }
//     }

//     public static String getVietnameseName(String name) {
//         if (name == null)
//             return null;
//         return NAME_MAPPINGS.getOrDefault(name, name); // Nếu không có mapping thì trả về tên gốc
//     }
// }