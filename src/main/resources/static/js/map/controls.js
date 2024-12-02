class MapControls {
  constructor(mapLayers) {
    this.mapLayers = mapLayers;
    this.initializeControls();
  }

  initializeControls() {
    document
      .getElementById("heatmapBtn")
      .addEventListener("click", () => this.mapLayers.loadHeatmap());
    document
      .getElementById("markerBtn")
      .addEventListener("click", () => this.mapLayers.loadMarkers());
    document
      .getElementById("clusterBtn")
      .addEventListener("click", () => this.mapLayers.loadClusterSymbols());
    document
      .getElementById("boundaryBtn")
      .addEventListener("click", () => this.mapLayers.toggleBoundaries());
    document
      .getElementById("regionBtn")
      .addEventListener("click", () => this.mapLayers.loadRegionCases());
    document
      .getElementById("farmBtn")
      .addEventListener("click", () => this.mapLayers.loadFarms());

    this.handleActiveStates();
  }

  handleActiveStates() {
    const buttons = document.querySelectorAll(".control-btn");
    buttons.forEach((button) => {
      button.addEventListener("click", () => {
        buttons.forEach((b) => b.classList.remove("active"));
        button.classList.add("active");
      });
    });
  }
}
