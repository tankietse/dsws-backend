class MapInitializer {
  constructor() {
    this.map = null;
    this.mapLayers = null;
    this.mapControls = null;
    this.initialized = false;
  }

  async init() {
    try {
      if (this.initialized) return;

      console.log("Starting map initialization...");

      // Show loading indicator
      const loadingEl = document.getElementById("map-loading");
      if (loadingEl) loadingEl.style.display = "flex";

      // Create map container first
      console.log("Creating map instance...");
      this.map = L.map("map", {
        attributionControl: false,
        ...MAP_CONFIG,
        layers: [], // Start with empty layers
      });

      console.log("Map instance created.");

      // Create custom panes with specific z-indices
      console.log("Creating custom panes...");
      this.map.createPane("basemap");
      this.map.getPane("basemap").style.zIndex = 200;

      this.map.createPane("boundaries");
      this.map.getPane("boundaries").style.zIndex = 250; // Adjusted z-index

      this.map.createPane("regions");
      this.map.getPane("regions").style.zIndex = 300;

      this.map.createPane("points");
      this.map.getPane("points").style.zIndex = 400;

      // Set map options
      this.map.options.preferCanvas = true;

      // Wait for map to be ready
      await new Promise((resolve) => {
        console.log("Waiting for map to be ready...");
        this.map.whenReady(() => {
          console.log("Map is ready.");
          resolve();
        });
      });

      // Set initial view after map is ready
      this.map.setView(MAP_CONFIG.center, MAP_CONFIG.zoom);
      console.log("Initial view set.");

      // Only use ArcGIS basemap
      try {
        console.log("Adding ArcGIS basemap...");
        const baseLayer = L.esri.Vector.vectorBasemapLayer(
          MAP_CONFIG.basemapEnum,
          {
            apiKey: MAP_CONFIG.accessToken,
            pane: "basemap", // Assign to basemap pane
          }
        );

        baseLayer.addTo(this.map);
        console.log("Basemap layer added to the map.");

        // Proceed without waiting for the 'load' event
        console.log("Initializing MapLayers and MapControls...");
        this.mapLayers = new MapLayers(this.map);
        this.mapControls = new MapControls(this.mapLayers);

        console.log("Loading initial layers...");
        await Promise.all([
          this.mapLayers.loadAdminBoundaries(),
          this.mapLayers.loadMarkers(),
        ]);
      } catch (error) {
        console.error("Critical error loading ArcGIS basemap:", error);
        throw new Error("Unable to initialize map without basemap");
      }

      this.initialized = true;
      console.log("Map fully initialized");

      // Hide loading indicator
      if (loadingEl) loadingEl.style.display = "none";
    } catch (error) {
      console.error("Error initializing map:", error);
      const loadingEl = document.getElementById("map-loading");
      if (loadingEl) {
        loadingEl.innerHTML = `
          <div class="text-red-500">
            <p>Lỗi tải bản đồ</p>
            <p class="text-sm">${error.message}</p>
          </div>
        `;
      }
    }
  }
}

// Initialize map when DOM is ready
let mapInit = null;

function initializeMap() {
  console.log("Initializing map...");
  if (!mapInit) {
    mapInit = new MapInitializer();
    mapInit.init().catch((err) => {
      console.error("Failed to initialize map:", err);
    });
    // Move the assignment here
    window.mapInit = mapInit;
  }
}

// Handle different load scenarios
if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initializeMap);
} else {
  initializeMap();
}
