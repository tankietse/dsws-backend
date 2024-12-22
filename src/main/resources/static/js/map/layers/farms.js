export default function loadFarms() {
  return new Promise((resolve, reject) => {
    const mapLoading = document.getElementById("map-loading");

    if (mapLoading) {
      mapLoading.style.display = "flex";
    }

    const updateProgressBar = (processed, total, elapsed, layersArray) => {
      if (processed === total) {
        console.log("All farm clusters loaded");
        // Hide loading when clustering is complete
        if (mapLoading) {
          mapLoading.style.display = "none";
        }
      }
    };

    if (this.currentLayer) {
      this.map.removeLayer(this.currentLayer);
    }

    fetch("/api/v1/trang-trai/geojson", {
      credentials: "include",
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Network response was not ok");
        }
        return response.json();
      })
      .then((data) => {
        const farmClusters = L.markerClusterGroup({
          maxClusterRadius: 80, // Increase cluster radius for better performance
          disableClusteringAtZoom: 14,
          chunkedLoading: true, // Enable chunked loading for large datasets
          chunkProgress: updateProgressBar, // Optional: update progress bar during loading
        });
        data.features.forEach((feature) => {
          const marker = L.marker([
            feature.geometry.coordinates[1],
            feature.geometry.coordinates[0],
          ]);

          // Format list thông tin về vật nuôi
          const vatNuoiList = Object.entries(feature.properties.vatNuoi)
            .map(
              ([ten, soLuong]) => `
                <div class="stats-row">
                  <span class="stats-label">${ten}:</span>
                  <span class="stats-value">${soLuong} con</span>
                </div>
              `
            )
            .join("");

          // Format lại date cho thông tin trang trại
          const ngayCapNhat = new Date(feature.properties.ngayCapNhat);
          const formattedDate = new Intl.DateTimeFormat("vi-VN", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
          }).format(ngayCapNhat);

          marker.bindPopup(
            `
              <div class="farm-form-popup z-1000">
                <div class="farm-form-header">
                  <h3 class="text-lg font-semibold">${
                    feature.properties.tenTrangTrai || "Trang trại không tên"
                  }</h3>
                  <p class="text-sm opacity-90">${
                    feature.properties.donViHanhChinh || "Chưa cập nhật"
                  }</p>
                </div>
                
                <div class="farm-form-content">
                  <div class="farm-form-group">
                    <div class="farm-form-row">
                      <label class="farm-form-label">Chủ sở hữu</label>
                      <div class="farm-form-value">${
                        feature.properties.tenChu || "Chưa cập nhật"
                      }</div>
                    </div>
                    <div class="farm-form-row">
                      <label class="farm-form-label">Liên hệ</label>
                      <div class="farm-form-value">${
                        feature.properties.soDienThoai || "Chưa cập nhật"
                      }</div>
                    </div>
                  </div>
          
                  <div class="farm-form-group">
                    <div class="farm-form-row">
                      <label class="farm-form-label">Thông tin đàn</label>
                      <div class="farm-animal-stats">
                        ${Object.entries(feature.properties.vatNuoi)
                          .map(
                            ([ten, soLuong]) => `
                              <div class="farm-animal-item">
                                <div class="farm-animal-icon">
                                  <img src="/img/icons/${ten}.png" alt="${ten}" class="farm-animal-icon-img">
                                </div>
                                <div class="farm-animal-info">
                                  <div class="farm-animal-name">${ten}</div>
                                  <div class="farm-animal-count">${soLuong.toLocaleString(
                                    "vi-VN"
                                  )} con</div>
                                </div>
                              </div>
                            `
                          )
                          .join("")}
                        <div class="farm-total-count">
                          <div class="farm-total-icon">
                            <i class="fas fa-calculator"></i>
                          </div>
                          <div class="farm-total-info">
                            <div class="farm-total-label">Tổng đàn</div>
                            <div class="farm-total-value">${
                              feature.properties.tongDan?.toLocaleString(
                                "vi-VN"
                              ) || 0
                            } con</div>
                          </div>
                        </div>
                      </div>
                    </div>
          
                    <div class="farm-form-group">
                      <div class="farm-form-row">
                        <label class="farm-form-label">Địa chỉ</label>
                        <div class="farm-form-value">${
                          feature.properties.diaChiDayDu || "Chưa cập nhật"
                        }</div>
                      </div>
                      <div class="farm-form-row">
                        <label class="farm-form-label">Phương thức chăn nuôi</label>
                        <div class="farm-form-value">${
                          feature.properties.phuongThucChanNuoi ||
                          "Chưa cập nhật"
                        }</div>
                      </div>
                    </div>
          
                    <div class="farm-form-footer">
                      <button class="view-farm-btn" onclick="window.location.href='/trang-trai/${
                        feature.properties.id
                      }'">
                        <i class="fas fa-external-link-alt"></i>
                        Xem chi tiết
                      </button>
                    </div>
                  </div>
                </div>
              `,
            {
              maxWidth: 400,
              className: "farm-popup-container",
            }
          );
          farmClusters.addLayer(marker);
        });
        this.currentLayer = farmClusters;
        this.currentLayer.addTo(this.map);
        resolve();
      })
      .catch((error) => {
        console.error("Error loading farms:", error);
        // Hide loading on error
        if (mapLoading) {
          mapLoading.style.display = "none";
        }
        if (error.status === 401) {
          window.location.href = "/auth/login";
        }
        reject(error);
      });
  });
}
