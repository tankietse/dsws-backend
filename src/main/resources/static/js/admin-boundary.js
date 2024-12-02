let map, currentLayer;
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
    Streets: L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"),
    Satellite: L.tileLayer(
      "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
    ),
    Terrain: L.tileLayer(
      "https://stamen-tiles-{s}.a.ssl.fastly.net/terrain/{z}/{x}/{y}{r}.png"
    ),
  };

  baseLayers["Streets"].addTo(map);
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

function loadDonViHanhChinh() {
  $.get(apiUrl, function (data) {
    const tableBody = $("#tableBody");
    tableBody.empty();
    data.forEach((item) => {
      tableBody.append(`
        <tr>
          <td class="border px-4 py-2">${item.id}</td>
          <td class="border px-4 py-2">${item.ten}</td>
          <td class="border px-4 py-2">${item.capHanhChinh}</td>
          <td class="border px-4 py-2">
            <button class="btnEdit bg-yellow-500 text-white px-2 py-1 rounded" data-id="${item.id}">Sửa</button>
            <button class="btnDelete bg-red-500 text-white px-2 py-1 rounded" data-id="${item.id}">Xóa</button>
          </td>
        </tr>
      `);
    });
  });
}

$(document).ready(function () {
  loadDonViHanhChinh();

  $("#btnAdd").click(function () {
    $("#modalTitle").text("Thêm Đơn Vị Hành Chính");
    $("#donViId").val("");
    $("#ten").val("");
    $("#capHanhChinh").val("");
    $("#modal").removeClass("hidden");
  });

  $("#btnClose").click(function () {
    $("#modal").addClass("hidden");
  });

  $("#btnSave").click(function () {
    const id = $("#donViId").val();
    const donViData = {
      ten: $("#ten").val(),
      capHanhChinh: $("#capHanhChinh").val(),
    };

    if (id) {
      $.ajax({
        url: `${apiUrl}/${id}`,
        type: "PUT",
        contentType: "application/json",
        data: JSON.stringify(donViData),
        success: function () {
          loadDonViHanhChinh();
          $("#modal").addClass("hidden");
        },
      });
    } else {
      $.post(apiUrl, donViData, function () {
        loadDonViHanhChinh();
        $("#modal").addClass("hidden");
      });
    }
  });

  $(document).on("click", ".btnEdit", function () {
    const id = $(this).data("id");
    $.get(`${apiUrl}/${id}`, function (data) {
      $("#modalTitle").text("Sửa Đơn Vị Hành Chính");
      $("#donViId").val(data.id);
      $("#ten").val(data.ten);
      $("#capHanhChinh").val(data.capHanhChinh);
      $("#modal").removeClass("hidden");
    });
  });

  $(document).on("click", ".btnDelete", function () {
    const id = $(this).data("id");
    if (confirm("Bạn có chắc chắn muốn xóa đơn vị hành chính này?")) {
      $.ajax({
        url: `${apiUrl}/${id}`,
        type: "DELETE",
        success: function () {
          loadDonViHanhChinh();
        },
      });
    }
  });

  $("#btnSearch").click(function () {
    const name = $("#searchInput").val();
    $.get(`${apiUrl}/search?name=${name}`, function (data) {
      const tableBody = $("#tableBody");
      tableBody.empty();
      data.forEach((item) => {
        tableBody.append(`
          <tr>
            <td class="border px-4 py-2">${item.id}</td>
            <td class="border px-4 py-2">${item.ten}</td>
            <td class="border px-4 py-2">${item.capHanhChinh}</td>
            <td class="border px-4 py-2">
              <button class="btnEdit bg-yellow-500 text-white px-2 py-1 rounded" data-id="${item.id}">Sửa</button>
              <button class="btnDelete bg-red-500 text-white px-2 py-1 rounded" data-id="${item.id}">Xóa</button>
            </td>
          </tr>
        `);
      });
    });
  });
});
