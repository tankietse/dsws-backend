const MAP_CONFIG = {
  center: [10.762622, 106.660172], // Tọa độ TP.HCM
  zoom: 12, // Điều chỉnh zoom level phù hợp hơn
  minZoom: 5,
  maxZoom: 18,
  accessToken:
    "AAPTxy8BH1VEsoebNVZXo8HurME8mhreBXGaRJgvnaN5cC43A7NYz5KT8CgGQ9vATF_rgvKUTryygEQpoMPspFmHjniacN1sL5amsa1xwzmFRMzugLvaz3d4mcveQ_cfwFT0zfr4tDV_0hfGc4_hCzy8tHKobl72caHgO1kp_ElXJsq1dPiqx39QaGyJguzoMpHsICSdSaKtjdaXXKkKPxwCqZ6aIICRSHSPjHj-DFPzOFWm3EQKm4uiOCuM1enCEx5bAT1_YdAd4C5N", // Đảm bảo API key là hợp lệ
  basemapEnum: "arcgis/light-gray", // Cập nhật enum mặc định
};

const SEVERITY_COLORS = {
  CAP_DO_1: "#4dff4d",
  CAP_DO_2: "#ffff4d",
  CAP_DO_3: "#ffa64d",
  CAP_DO_4: "#ff4d4d",
  default: "#808080",
};

const CASE_COUNT_COLORS = {
  100000: "#FF0000",
  50000: "#FF4500",
  20000: "#FFA500",
  10000: "#FFFF00",
  0: "#90EE90",
  default: "transparent",
};
