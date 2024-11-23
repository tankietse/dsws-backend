// package com.webgis.dsws.domain.model.constant;

// import com.webgis.dsws.domain.model.enums.MucDoBenhEnum;
// import java.util.*;

// public class BenhDanhMuc {
// private static final Map<String, Set<MucDoBenhEnum>> BENH_CATEGORIES = new
// HashMap<>();

// static {
// // Bệnh bảng A - phải công bố dịch
// Set<String> bangA = new HashSet<>(Arrays.asList(
// "Lở mồm long móng",
// "Cúm gia cầm chủng độc lực cao",
// "Dịch tả lợn",
// "Dịch tả trâu bò",
// "Lưỡi xanh",
// "Niu cát xơn",
// "Đậu cừu", "Đậu dê"));

// // Bệnh bảng B - phải công bố dịch
// Set<String> bangB = new HashSet<>(Arrays.asList(
// "Nhiệt thán",
// "Dại",
// "Tụ huyết trùng trâu bò",
// "Bò điên"));

// // Bệnh nguy hiểm
// Set<String> nguyHiem = new HashSet<>(Arrays.asList(
// "Gum bô rô", "Lép tô", "Tiên mao trùng", "Biên trùng",
// "Lê dạng trùng", "Giả dại", "Ung khí thán", "Giun bao",
// "Suyễn lợn", "Rối loạn sinh sản và hô hấp ở lợn",
// "Viêm gan vịt", "Xuất huyết ở thỏ"));
// nguyHiem.addAll(bangA); // Thêm các bệnh bảng A vào danh sách nguy hiểm
// nguyHiem.addAll(bangB); // Thêm các bệnh bảng B vào danh sách nguy hiểm

// // Bệnh phải phòng bắt buộc
// Set<String> phongBatBuoc = new HashSet<>(Arrays.asList(
// "Cúm gia cầm", "Dịch tả vịt"));

// // Map tên bệnh với các danh mục
// for (String benh : bangA) {
// BENH_CATEGORIES.computeIfAbsent(benh, k -> new HashSet<>())
// .add(MucDoBenhEnum.BANG_A);
// }
// for (String benh : bangB) {
// BENH_CATEGORIES.computeIfAbsent(benh, k -> new HashSet<>())
// .add(MucDoBenhEnum.BANG_B);
// }
// for (String benh : nguyHiem) {
// BENH_CATEGORIES.computeIfAbsent(benh, k -> new HashSet<>())
// .add(MucDoBenhEnum.NGUY_HIEM);
// }
// for (String benh : phongBatBuoc) {
// BENH_CATEGORIES.computeIfAbsent(benh, k -> new HashSet<>())
// .add(MucDoBenhEnum.PHONG_BENH_BAT_BUOC);
// }
// }

// public static Set<MucDoBenhEnum> getCategories(String tenBenh) {
// // Chuyển đổi tên bệnh sang tiếng Việt trước khi tìm danh mục
// String vietnameseName = BenhMapping.getVietnameseName(tenBenh);
// Set<MucDoBenhEnum> categories = BENH_CATEGORIES.get(vietnameseName);

// // Nếu bệnh không thuộc danh mục nào thì xếp vào bệnh thông thường
// if (categories == null || categories.isEmpty()) {
// categories = new HashSet<>();
// categories.add(MucDoBenhEnum.THONG_THUONG);
// }

// return categories;
// }
// }