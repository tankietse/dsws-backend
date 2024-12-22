export default function loadRegionCases(filters = {}) {
  return new Promise((resolve, reject) => {
    try {
      const mapLoading = document.getElementById("map-loading");
      if (mapLoading) mapLoading.style.display = "flex";

      this.clearCurrentLayer();

      const { loaiVatNuoiId, benhId, showFarms } = filters;

      if (!loaiVatNuoiId) {
        throw new Error("Vui lòng chọn loại vật nuôi");
      }

      // Create feature group to hold all layers
      this.currentLayer = L.featureGroup().addTo(this.map);

      fetch(
        `/api/v1/vung-dich/by-animal-type?loaiVatNuoiId=${loaiVatNuoiId}${
          benhId ? `&benhId=${benhId}` : ""
        }`,
        {
          credentials: "include",
        }
      )
        .then((response) => {
          if (!response.ok) throw new Error("Network response was not ok");
          return response.json();
        })
        .then((data) => {
          data.forEach((vungDich) => {
            // Create a group for this disease zone
            const zoneGroup = L.featureGroup().addTo(this.currentLayer);
            let boundaryLayer = null; // Initialize reference

            // Add administrative boundary if it exists
            if (vungDich.donViHanhChinh?.boundary) {
              boundaryLayer = L.geoJSON(vungDich.donViHanhChinh.boundary, {
                style: {
                  color: vungDich.colorCode,
                  weight: 2,
                  opacity: 0.8,
                  fillOpacity: 0.3,
                  fillColor: vungDich.colorCode,
                },
              });
              boundaryLayer.addTo(zoneGroup);
            }

            // Add disease zone circle with stronger colors
            const center = [
              vungDich.centerPoint.coordinates[1],
              vungDich.centerPoint.coordinates[0],
            ];

            const circle = L.circle(center, {
              radius: vungDich.banKinh * 1000,
              color: vungDich.colorCode,
              fillColor: vungDich.colorCode,
              fillOpacity: 0.5,
              weight: 3,
              className: "disease-zone-circle z-1100",
            });
            circle.addTo(zoneGroup);

            // Create combined popup content
            const popupContent = `
            <div class="disease-zone-popup z-1000">
              <h3>${vungDich.tenVung}</h3>
              <p><strong>Mức độ:</strong> ${vungDich.mucDo}</p>
              <p><strong>Bán kính:</strong> ${(vungDich.banKinh * 1000).toFixed(
                0
              )}m</p>
              <p><strong>Đơn vị hành chính:</strong> ${
                vungDich.donViHanhChinh?.ten || "Không xác định"
              }</p>
              ${
                showFarms
                  ? `
                <div class="affected-farms">
                  <p><strong>Số trang trại bị ảnh hưởng:</strong> ${
                    vungDich.trangTrais?.length || 0
                  }</p>
                </div>
              `
                  : ""
              }
            </div>
          `;

            // Store the tooltip instance
            let activeTooltip = null;

            // Add hover interaction for the entire zone
            zoneGroup.on({
              mouseover: (e) => {
                if (boundaryLayer) {
                  boundaryLayer.setStyle({
                    fillOpacity: 0.5,
                    opacity: 1,
                    weight: 3,
                  });
                }

                circle.setStyle({
                  fillOpacity: 0.7,
                  weight: 4,
                });

                // Create and show tooltip
                activeTooltip = L.tooltip({
                  permanent: false,
                  direction: "top",
                  className: "disease-zone-tooltip",
                })
                  .setContent(popupContent)
                  .setLatLng(circle.getLatLng())
                  .addTo(this.map);
              },
              mouseout: (e) => {
                if (boundaryLayer) {
                  boundaryLayer.setStyle({
                    fillOpacity: 0.3,
                    opacity: 0.8,
                    weight: 2,
                  });
                }

                circle.setStyle({
                  fillOpacity: 0.5,
                  weight: 3,
                });

                // Remove tooltip
                if (activeTooltip) {
                  this.map.removeLayer(activeTooltip);
                  activeTooltip = null;
                }
              },
            });

            // Add farm markers if enabled
            if (showFarms && vungDich.trangTrais) {
              vungDich.trangTrais.forEach((farm) => {
                if (farm.location) {
                  const farmMarker = L.circleMarker(
                    [
                      farm.location.coordinates[1],
                      farm.location.coordinates[0],
                    ],
                    {
                      radius: 6,
                      color: "#fff",
                      weight: 2,
                      fillColor: vungDich.colorCode,
                      fillOpacity: 1,
                    }
                  ).bindTooltip(
                    `
                  <div class="farm-tooltip">
                    <h4>${farm.tenChu || "Không có tên"}</h4>
                    <p>${farm.diaChi || "Không có địa chỉ"}</p>
                    <p>Khoảng cách: ${(farm.khoangCach * 1000).toFixed(0)}m</p>
                  </div>
                `,
                    {
                      permanent: false,
                      direction: "top",
                    }
                  );
                  farmMarker.addTo(zoneGroup);
                }
              });
            }
          });

          // Fit bounds to show all features
          if (this.currentLayer.getBounds().isValid()) {
            this.map.fitBounds(this.currentLayer.getBounds());
          }

          resolve(true);
        })
        .catch((error) => {
          console.error("Error loading region data:", error);
          reject(error);
        })
        .finally(() => {
          if (mapLoading) mapLoading.style.display = "none";
        });
    } catch (error) {
      console.error("Error in loadRegionCases:", error);
      if (mapLoading) mapLoading.style.display = "none";
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

    // // Load animal type options with IDs
    // fetch("/api/v1/loai-vat-nuoi")
    //   .then((response) => {
    //     if (!response.ok) throw new Error("Failed to load animal types");
    //     return response.json();
    //   })
    //   .then((animals) => {
    //     const select = document.getElementById("loaiVatNuoi");
    //     if (select) {
    //       select.innerHTML = '<option value="">Tất cả vật nuôi</option>';
    //       animals.forEach((animal) => {
    //         const option = document.createElement("option");
    //         option.value = animal.id;
    //         option.textContent = animal.tenLoai;
    //         select.appendChild(option);
    //       });
    //     }
    //   }),
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
    document.querySelectorAll(".region-dropdown.active").forEach((el) => {
      if (el !== dropdown) {
        el.classList.remove("active");
      }
    });

    // Toggle current dropdown
    dropdown.classList.toggle("active");

    if (!isActive) {
      // Load data if opening dropdown
      Promise.all([
        fetch("/api/v1/benh", { credentials: "include" }).then((r) => r.json()),
        fetch("/api/v1/loai-vat-nuoi").then((r) => r.json()),
      ])
        .then(([diseases, animals]) => {
          updateSelect("tenBenh", diseases, "id", "tenBenh");
          updateSelect("loaiVatNuoi", animals, "id", "tenLoai");
        })
        .catch(console.error);
    }
  });

  // Close dropdown when clicking outside
  document.addEventListener("click", (e) => {
    if (!e.target.closest(".control-container")) {
      document.querySelectorAll(".region-dropdown").forEach((el) => {
        el.classList.remove("active");
      });
    }
  });

  function updateSelect(elementId, data, valueKey, textKey) {
    const select = document.getElementById(elementId);
    if (!select) return;

    const currentValue = select.value;
    select.innerHTML = `<option value="">Tất cả ${
      elementId === "tenBenh" ? "bệnh" : "vật nuôi"
    }</option>`;

    data.forEach((item) => {
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
    input.addEventListener("change", (e) => {
      if (e.target.value) {
        try {
          const date = new Date(e.target.value);
          e.target.value = date.toISOString().split("T")[0];
        } catch (err) {
          console.error("Invalid date:", err);
        }
      }
    });
  };

  // Add date formatting to date inputs
  handleDateInput(document.getElementById("fromDate"));
  handleDateInput(document.getElementById("toDate"));
}
