
const baseApiUrl = "/api/v1/don-vi-hanh-chinh";
const apiUrl = "/api/v1/don-vi-hanh-chinh";

// Initialize Dropzone
Dropzone.options.geojsonDropzone = {
  acceptedFiles: ".geojson,.json",
  maxFilesize: 10,
  init: function () {
    this.on("success", function (file, response) {
      showToast("Success", "Dữ liệu đã được import thành công");
      loadBoundaries();
    });
    this.on("error", function (file, message) {
      showToast("Error", "Lỗi khi import dữ liệu: " + message);
    });
  },
};

// Initialize map
function initMap() {
  map = L.map("map").setView([16.047079, 108.20623], 5);

  // Add base layers
  const baseLayers = {
    Satellite: L.tileLayer(
      "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
    ),
    Terrain: L.tileLayer(
      "https://stamen-tiles-{s}.a.ssl.fastly.net/terrain/{z}/{x}/{y}{r}.png"
    ),
  };

  baseLayers["Satellite"].addTo(map);
  L.control.layers(baseLayers).addTo(map);

  // Initialize drawing controls
  map.pm.addControls({
    position: "topleft",
    drawMarker: false,
    drawCircle: false,
    drawCircleMarker: false,
    drawRectangle: false,
    drawPolyline: false,
    drawText: false,
    cutPolygon: true,
  });

  loadBoundaries();
  initTreeView();
}

