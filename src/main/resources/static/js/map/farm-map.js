import { MAP_CONFIG } from "./config.js";

require([
  "esri/Map",
  "esri/views/MapView",
  "esri/Basemap",
  "esri/layers/FeatureLayer",
  "esri/layers/GraphicsLayer",
  "esri/widgets/BasemapToggle",
  "esri/widgets/BasemapGallery",
  "esri/widgets/LayerList",
  "esri/widgets/Legend",
  "esri/widgets/Expand",
], function (
  Map,
  MapView,
  Basemap,
  FeatureLayer,
  GraphicsLayer,
  BasemapToggle,
  BasemapGallery,
  LayerList,
  Legend,
  Expand
) {
  // Initialize map
  const map = new Map({
    basemap: "topo-vector",
  });

  // Create the MapView
  const view = new MapView({
    container: "map",
    map: map,
    center: MAP_CONFIG.center,
    zoom: MAP_CONFIG.zoom,
  });

  // Add basemap gallery
  const basemapGallery = new BasemapGallery({
    view: view,
    container: document.createElement("div"),
  });

  const bgExpand = new Expand({
    view: view,
    content: basemapGallery,
    expandIcon: "basemap",
  });
  view.ui.add(bgExpand, "top-right");

  // Create farm feature layer
  const farmLayer = new FeatureLayer({
    url: "/api/v1/trang-trai/geojson",
    title: "Trang trại",
    popupTemplate: {
      title: "{tenTrangTrai}",
      content: [
        {
          type: "fields",
          fieldInfos: [
            { fieldName: "diaChi", label: "Địa chỉ" },
            { fieldName: "loaiVatNuoi", label: "Loại vật nuôi" },
            { fieldName: "soDienThoai", label: "Số điện thoại" },
          ],
        },
      ],
      actions: [
        {
          title: "Chi tiết",
          id: "farm-details",
          className: "esri-icon-description",
        },
      ],
    },
  });

  // Create heatmap layer
  const heatmapLayer = new FeatureLayer({
    url: "/api/v1/trang-trai/heatmap",
    title: "Mật độ trang trại",
    renderer: {
      type: "heatmap",
      colorStops: [
        { color: "rgba(63, 40, 102, 0)", ratio: 0 },
        { color: "#472b77", ratio: 0.2 },
        { color: "#4e2d87", ratio: 0.4 },
        { color: "#9101c4", ratio: 0.6 },
        { color: "#b100cf", ratio: 0.8 },
        { color: "#ff00ff", ratio: 1 },
      ],
      maxPixelIntensity: 100,
      minPixelIntensity: 0,
    },
    visible: false,
  });

  // Add layers to map
  map.addMany([farmLayer, heatmapLayer]);

  // Add layer list widget
  const layerList = new LayerList({
    view: view,
    container: document.createElement("div"),
  });

  const layerListExpand = new Expand({
    view: view,
    content: layerList,
    expandIcon: "layers",
  });
  view.ui.add(layerListExpand, "top-right");

  // Add legend
  const legend = new Legend({
    view: view,
    container: document.createElement("div"),
  });

  const legendExpand = new Expand({
    view: view,
    content: legend,
    expandIcon: "legend",
  });
  view.ui.add(legendExpand, "bottom-right");

  // Handle layer visibility from controls
  document.querySelectorAll(".layer-control input").forEach((input) => {
    input.addEventListener("change", function () {
      const layerType = this.dataset.layer;
      switch (layerType) {
        case "farms":
          farmLayer.visible = this.checked;
          break;
        case "heatmap":
          heatmapLayer.visible = this.checked;
          break;
        // Add other layer controls as needed
      }
    });
  });

  // Handle farm search
  const searchInput = document.getElementById("farm-search");
  searchInput.addEventListener("input", function (e) {
    const searchTerm = e.target.value.toLowerCase();
    const whereClause = `LOWER(tenTrangTrai) LIKE '%${searchTerm}%'`;
    farmLayer.definitionExpression = searchTerm ? whereClause : null;
  });

  // Load farm data into side panel
  function loadFarmList() {
    fetch("/api/v1/trang-trai")
      .then((response) => response.json())
      .then((farms) => {
        const farmList = document.querySelector(".farm-info-panel .space-y-3");
        farmList.innerHTML = farms
          .map(
            (farm) => `
                    <div class="bg-blue-50 p-3 rounded-lg">
                        <h3 class="font-semibold">${farm.tenTrangTrai}</h3>
                        <p class="text-sm text-gray-600">${farm.diaChi}</p>
                        <button onclick="showFarmDetails(${farm.id})" 
                                class="mt-2 text-blue-600 text-sm hover:text-blue-800">
                            Xem chi tiết
                        </button>
                    </div>
                `
          )
          .join("");
      })
      .catch(console.error);
  }

  // Initial load
  loadFarmList();

  // Export for global access
  window.showFarmDetails = function (farmId) {
    fetch(`/api/v1/trang-trai/${farmId}/chi-tiet`)
      .then((response) => response.json())
      .then((details) => {
        // Implement detail view logic
        console.log("Farm details:", details);
      })
      .catch(console.error);
  };

  // Add modal control functions
  window.openAddFarmModal = function () {
    const modal = document.getElementById("addFarmModal");
    modal.classList.remove("hidden");

    // Load loại vật nuôi options
    loadAnimalTypes();

    // Load địa giới hành chính
    loadAdministrativeUnits();
  };

  window.closeAddFarmModal = function () {
    const modal = document.getElementById("addFarmModal");
    modal.classList.add("hidden");
    // Reset form
    document.getElementById("addFarmForm").reset();
  };

  // Load animal types for the select
  async function loadAnimalTypes() {
    try {
      const response = await fetch("/api/v1/loai-vat-nuoi");
      const data = await response.json();

      const options = data
        .map((type) => `<option value="${type.id}">${type.tenLoai}</option>`)
        .join("");

      // Get all loại vật nuôi selects (there might be multiple due to dynamic rows)
      document
        .querySelectorAll('select[name^="vatNuoi"][name$="loaiVatNuoi"]')
        .forEach((select) => {
          select.innerHTML =
            '<option value="">Chọn loại vật nuôi</option>' + options;
        });
    } catch (error) {
      console.error("Error loading animal types:", error);
    }
  }

  // Setup form submission
  document
    .getElementById("addFarmForm")
    .addEventListener("submit", async function (e) {
      e.preventDefault();

      const formData = new FormData(this);
      const data = {
        tenTrangTrai: formData.get("tenTrangTrai"),
        maTrangTrai: formData.get("maTrangTrai"),
        tenChu: formData.get("tenChu"),
        soDienThoai: formData.get("soDienThoai"),
        email: formData.get("email"),
        dienTich: parseFloat(formData.get("dienTich")),
        tongDan: parseInt(formData.get("tongDan")),
        phuongThucChanNuoi: formData.get("phuongThucChanNuoi"),
        diaChi: {
          soNha: formData.get("soNha"),
          tenDuong: formData.get("tenDuong"),
          khuPho: formData.get("khuPho"),
          idPhuongXa: formData.get("phuongXa"),
        },
        vatNuoi: Array.from(document.querySelectorAll(".vat-nuoi-row")).map(
          (row) => ({
            idLoaiVatNuoi: row.querySelector('[name$="loaiVatNuoi"]').value,
            soLuong: parseInt(row.querySelector('[name$="soLuong"]').value),
          })
        ),
      };

      try {
        const response = await fetch("/api/v1/trang-trai", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(data),
        });

        if (response.ok) {
          const newFarm = await response.json();
          // Add new farm to map and list
          addFarmToMap(newFarm);
          loadFarmList();
          closeAddFarmModal();
          // Show success message
          alert("Thêm trang trại thành công!");
        } else {
          throw new Error("Lỗi khi thêm trang trại");
        }
      } catch (error) {
        console.error("Error:", error);
        alert("Có lỗi xảy ra khi thêm trang trại");
      }
    });

  // Add button to add more animal types
  document
    .getElementById("addVatNuoiBtn")
    .addEventListener("click", function () {
      const container = document.getElementById("vatNuoiContainer");
      const index = container.children.length;

      const newRow = document.createElement("div");
      newRow.className = "flex gap-2 vat-nuoi-row";
      newRow.innerHTML = `
            <select name="vatNuoi[${index}].loaiVatNuoi" 
                    class="flex-1 border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500" 
                    required>
                <option value="">Chọn loại vật nuôi</option>
            </select>
            <input type="number" 
                   name="vatNuoi[${index}].soLuong" 
                   placeholder="Số lượng"
                   class="w-32 border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                   required>
            <button type="button" 
                    class="text-red-500 hover:text-red-700"
                    onclick="this.parentElement.remove()">
                <i class="fas fa-times"></i>
            </button>
        `;

      container.appendChild(newRow);
      loadAnimalTypes(); // Load options for new select
    });

  // Close modal when clicking outside
  document
    .getElementById("addFarmModal")
    .addEventListener("click", function (e) {
      if (e.target === this) {
        closeAddFarmModal();
      }
    });

  // Add filter state
  let filterState = {
    search: '',
    animalType: '',
    farmingMethod: '',
    minScale: null,
    maxScale: null,
    area: ''
  };

  // Initialize UI controls
  initializeFilterControls();
  initializeVisualizationControls();

  function initializeFilterControls() {
    // Toggle filter panel
    document.getElementById('toggleFilterBtn').addEventListener('click', function() {
      const content = document.getElementById('filterContent');
      const icon = this.querySelector('i');
      if (content.style.display === 'none') {
        content.style.display = 'block';
        icon.classList.replace('fa-chevron-down', 'fa-chevron-up');
      } else {
        content.style.display = 'none';
        icon.classList.replace('fa-chevron-up', 'fa-chevron-down');
      }
    });

    // Load animal types
    fetch('/api/v1/loai-vat-nuoi')
      .then(response => response.json())
      .then(data => {
        const select = document.getElementById('animalTypeFilter');
        data.forEach(type => {
          const option = document.createElement('option');
          option.value = type.id;
          option.textContent = type.tenLoai;
          select.appendChild(option);
        });
      });

    // Load administrative areas
    fetch('/api/v1/dia-gioi/tree')
      .then(response => response.json())
      .then(data => {
        const select = document.getElementById('areaFilter');
        populateAreaOptions(data, select);
      });

    // Add filter event listeners
    const filterInputs = ['farm-search', 'animalTypeFilter', 'farmingMethodFilter', 
                          'minScale', 'maxScale', 'areaFilter'];
    filterInputs.forEach(id => {
      document.getElementById(id).addEventListener('change', updateFilters);
    });

    // Reset filters
    document.getElementById('resetFilterBtn').addEventListener('click', resetFilters);
  }

  function updateFilters() {
    filterState = {
      search: document.getElementById('farm-search').value,
      animalType: document.getElementById('animalTypeFilter').value,
      farmingMethod: document.getElementById('farmingMethodFilter').value,
      minScale: document.getElementById('minScale').value,
      maxScale: document.getElementById('maxScale').value,
      area: document.getElementById('areaFilter').value
    };

    // Build where clause for feature layer
    let whereClause = buildWhereClause(filterState);
    farmLayer.definitionExpression = whereClause;

    // Update other visualization layers
    updateVisualization();
  }

  function buildWhereClause(filters) {
    const conditions = [];
    
    if (filters.search) {
      conditions.push(`LOWER(tenTrangTrai) LIKE '%${filters.search.toLowerCase()}%'`);
    }
    if (filters.animalType) {
      conditions.push(`idLoaiVatNuoi = ${filters.animalType}`);
    }
    if (filters.farmingMethod) {
      conditions.push(`phuongThucChanNuoi = '${filters.farmingMethod}'`);
    }
    if (filters.minScale) {
      conditions.push(`tongDan >= ${filters.minScale}`);
    }
    if (filters.maxScale) {
      conditions.push(`tongDan <= ${filters.maxScale}`);
    }
    if (filters.area) {
      conditions.push(`idPhuongXa = ${filters.area}`);
    }

    return conditions.length > 0 ? conditions.join(' AND ') : '1=1';
  }
});
