class MapLayers {
  constructor(map) {
    this.map = map;
    this.currentLayer = null;
    this.boundaryLayer = null;
    this.legendControl = null;

    // Create a custom Canvas renderer with willReadFrequently set to true
    this.heatRenderer = L.canvas({ padding: 0.5, willReadFrequently: true });
  }

  clearCurrentLayer() {
    if (this.currentLayer) {
      this.map.removeLayer(this.currentLayer);
      this.currentLayer = null;
    }
    if (this.legendControl) {
      this.map.removeControl(this.legendControl);
      this.legendControl = null;
    }
  }

  getColorByMucDo(mucDo) {
    console.log("getColorByMucDo called with:", mucDo);
    switch (mucDo) {
      case "CAP_DO_1":
      case "CAP_DO_3":
        return "#ffa64d";
      case "CAP_DO_4":
        return "#ff4d4d";
      default:
        return "#808080";
    }
  }

  loadMarkers() {
    console.log("loadMarkers called");
    return new Promise((resolve, reject) => {
      this.clearCurrentLayer();
      fetch("/api/v1/vung-dich/heatmap", {
        credentials: "include",
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error("Network response was not ok");
          }
          return response.json();
        })
        .then((data) => {
          const markers = L.markerClusterGroup({
            iconCreateFunction: (cluster) => {
              return this.createCustomClusterIcon(cluster);
            },
            showCoverageOnHover: false,
            spiderfyOnMaxZoom: true,
            maxClusterRadius: 40,
            zoomToBoundsOnClick: true,

            // Add custom hover functionality for clusters
            chunkedLoading: true,
            singleMarkerMode: false,

            // Custom popup settings
            spiderfyDistanceMultiplier: 2,

            // Configure popup behavior for clusters
            spiderfyShapePositions: function (count, centerPt) {
              var distanceFromCenter = 35,
                markerDistance = 45,
                lineLength = markerDistance * (count - 1),
                lineStart = centerPt.y - lineLength / 2;

              return [...Array(count)].map((_, index) => {
                return L.point(
                  centerPt.x + distanceFromCenter,
                  lineStart + markerDistance * index
                );
              });
            },
          });

          data.forEach((point) => {
            const marker = L.marker([point.latitude, point.longitude], {
              // ...existing marker options...
            });

            // Create detailed popup content
            const formattedDate = new Date(point.ngayBatDau).toLocaleDateString(
              "vi-VN",
              {
                day: "2-digit",
                month: "2-digit",
                year: "numeric",
                hour: "2-digit",
                minute: "2-digit",
              }
            );

            const status = point.ngayKetThuc ? "Đã kết thúc" : "Đang hoạt động";
            const statusClass = point.ngayKetThuc ? "inactive" : "active";

            const popupContent = `
              <div class="disease-zone-popup">
                <div class="disease-zone-header" style="background-color: ${
                  point.color
                }">
                  <h3>${point.tenVung}</h3>
                  <span class="status-badge ${statusClass}">${status}</span>
                </div>
                <div class="disease-zone-content">
                  <div class="info-row">
                    <span class="label">Loại bệnh:</span>
                    <span class="value">${point.tenBenh}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">Mức độ:</span>
                    <span class="value severity-badge">${point.mucDo.replace(
                      "CAP_DO_",
                      "Cấp độ "
                    )}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">Bắt đầu:</span>
                    <span class="value">${formattedDate}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">Bán kính:</span>
                    <span class="value">${(point.banKinh * 1000).toFixed(
                      0
                    )}m</span>
                  </div>
                  <div class="info-row description">
                    <p>${point.moTa}</p>
                  </div>
                </div>
              </div>
            `;

            marker.bindPopup(popupContent, {
              maxWidth: 300,
              className: "disease-zone-popup-container",
            });
            markers.addLayer(marker);
          });

          this.currentLayer = markers;
          this.currentLayer.addTo(this.map);
          resolve();
        })
        .catch((error) => {
          console.error("Error loading markers:", error);
          if (error.status === 401) {
            window.location.href = "/auth/login";
          }
          reject(error);
        });
    });
  }

  // Add new helper method for custom cluster icon
  createCustomClusterIcon(cluster) {
    const count = cluster.getChildCount();
    const points = cluster.getAllChildMarkers();

    // Get unique disease types and their colors
    const diseases = new Set(points.map((p) => p.options.diseaseData?.tenBenh));
    const colors = new Set(points.map((p) => p.options.diseaseData?.color));

    let className = "disease-cluster-icon";
    if (diseases.size > 1) {
      className += " multi-disease";
    }

    return L.divIcon({
      html: `<div><span>${count}</span></div>`,
      className: className,
      iconSize: L.point(40, 40),
    });
  }

  loadHeatmap() {
    return new Promise((resolve, reject) => {
      this.clearCurrentLayer();
      fetch("/api/v1/vung-dich/heatmap", {
        credentials: "include",
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error("Network response was not ok");
          }
          return response.json();
        })
        .then((data) => {
          const heatData = data.map((d) => [
            d.latitude,
            d.longitude,
            d.intensity * 100, // Scale up intensity for better visualization
          ]);
          this.currentLayer = L.heatLayer(heatData, {
            radius: 25,
            blur: 15,
            maxZoom: 15,
            pane: "points",
            gradient: {
              0.2: "#4dff4d",
              0.4: "#ffff4d",
              0.6: "#ffa64d",
              0.8: "#ff4d4d",
            },
          });
          this.currentLayer.addTo(this.map);
          resolve();
        })
        .catch((error) => {
          console.error("Error loading heatmap:", error);
          if (error.status === 401) {
            window.location.href = "/auth/login";
          }
          reject(error);
        });
    });
  }

  loadClusterSymbols() {
    return new Promise((resolve, reject) => {
      if (this.currentLayer) {
        this.map.removeLayer(this.currentLayer);
      }
      fetch("/api/v1/vung-dich/cluster", {
        credentials: "include",
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error("Network response was not ok");
          }
          return response.json();
        })
        .then((data) => {
          const markers = L.markerClusterGroup();
          data.forEach((d) => {
            // Validate coordinates
            if (
              typeof d.latitude !== "number" ||
              typeof d.longitude !== "number"
            ) {
              console.warn(
                `Invalid coordinates for cluster point: ${JSON.stringify(d)}`
              );
              return; // Skip this point
            }

            const marker = L.marker([d.latitude, d.longitude]);
            markers.addLayer(marker);
          });
          this.currentLayer = markers;
          this.currentLayer.addTo(this.map);
          resolve();
        })
        .catch((error) => {
          console.error("Error loading clusters:", error);
          if (error.status === 401) {
            window.location.href = "/auth/login";
          }
          reject(error);
        });
    });
  }

  loadAdminBoundaries() {
    console.log("loadAdminBoundaries called");
    return new Promise((resolve, reject) => {
      fetch("/api/v1/don-vi-hanh-chinh/cap/6/geojson", {
        credentials: "include",
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error("Network response was not ok");
          }
          return response.json();
        })
        .then((data) => {
          if (!data || !data.features || data.features.length === 0) {
            console.warn("No boundary data available");
            return;
          }
          this.boundaryLayer = L.geoJSON(data, {
            pane: "boundaries",
            style: {
              color: "#003399",
              weight: 1,
              opacity: 0.3,
              fillOpacity: 0.1,
            },
            onEachFeature: function (feature, layer) {
              layer.bindPopup(feature.properties.ten);
            },
          });
          this.boundaryLayer.addTo(this.map);
          resolve();
        })
        .catch((error) => {
          console.error("Error loading boundaries:", error);
          if (error.status === 401) {
            window.location.href = "/auth/login";
          }
          reject(error);
        });
    });
  }

  toggleBoundaries() {
    const boundaryBtn = document.getElementById("boundaryBtn");
    if (!boundaryBtn) {
      console.warn("Boundary button not found in the DOM");
      return;
    }

    if (this.boundaryLayer && this.map.hasLayer(this.boundaryLayer)) {
      this.map.removeLayer(this.boundaryLayer);
      boundaryBtn.textContent = "Hiện ranh giới";
    } else {
      if (this.boundaryLayer) {
        this.boundaryLayer.addTo(this.map);
      } else {
        this.loadAdminBoundaries();
      }
      boundaryBtn.textContent = "Ẩn ranh giới";
    }
  }

  loadRegionCases() {
    return new Promise((resolve, reject) => {
      this.clearCurrentLayer();

      const statsPanel = document.getElementById("statsPanel");
      const statsPanelContent = document.getElementById("statsPanelContent");

      fetch("/api/v1/ca-benh/geojson-vung?capHanhChinh=6", {
        credentials: "include",
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error("Network response was not ok");
          }
          return response.json();
        })
        .then((data) => {
          this.currentLayer = L.geoJSON(data, {
            pane: "regions",
            renderer: L.canvas(), // Use canvas renderer
            style: (feature) => ({
              fillColor: this.getColorByCaseCount(
                feature.properties.totalCases
              ),
              weight: 2,
              opacity: 1,
              color: "white",
              dashArray: "3",
              fillOpacity: 0.7,
            }),
            onEachFeature: (feature, layer) => {
              // Create detailed popup content
              const diseases = feature.properties.diseaseTypes;
              const totalCases = feature.properties.totalCases;

              let popupContent = `
                        <div class="stats-panel">
                          <div class="stats-title">
                            <i class="fas fa-chart-pie"></i>
                            ${feature.properties.ten}
                          </div>
                          <div class="stats-content">
                            <div class="stats-row">
                              <span class="stats-label">Tổng số ca nhiễm:</span>
                              <span class="stats-value">${totalCases}</span>
                            </div>
                            <table class="stats-table">
                              <thead>
                                <tr>
                                  <th>Loại bệnh</th>
                                  <th>Số lượng</th>
                                  <th>Tỷ lệ</th>
                                </tr>
                              </thead>
                              <tbody>
                                ${diseases
                                  .map((disease) => {
                                    const cases =
                                      feature.properties.diseaseCases[
                                        disease
                                      ] || 0;
                                    const percentage = (
                                      (cases / totalCases) *
                                      100
                                    ).toFixed(1);
                                    return `
                                      <tr>
                                        <td>${disease}</td>
                                        <td>${cases}</td>
                                        <td>${percentage}%</td>
                                      </tr>
                                    `;
                                  })
                                  .join("")}
                              </tbody>
                            </table>
                          </div>
                        </div>
                    `;

              layer.bindPopup(popupContent);

              // Add hover effect
              layer.on({
                mouseover: function (e) {
                  layer.setStyle({
                    weight: 3,
                    color: "LightSlateGray",
                    fillOpacity: 0.9,
                  });
                  layer.bringToFront();
                },
                mouseout: function (e) {
                  layer.setStyle({
                    weight: 2,
                    color: "white",
                    fillOpacity: 0.7,
                  });
                  layer.bringToBack();
                },
              });
            },
            coordsToLatLng: (coords) => {
              // Swap longitude and latitude for Leaflet
              return new L.LatLng(coords[1], coords[0]);
            },
          }).addTo(this.map);

          // Add legend
          this.legendControl = L.control({ position: "bottomleft" });

          this.legendControl.onAdd = (map) => {
            var div = L.DomUtil.create("div", "info-legend");

            const grades = [0, 10000, 20000, 50000, 100000];
            const total = grades.reduce((acc, curr) => acc + curr, 0);

            div.innerHTML = `
            <div class="legend-header">
                <svg width="24" height="24" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 3.055A9.001 9.001 0 1020.945 13H11V3.055z"></path>
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.488 9H15V3.512A9.025 9.025 0 0120.488 9z"></path>
                </svg>
                <div class="legend-title">Phân bố ca bệnh</div>
            </div>
            <div class="legend-items">
                ${grades
                  .map(
                    (grade, i) => `
                    <div class="legend-item">
                        <div class="legend-color" style="background: ${this.getColorByCaseCount(
                          grade + 1
                        )}"></div>
                        <span class="legend-label">${grade}${
                      grades[i + 1] ? " - " + grades[i + 1] : "+"
                    }</span>
                        <span class="legend-value">${(
                          (grade / total) *
                          100
                        ).toFixed(1)}%</span>
                    </div>
                `
                  )
                  .join("")}
            </div>
          `;

            return div;
          };

          this.legendControl.addTo(this.map);
          resolve();
        })
        .catch((error) => {
          console.error("Error loading region cases:", error);
          if (error.status === 401) {
            window.location.href = "/auth/login";
          }
          reject(error);
        });
    });
  }

  getColorByCaseCount(caseCount) {
    console.log("getColorByCaseCount called with:", caseCount);
    if (caseCount > 100000) return "#FF0000";
    if (caseCount > 50000) return "#FF4500";
    if (caseCount > 20000) return "#FFA500";
    if (caseCount > 10000) return "#FFFF00";
    if (caseCount > 0) return "#90EE90";
    return "transparent";
  }

  loadFarms() {
    return new Promise((resolve, reject) => {
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
              <div class="farm-form-popup">
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
          if (error.status === 401) {
            window.location.href = "/auth/login";
          }
          reject(error);
        });
    });
  }

  // Add method to load disease outbreaks with special symbols
  loadDiseaseOutbreaks() {
    fetch("/api/v1/ca-benh/geojson", {
      credentials: "include",
    })
      .then((response) => response.json())
      .then((data) => {
        const outbreakMarkers = L.geoJSON(data, {
          pointToLayer: (feature, latlng) => {
            return L.marker(latlng, {
              icon: this.getDiseaseIcon(feature.properties),
            });
          },
          onEachFeature: (feature, layer) => {
            layer.on("click", () => {
              this.showDiseaseDetails(feature.properties);
            });
          },
        });
        outbreakMarkers.addTo(this.map);
      })
      .catch((error) => {
        console.error("Error loading disease outbreaks:", error);
      });
  }

  getDiseaseIcon(properties) {
    const severity = properties.mucDo || "default";
    const iconSize = [25, 25];

    return L.divIcon({
      className: `disease-icon severity-${severity.toLowerCase()}`,
      html: `<i class="fas fa-exclamation-triangle"></i>`,
      iconSize: iconSize,
    });
  }

  // Get icon for farms based on type
  getFarmIcon(properties) {
    const farmType = properties.loaiHinh || "default";
    const iconSize = [25, 25];

    return L.divIcon({
      className: `farm-icon type-${farmType.toLowerCase()}`,
      html: `<i class="fas fa-home"></i>`,
      iconSize: iconSize,
    });
  }

  // Show detailed disease information in a popup
  showDiseaseDetails(properties) {
    const popup = L.popup({
      maxWidth: 300,
      className: "disease-details-popup",
    });

    const content = `
      <div class="disease-details">
        <h3>${properties.tenBenh}</h3>
        <p><strong>Mức độ:</strong> ${properties.mucDo}</p>
        <p><strong>Ngày phát hiện:</strong> ${new Date(
          properties.ngayPhatHien
        ).toLocaleDateString()}</p>
        <p><strong>Số ca nhiễm:</strong> ${properties.soCaNhiem}</p>
        <p>${properties.moTa || ""}</p>
      </div>
    `;

    popup.setContent(content);
    return popup;
  }

  // Show detailed farm information in a popup
  showFarmDetails(properties) {
    const popup = L.popup({
      maxWidth: 300,
      className: "farm-details-popup",
    });

    const content = `
      <div class="farm-details">
        <h3>${properties.tenTrangTrai}</h3>
        <p><strong>Chủ sở hữu:</strong> ${properties.chuSoHuu}</p>
        <p><strong>Loại hình:</strong> ${properties.loaiHinh}</p>
        <p><strong>Quy mô:</strong> ${properties.quyMo}</p>
        <p><strong>Địa chỉ:</strong> ${properties.diaChi}</p>
      </div>
    `;

    popup.setContent(content);
    return popup;
  }

  // Add method to load risk zones and set up alerts
  loadRiskZones() {
    fetch("/api/v1/vung-dich/heatmap", {
      credentials: "include",
    })
      .then((response) => response.json())
      .then((data) => {
        const riskZones = L.geoJSON(data, {
          style: this.getRiskZoneStyle,
        });
        riskZones.addTo(this.map);
        this.setupProximityAlerts(riskZones);
      })
      .catch((error) => {
        console.error("Error loading risk zones:", error);
      });
  }

  // Add method to set up proximity alerts
  setupProximityAlerts(riskZones) {
    this.map.on("locationfound", (e) => {
      const userLocation = e.latlng;
      riskZones.eachLayer((layer) => {
        const withinZone = layer.getBounds().contains(userLocation);
        if (withinZone) {
          alert("Bạn đang ở gần khu vực nguy hiểm!");
        }
      });
    });
  }

  // Add method to get real-time updates
  setupRealtimeUpdates() {
    setInterval(() => {
      this.loadDiseaseOutbreaks();
      this.loadFarms();
    }, 60000); // Update every 60 seconds
  }
}

function updateProgressBar(processed, total, elapsed, layersArray) {
  if (elapsed > 1000) {
    // if it takes more than a second to load, display the progress bar
    const progress = Math.round((processed / total) * 100);
    console.log(`Loading: ${progress}%`);
  }
}
