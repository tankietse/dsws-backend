class MapLayers {
  constructor(map) {
    this.map = map;
    this.currentLayer = null;
    this.boundaryLayer = null;
    this.legendControl = null;
    this.map.createPane("boundaryPane");
    this.map.getPane("boundaryPane").style.zIndex = 250;

    // Create a custom Canvas renderer with willReadFrequently set to true
    this.heatRenderer = L.canvas({ padding: 0.5, willReadFrequently: true });
  }

  getColorByMucDo(mucDo) {
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
    if (this.currentLayer) {
      this.map.removeLayer(this.currentLayer);
    }
    if (this.legendControl) {
      this.map.removeControl(this.legendControl);
    }
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
        const markers = L.layerGroup();
        data.forEach((point) => {
          const color = this.getColorByMucDo(point.mucDo);
          const marker = L.circleMarker([point.latitude, point.longitude], {
            radius: 8 + point.intensity * 8,
            fillColor: color,
            color: "#000",
            weight: 1,
            opacity: 1,
            fillOpacity: 0.7,
          });

          const popupContent = `
                  <div class="stats-panel">
                    <div class="stats-title">
                      <i class="fas fa-map-marker-alt"></i>
                      ${point.tenVung}
                    </div>
                    <div class="stats-content">
                      <div class="stats-row">
                        <span class="stats-label">Bệnh:</span>
                        <span class="stats-value">${point.tenBenh}</span>
                      </div>
                      <div class="stats-row">
                        <span class="stats-label">Mức độ:</span>
                        <span class="stats-badge severity-${point.mucDo.slice(
                          -1
                        )}">${point.mucDo}</span>
                      </div>
                      <div class="stats-row">
                        <span class="stats-label">Ngày bắt đầu:</span>
                        <span class="stats-value">${new Date(
                          point.ngayBatDau
                        ).toLocaleDateString()}</span>
                      </div>
                      <div class="stats-row">
                        <span class="stats-label">Mô tả:</span>
                        <span class="stats-value">${point.moTa}</span>
                      </div>
                    </div>
                  </div>
                `;
          marker.bindPopup(popupContent);
          markers.addLayer(marker);
        });
        this.currentLayer = markers;
        this.currentLayer.addTo(this.map);

        // Create new legend
        this.legendControl = L.control({ position: "bottomright" });
        this.legendControl.onAdd = function (map) {
          const div = L.DomUtil.create("div", "stats-panel legend");
          const grades = ["CAP_DO_1", "CAP_DO_2", "CAP_DO_3", "CAP_DO_4"];
          const labels = ["Cấp độ 1", "Cấp độ 2", "Cấp độ 3", "Cấp độ 4"];

          div.innerHTML = "<h4>Mức độ dịch bệnh</h4>";
          for (let i = 0; i < grades.length; i++) {
            div.innerHTML +=
              '<i style="background:' +
              this.getColorByMucDo(grades[i]) +
              '"></i> ' +
              labels[i] +
              "<br>";
          }
          return div;
        }.bind(this);
        this.legendControl.addTo(this.map);
      })
      .catch((error) => {
        console.error("Error loading markers:", error);
        if (error.status === 401) {
          // Redirect to login if unauthorized
          window.location.href = "/auth/login";
        }
      });
  }

  loadHeatmap() {
    if (this.currentLayer) {
      this.map.removeLayer(this.currentLayer);
    }
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
          gradient: {
            0.2: "#4dff4d",
            0.4: "#ffff4d",
            0.6: "#ffa64d",
            0.8: "#ff4d4d",
          },
        });
        this.currentLayer.addTo(this.map);
      })
      .catch((error) => {
        console.error("Error loading heatmap:", error);
        if (error.status === 401) {
          window.location.href = "/auth/login";
        }
      });
  }

  loadClusterSymbols() {
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
      })
      .catch((error) => {
        console.error("Error loading clusters:", error);
        if (error.status === 401) {
          window.location.href = "/auth/login";
        }
      });
  }

  loadAdminBoundaries() {
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
          style: {
            color: "#003399",
            weight: 1,
            opacity: 0.3,
            fillOpacity: 0.1,
          },
          onEachFeature: function (feature, layer) {
            layer.bindPopup(feature.properties.ten);
          },
          pane: "boundaryPane", // Assign to the custom pane
        });
        this.boundaryLayer.addTo(this.map);
      })
      .catch((error) => {
        console.error("Error loading boundaries:", error);
        if (error.status === 401) {
          window.location.href = "/auth/login";
        }
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
    if (this.currentLayer) {
      this.map.removeLayer(this.currentLayer);
    }
    if (this.legendControl) {
      this.map.removeControl(this.legendControl);
    }

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
          style: (feature) => ({
            fillColor: this.getColorByCaseCount(feature.properties.totalCases),
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
      })
      .catch((error) => {
        console.error("Error loading region cases:", error);
        if (error.status === 401) {
          window.location.href = "/auth/login";
        }
      });
  }

  getColorByCaseCount(caseCount) {
    if (caseCount > 100000) return "#FF0000";
    if (caseCount > 50000) return "#FF4500";
    if (caseCount > 20000) return "#FFA500";
    if (caseCount > 10000) return "#FFFF00";
    if (caseCount > 0) return "#90EE90";
    return "transparent";
  }

  loadFarms() {
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
          disableClusteringAtZoom: 14, // Disable clustering at higher zoom levels
          chunkedLoading: true, // Enable chunked loading for large datasets
          chunkProgress: updateProgressBar, // Optional: update progress bar during loading
        });
        data.features.forEach((feature) => {
          const marker = L.marker([
            feature.geometry.coordinates[1],
            feature.geometry.coordinates[0],
          ]);
          marker.bindPopup(`
            <div class="stats-panel">
              <div class="stats-title">
                <i class="fas fa-tractor"></i>
                ${feature.properties.tenTrangTrai}
              </div>
              <div class="stats-content">
                <div class="stats-row">
                  <span class="stats-label">Loại vật nuôi:</span>
                  <span class="stats-value">${feature.properties.loaiVatNuoi}</span>
                </div>
                <div class="stats-row">
                  <span class="stats-label">Số lượng:</span>
                  <span class="stats-value">${feature.properties.soLuong}</span>
                </div>
              </div>
            </div>
          `);
          farmClusters.addLayer(marker);
        });
        this.currentLayer = farmClusters;
        this.currentLayer.addTo(this.map);
      })
      .catch((error) => {
        console.error("Error loading farms:", error);
        if (error.status === 401) {
          window.location.href = "/auth/login";
        }
      });
  }
}

function updateProgressBar(processed, total, elapsed, layersArray) {
  if (elapsed > 1000) {
    // if it takes more than a second to load, display the progress bar
    const progress = Math.round((processed / total) * 100);
    console.log(`Loading: ${progress}%`);
  }
}
