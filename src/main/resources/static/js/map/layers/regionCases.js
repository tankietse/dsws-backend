export default function loadRegionCases(level) {
  return new Promise((resolve, reject) => {
    try {
      const mapLoading = document.getElementById("map-loading");
      if (mapLoading) {
        mapLoading.style.display = "flex";
      }

      this.clearCurrentLayer();

      // Get the apply button reference
      const applyBtn = document.getElementById("applyFilters");
      if (applyBtn) {
        applyBtn.disabled = true;
        applyBtn.innerHTML =
          '<i class="fas fa-spinner fa-spin"></i> Đang tải...';
      }

      // Get filter values and format dates properly
      const fromDateEl = document.getElementById("fromDate");
      const toDateEl = document.getElementById("toDate");

      const filters = {
        capHanhChinh: level || document.getElementById("regionLevel")?.value,
        benhId: document.getElementById("tenBenh")?.value,
        mucDoBenh: document.getElementById("mucDoBenh")?.value,
        loaiVatNuoiId: document.getElementById("loaiVatNuoi")?.value,
        fromDate: fromDateEl?.value
          ? new Date(fromDateEl.value).toISOString().split("T")[0]
          : null,
        toDate: toDateEl?.value
          ? new Date(toDateEl.value).toISOString().split("T")[0]
          : null,
      };

      // Build query string - only include non-null values
      const queryParams = new URLSearchParams();
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== null && value !== "") {
          queryParams.append(key, value);
        }
      });

      // Add error handling for fetch with specific error messages
      fetch(`/api/v1/ca-benh/geojson-vung?${queryParams.toString()}`, {
        credentials: "include",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      })
        .then(async (response) => {
          if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Network response was not ok");
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
                                    feature.properties.diseaseCases[disease] ||
                                    0;
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

              // Replace bindPopup with bindTooltip
              layer.bindTooltip(popupContent, {
                sticky: true,
                direction: "auto",
                className: "stats-panel",
                opacity: 1,
              });

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
                click: function (e) {
                  // Add any click behavior you want here
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
        })
        .finally(() => {
          // Reset button state
          if (applyBtn) {
            applyBtn.disabled = false;
            applyBtn.innerHTML = '<i class="fas fa-filter"></i> Áp dụng bộ lọc';
          }
        });
    } catch (error) {
      console.error("Error in loadRegionCases:", error);
      reject(error);
    }
  });
}

// Add function to initialize filters
export function initializeRegionFilters() {
  return Promise.all([
    // Load disease options with IDs
    fetch("/api/v1/benh", { credentials: "include" })
      .then((response) => {
        if (!response.ok) throw new Error("Failed to load diseases");
        return response.json();
      })
      .then((diseases) => {
        const select = document.getElementById("tenBenh");
        if (select) {
          select.innerHTML = '<option value="">Tất cả bệnh</option>';
          diseases.forEach((disease) => {
            const option = document.createElement("option");
            option.value = disease.id;
            option.textContent = disease.tenBenh;
            select.appendChild(option);
          });
        }
      }),

    // Load animal type options with IDs
    fetch("/api/v1/loai-vat-nuoi")
      .then((response) => {
        if (!response.ok) throw new Error("Failed to load animal types");
        return response.json();
      })
      .then((animals) => {
        const select = document.getElementById("loaiVatNuoi");
        if (select) {
          select.innerHTML = '<option value="">Tất cả vật nuôi</option>';
          animals.forEach((animal) => {
            const option = document.createElement("option");
            option.value = animal.id;
            option.textContent = animal.tenLoai;
            select.appendChild(option);
          });
        }
      }),
  ]).catch((error) => {
    console.error("Error initializing filters:", error);
    throw error;
  });

  // Show filters when region view is activated
  document.getElementById("regionBtn").addEventListener("click", () => {
    const filtersPanel = document.getElementById("regionFilters");
    filtersPanel.style.display =
      filtersPanel.style.display === "none" ? "block" : "none";
  });

  // Handle filter changes
  const filterControls = [
    "regionLevel",
    "tenBenh",
    "mucDoBenh",
    "loaiVatNuoi",
    "fromDate",
    "toDate",
  ];
  filterControls.forEach((id) => {
    document.getElementById(id)?.addEventListener("change", () => {
      const applyBtn = document.getElementById("applyFilters");
      if (applyBtn) applyBtn.classList.add("active");
    });
  });

  // Update region button click handler
  document.getElementById("regionBtn")?.addEventListener("click", (e) => {
    e.stopPropagation();
    const dropdown = document.getElementById("regionDropdown");
    const isActive = dropdown.classList.contains("active");
    
    // Close any other open dropdowns first
    document.querySelectorAll(".region-dropdown.active").forEach(el => {
      if (el !== dropdown) {
        el.classList.remove("active");
      }
    });

    // Toggle current dropdown
    dropdown.classList.toggle("active");
    
    if (!isActive) {
      // Load data if opening dropdown
      Promise.all([
        fetch("/api/v1/benh", { credentials: "include" }).then(r => r.json()),
        fetch("/api/v1/loai-vat-nuoi").then(r => r.json())
      ]).then(([diseases, animals]) => {
        updateSelect("tenBenh", diseases, "id", "tenBenh");
        updateSelect("loaiVatNuoi", animals, "id", "tenLoai");
      }).catch(console.error);
    }
  });

  // Close dropdown when clicking outside
  document.addEventListener("click", (e) => {
    if (!e.target.closest(".control-container")) {
      document.querySelectorAll(".region-dropdown").forEach(el => {
        el.classList.remove("active");
      });
    }
  });

  function updateSelect(elementId, data, valueKey, textKey) {
    const select = document.getElementById(elementId);
    if (!select) return;
    
    const currentValue = select.value;
    select.innerHTML = `<option value="">Tất cả ${elementId === "tenBenh" ? "bệnh" : "vật nuôi"}</option>`;
    
    data.forEach(item => {
      const option = document.createElement("option");
      option.value = item[valueKey];
      option.textContent = item[textKey];
      select.appendChild(option);
    });
    
    if (currentValue) {
      select.value = currentValue;
    }
  }

  // Apply filters button handler
  document.getElementById("applyFilters")?.addEventListener("click", () => {
    const dropdown = document.getElementById("regionDropdown");
    loadRegionCases().then(() => {
      dropdown.style.display = "none";
    });
  });

  // Reset filters handler
  document.getElementById("resetFilters")?.addEventListener("click", () => {
    filterControls.forEach((id) => {
      const element = document.getElementById(id);
      if (element) {
        if (element.type === "date") {
          element.value = "";
        } else {
          element.selectedIndex = 0;
        }
      }
    });
    loadRegionCases();
  });

  // Format dates properly before sending
  const handleDateInput = (input) => {
    input.addEventListener('change', (e) => {
      if (e.target.value) {
        try {
          const date = new Date(e.target.value);
          e.target.value = date.toISOString().split('T')[0];
        } catch (err) {
          console.error('Invalid date:', err);
        }
      }
    });
  };

  // Add date formatting to date inputs
  handleDateInput(document.getElementById('fromDate'));
  handleDateInput(document.getElementById('toDate'));
}
