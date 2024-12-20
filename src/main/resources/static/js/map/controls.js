export class MapControls {
  constructor(mapLayers) {
    this.mapLayers = mapLayers;
    this.isCollapsed = false;
    this.controls = {
      heatmap: {
        id: "heatmapBtn",
        handler: () => this.mapLayers.loadHeatmap(),
      },
      marker: { id: "markerBtn", handler: () => this.mapLayers.loadMarkers() },
      cluster: {
        id: "clusterBtn",
        handler: () => this.mapLayers.loadClusterSymbols(),
      },
      boundary: {
        id: "boundaryBtn",
        handler: () => this.mapLayers.toggleBoundaries(),
      },
      region: {
        id: "regionBtn",
        handler: () => {
          const regionDropdown = document.getElementById("regionDropdown");
          if (regionDropdown.style.display === "none") {
            regionDropdown.style.display = "block";
          } else {
            regionDropdown.style.display = "none";
            this.mapLayers.clearCurrentLayer();
          }
        },
      },
      farm: { id: "farmBtn", handler: () => this.mapLayers.loadFarms() },
    };

    this.initializeControls();
    this.initializeRegionLevelSelect();
    this.initializeCollapseButton();
    this.loadStoredState();
    this.initializeRegionDropdown();
  }

  initializeControls() {
    console.log("Attaching event listeners to control buttons...");

    Object.entries(this.controls).forEach(([key, control]) => {
      const button = document.getElementById(control.id);
      if (button) {
        button.addEventListener("click", () => {
          console.log(`${key} button clicked`);
          control.handler();
        });
      } else {
        console.warn(`Button with id ${control.id} not found`);
      }
    });

    this.handleActiveStates();
  }

  initializeRegionLevelSelect() {
    const regionSelect = document.getElementById("regionLevel");
    if (regionSelect) {
      regionSelect.addEventListener("change", (e) => {
        const level = e.target.value;
        this.mapLayers.loadRegionCases(level);
      });
    }
  }

  initializeCollapseButton() {
    const mapControls = document.querySelector(".map-controls");
    const collapseBtn = document.querySelector(".collapse-btn");
    const controlsHeader = document.querySelector(".map-controls-header");

    if (controlsHeader) {
      controlsHeader.addEventListener("click", (e) => {
        if (e.target === controlsHeader || e.target.closest(".collapse-btn")) {
          this.toggleCollapse(mapControls);
        }
      });
    }
  }

  toggleCollapse(mapControls) {
    this.isCollapsed = !this.isCollapsed;
    mapControls.classList.toggle("collapsed");
    this.saveState();
  }

  loadStoredState() {
    const mapControls = document.querySelector(".map-controls");
    const storedState = localStorage.getItem("mapControlsState");

    if (storedState) {
      const state = JSON.parse(storedState);
      this.isCollapsed = state.isCollapsed;

      if (this.isCollapsed) {
        mapControls.classList.add("collapsed");
      }
    }
  }

  saveState() {
    const state = {
      isCollapsed: this.isCollapsed,
    };
    localStorage.setItem("mapControlsState", JSON.stringify(state));
  }

  handleActiveStates() {
    const buttons = document.querySelectorAll(".control-btn");
    let activeButton = null;

    buttons.forEach((button) => {
      button.addEventListener("click", () => {
        if (activeButton) {
          activeButton.classList.remove("active");
        }
        button.classList.add("active");
        activeButton = button;
      });
    });
  }

  initializeRegionDropdown() {
    const tenBenhSelect = document.getElementById("tenBenh");
    const loaiVatNuoiSelect = document.getElementById("loaiVatNuoi");
    const mucDoBenhSelect = document.getElementById("mucDoBenh");
    const regionLevelSelect = document.getElementById("regionLevel");
    const fromDateInput = document.getElementById("fromDate");
    const toDateInput = document.getElementById("toDate");
    const applyFiltersBtn = document.getElementById("applyFilters");
    const resetFiltersBtn = document.getElementById("resetFilters");

    // Tải dữ liệu cho Loại bệnh và Loại vật nuôi
    this.loadLoaiBenhOptions(tenBenhSelect);
    this.loadLoaiVatNuoiOptions(loaiVatNuoiSelect);

    applyFiltersBtn.addEventListener("click", () => {
      const filters = {
        regionLevel: regionLevelSelect.value,
        tenBenh: tenBenhSelect.value,
        mucDoBenh: mucDoBenhSelect.value,
        loaiVatNuoi: loaiVatNuoiSelect.value,
        fromDate: fromDateInput.value,
        toDate: toDateInput.value,
      };
      this.mapLayers.loadRegionCases(filters);
    });

    resetFiltersBtn.addEventListener("click", () => {
      regionLevelSelect.value = "8";
      tenBenhSelect.value = "";
      mucDoBenhSelect.value = "";
      loaiVatNuoiSelect.value = "";
      fromDateInput.value = "";
      toDateInput.value = "";
      this.mapLayers.clearCurrentLayer();
    });
  }

  loadLoaiBenhOptions(selectElement) {
    fetch("/api/v1/benh", {
      credentials: "include",
    })
      .then((response) => response.json())
      .then((data) => {
        data.forEach((disease) => {
          const option = document.createElement("option");
          option.value = disease.id;
          option.textContent = disease.tenBenh;
          selectElement.appendChild(option);
        });
      });
  }

  loadLoaiVatNuoiOptions(selectElement) {
    fetch("/api/v1/loai-vat-nuoi", {
      credentials: "include",
    })
      .then((response) => response.json())
      .then((data) => {
        data.forEach((animalType) => {
          const option = document.createElement("option");
          option.value = animalType.id;
          option.textContent = animalType.tenLoai;
          selectElement.appendChild(option);
        });
      });
  }
}