// Load administrative boundaries
async function loadBoundaries() {
  try {
    const response = await fetch(`${baseApiUrl}/cap/6/geojson`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const data = await response.json();

    if (currentLayer) {
      map.removeLayer(currentLayer);
    }

    currentLayer = L.geoJSON(data, {
      style: getStyleByLevel,
      onEachFeature: onEachFeature,
    }).addTo(map);

    map.fitBounds(currentLayer.getBounds());
  } catch (error) {
    console.error("Error loading boundaries:", error);
    showToast("Error", "Lỗi khi tải dữ liệu ranh giới");
  }
}

// Style boundaries based on admin level
function getStyleByLevel(feature) {
  const level = feature.properties.adminLevel;
  return {
    fillColor: getColorByLevel(level),
    weight: 2,
    opacity: 1,
    color: "white",
    fillOpacity: 0.7,
  };
}

// Handle feature interaction
function onEachFeature(feature, layer) {
  layer.on({
    mouseover: highlightFeature,
    mouseout: resetHighlight,
    click: showBoundaryDetails,
  });
}

// Show boundary details in stats panel
function showBoundaryDetails(e) {
  const feature = e.target.feature;
  const stats = document.getElementById("boundaryStats");
  const panel = document.getElementById("statsPanel");

  stats.innerHTML = `
        <table class="stats-table">
            <tr>
                <th>Tên</th>
                <td>${feature.properties.name}</td>
            </tr>
            <tr>
                <th>Cấp</th>
                <td>${feature.properties.adminLevel}</td>
            </tr>
            <tr>
                <th>Diện tích</th>
                <td>${calculateArea(feature)} km²</td>
            </tr>
        </table>
    `;

  panel.style.display = "block";
}

// Initialize tree view
function initTreeView() {
  // Implementation depends on your tree view library
  // Example using jsTree
}

// Helper functions
function calculateArea(feature) {
  // Calculate area using turf.js
}

function showToast(type, message) {
  // Implement toast notifications
}

// Initialize map when document is ready
document.addEventListener("DOMContentLoaded", initMap);

class AdminBoundaryManager {
  constructor() {
    this.map = null;
    this.boundaryLayer = null;
    this.highlightLayer = null;
    this.init();
  }

  async init() {
    // Initialize map with config settings
    this.map = L.map("map", {
      center: MAP_CONFIG.center,
      zoom: MAP_CONFIG.zoom,
      minZoom: MAP_CONFIG.minZoom,
      maxZoom: MAP_CONFIG.maxZoom,
    });

    // Add ArcGIS basemap
    L.esri.Vector.vectorBasemapLayer(MAP_CONFIG.basemapEnum, {
      apikey: MAP_CONFIG.accessToken,
    }).addTo(this.map);

    // Initialize table handlers
    this.initTableHandlers();

    // Load initial data
    await this.loadBoundaries();
  }

  async loadBoundaries(filters = {}) {
    try {
      let url = "/api/v1/don-vi-hanh-chinh/geojson";
      const params = new URLSearchParams();
      if (filters.capHanhChinh)
        params.append("capHanhChinh", filters.capHanhChinh);
      if (filters.name) params.append("name", filters.name);
      if (params.toString()) url += "?" + params.toString();

      const response = await fetch(url);
      const data = await response.json();

      if (this.boundaryLayer) {
        this.map.removeLayer(this.boundaryLayer);
      }

      this.boundaryLayer = L.geoJSON(data, {
        style: {
          color: "#003399",
          weight: 1,
          opacity: 0.5,
          fillOpacity: 0.1,
        },
        onEachFeature: (feature, layer) => {
          layer.on({
            mouseover: (e) => this.highlightFeature(e.target),
            mouseout: (e) => this.resetHighlight(e.target),
            click: (e) => this.zoomToFeature(e.target),
          });

          // Bind popup with administrative unit info
          layer.bindPopup(this.createPopupContent(feature.properties));
        },
      }).addTo(this.map);

      // Update table with the same data
      this.updateTable(data.features);
    } catch (error) {
      console.error("Error loading boundaries:", error);
    }
  }

  highlightFeature(layer) {
    layer.setStyle({
      weight: 2,
      color: "#666",
      fillOpacity: 0.3,
    });
  }

  resetHighlight(layer) {
    this.boundaryLayer.resetStyle(layer);
  }

  zoomToFeature(layer) {
    this.map.fitBounds(layer.getBounds());
  }

  createPopupContent(properties) {
    return `
        <div class="popup-content">
            <h3 class="font-bold">${properties.ten}</h3>
            <p>Cấp hành chính: ${properties.capHanhChinh}</p>
            ${
              properties.donViCha
                ? `<p>Thuộc: ${properties.donViCha.ten}</p>`
                : ""
            }
        </div>
    `;
  }

  initTableHandlers() {
    // Filter handlers
    document.getElementById("capFilter")?.addEventListener("change", (e) => {
      this.loadBoundaries({ capHanhChinh: e.target.value });
    });

    document.getElementById("nameFilter")?.addEventListener("input", (e) => {
      this.loadBoundaries({ name: e.target.value });
    });
  }

  updateTable(features) {
    const tbody = document.getElementById("donViTableBody");
    if (!tbody) return;

    tbody.innerHTML = features
      .map(
        (feature) => `
        <tr class="hover:bg-gray-50 cursor-pointer" data-id="${
          feature.properties.id
        }">
            <td class="border px-4 py-2">${feature.properties.id}</td>
            <td class="border px-4 py-2">${feature.properties.ten}</td>
            <td class="border px-4 py-2">${feature.properties.capHanhChinh}</td>
            <td class="border px-4 py-2">${
              feature.properties.donViCha?.ten || ""
            }</td>
            <td class="border px-4 py-2">
                <button onclick="editDonViHanhChinh(${
                  feature.properties.id
                })" class="text-blue-600 hover:text-blue-800 mr-2">
                    <i class="fas fa-edit"></i>
                </button>
                <button onclick="deleteDonViHanhChinh(${
                  feature.properties.id
                })" class="text-red-600 hover:text-red-800">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
    `
      )
      .join("");

    // Add row click handlers
    tbody.querySelectorAll("tr").forEach((row) => {
      row.addEventListener("click", () => {
        const feature = features.find(
          (f) => f.properties.id === parseInt(row.dataset.id)
        );
        if (feature) {
          const bounds = L.geoJSON(feature).getBounds();
          this.map.fitBounds(bounds);
        }
      });
    });
  }
}

// Initialize when DOM is loaded
document.addEventListener("DOMContentLoaded", () => {
  window.adminBoundaryManager = new AdminBoundaryManager();
});

class AdminBoundaryTable {
  constructor() {
    this.currentLayer = null;
    this.highlightLayer = null;
    this.waitForMap().then(() => {
      this.initializeEventListeners();
      this.loadInitialData();
    });
  }

  // Wait for map to be initialized by init.js
  async waitForMap() {
    return new Promise((resolve) => {
      const checkMap = () => {
        if (window.mapInit && window.mapInit.map) {
          resolve();
        } else {
          setTimeout(checkMap, 100);
        }
      };
      checkMap();
    });
  }

  initializeEventListeners() {
    // Filter handlers
    document.getElementById("capFilter").addEventListener("change", (e) => {
      this.filterData(
        e.target.value,
        document.getElementById("nameFilter").value
      );
    });

    document.getElementById("nameFilter").addEventListener("input", (e) => {
      this.filterData(
        document.getElementById("capFilter").value,
        e.target.value
      );
    });

    // Entries per page handler
    document.getElementById("entries").addEventListener("change", (e) => {
      this.itemsPerPage = parseInt(e.target.value);
      this.currentPage = 1;
      this.loadData();
    });
  }

  async filterData(capHanhChinh, name) {
    try {
      let url = "/api/v1/don-vi-hanh-chinh/geojson";
      const params = new URLSearchParams();
      if (capHanhChinh) params.append("capHanhChinh", capHanhChinh);
      if (name) params.append("name", name);
      if (params.toString()) url += "?" + params.toString();

      const response = await fetch(url);
      const data = await response.json();

      // Update table
      this.updateTable(data.features);

      // Update map
      this.updateMapLayer(data);
    } catch (error) {
      console.error("Error filtering data:", error);
    }
  }

  updateTable(features) {
    const tbody = document.getElementById("donViTableBody");
    if (!tbody) return;

    tbody.innerHTML = features
      .map(
        (feature) => `
            <tr class="hover:bg-gray-50 cursor-pointer" data-id="${
              feature.properties.id
            }">
                <td class="border px-4 py-2">${feature.properties.id}</td>
                <td class="border px-4 py-2">${feature.properties.ten}</td>
                <td class="border px-4 py-2">${
                  feature.properties.capHanhChinh
                }</td>
                <td class="border px-4 py-2">${
                  feature.properties.donViCha?.ten || ""
                }</td>
                <td class="border px-4 py-2">
                    <button onclick="editDonViHanhChinh(${
                      feature.properties.id
                    })" 
                            class="text-blue-600 hover:text-blue-800 mr-2">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button onclick="deleteDonViHanhChinh(${
                      feature.properties.id
                    })" 
                            class="text-red-600 hover:text-red-800">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>
        `
      )
      .join("");

    // Add row click handlers to highlight on map
    tbody.querySelectorAll("tr").forEach((row) => {
      row.addEventListener("click", () => {
        const feature = features.find(
          (f) => f.properties.id === parseInt(row.dataset.id)
        );
        if (feature) {
          this.highlightFeatureOnMap(feature);
        }
      });
    });
  }

  updateMapLayer(geoJson) {
    // Remove existing layer if any
    if (this.currentLayer && window.mapInit.map) {
      window.mapInit.map.removeLayer(this.currentLayer);
    }

    // Add new layer
    if (window.mapInit && window.mapInit.map) {
      this.currentLayer = L.geoJSON(geoJson, {
        style: {
          color: "#003399",
          weight: 1,
          opacity: 0.5,
          fillOpacity: 0.1,
        },
        onEachFeature: (feature, layer) => {
          layer.bindPopup(this.createPopupContent(feature.properties));
        },
      }).addTo(window.mapInit.map);
    }
  }

  highlightFeatureOnMap(feature) {
    if (!window.mapInit || !window.mapInit.map) return;

    // Remove previous highlight
    if (this.highlightLayer) {
      window.mapInit.map.removeLayer(this.highlightLayer);
    }

    // Add highlight layer
    this.highlightLayer = L.geoJSON(feature, {
      style: {
        color: "#ff7800",
        weight: 2,
        opacity: 0.9,
        fillOpacity: 0.3,
      },
    }).addTo(window.mapInit.map);

    // Zoom to feature
    window.mapInit.map.fitBounds(this.highlightLayer.getBounds());
  }

  createPopupContent(properties) {
    return `
            <div class="popup-content">
                <h3 class="font-bold">${properties.ten}</h3>
                <p>Cấp hành chính: ${properties.capHanhChinh}</p>
                ${
                  properties.donViCha
                    ? `<p>Thuộc: ${properties.donViCha.ten}</p>`
                    : ""
                }
            </div>
        `;
  }

  async loadInitialData() {
    await this.filterData("", "");
  }
}

// Initialize when DOM is loaded
document.addEventListener("DOMContentLoaded", () => {
  window.adminBoundaryTable = new AdminBoundaryTable();
});
