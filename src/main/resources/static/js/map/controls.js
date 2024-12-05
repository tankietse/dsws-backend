class MapControls {
  constructor(mapLayers) {
    this.mapLayers = mapLayers;
    console.log("Initializing MapControls...");
    this.initializeControls();
  }

  initializeControls() {
    console.log("Attaching event listeners to control buttons...");

    document.getElementById("heatmapBtn").addEventListener("click", () => {
      console.log("Heatmap button clicked");
      this.mapLayers.loadHeatmap();
    });

    document.getElementById("markerBtn").addEventListener("click", () => {
      console.log("Marker button clicked");
      this.mapLayers.loadMarkers();
    });

    document.getElementById("clusterBtn").addEventListener("click", () => {
      console.log("Cluster button clicked");
      this.mapLayers.loadClusterSymbols();
    });

    document.getElementById("boundaryBtn").addEventListener("click", () => {
      console.log("Boundary button clicked");
      this.mapLayers.toggleBoundaries();
    });

    document.getElementById("regionBtn").addEventListener("click", () => {
      console.log("Region button clicked");
      this.mapLayers.loadRegionCases();
    });

    document.getElementById("farmBtn").addEventListener("click", () => {
      console.log("Farm button clicked");
      this.mapLayers.loadFarms();
    });

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
