import loadMarkers from "./layers/markers.js";
import loadHeatmap from "./layers/heatmap.js";
import loadClusterSymbols from "./layers/clusterSymbols.js";
import loadAdminBoundaries from "./layers/adminBoundaries.js";
import loadRegionCases from "./layers/regionCases.js";
import loadFarms from "./layers/farms.js";

export class MapLayers {
  constructor(map) {
    this.map = map;
    this.currentLayer = null;
    this.boundaryLayer = null;
    this.legendControl = null;
    this.heatRenderer = L.canvas({ padding: 0.5, willReadFrequently: true });

    // Bind methods to maintain 'this' context
    this.loadMarkers = loadMarkers.bind(this);
    this.loadHeatmap = loadHeatmap.bind(this);
    this.loadClusterSymbols = loadClusterSymbols.bind(this);
    this.loadAdminBoundaries = loadAdminBoundaries.bind(this);
    this.loadRegionCases = loadRegionCases.bind(this);
    this.loadFarms = loadFarms.bind(this);
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

  getColorByCaseCount(caseCount) {
    console.log("getColorByCaseCount called with:", caseCount);
    if (caseCount > 100000) return "#FF0000";
    if (caseCount > 50000) return "#FF4500";
    if (caseCount > 20000) return "#FFA500";
    if (caseCount > 10000) return "#FFFF00";
    if (caseCount > 0) return "#90EE90";
    return "transparent";
  }

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

  toggleBoundaries() {
    const boundaryBtn = document.getElementById("boundaryBtn");
    if (!boundaryBtn) {
      console.warn("Boundary button not found in the DOM");
      return;
    }

    const spanElement = boundaryBtn.querySelector("span");
    if (!spanElement) {
      console.warn("Span element not found in boundary button");
      return;
    }

    if (this.boundaryLayer && this.map.hasLayer(this.boundaryLayer)) {
      this.map.removeLayer(this.boundaryLayer);
      spanElement.textContent = "Hiện ranh giới";
    } else {
      if (this.boundaryLayer) {
        this.boundaryLayer.addTo(this.map);
      } else {
        this.loadAdminBoundaries();
      }
      spanElement.textContent = "Ẩn ranh giới";
    }
  }

  setupRealtimeUpdates() {
    setInterval(() => {
      this.loadMarkers();
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
