export default function loadAdminBoundaries(filterOptions = {}) {
  return new Promise((resolve, reject) => {
    // Build query parameters based on filterOptions
    let queryParams = '';
    if (filterOptions.capHanhChinh) {
      queryParams += `capHanhChinh=${filterOptions.capHanhChinh}`;
    }
    if (filterOptions.name) {
      queryParams += queryParams ? '&' : '';
      queryParams += `name=${encodeURIComponent(filterOptions.name)}`;
    }
    const url = `/api/v1/don-vi-hanh-chinh/geojson${queryParams ? '?' + queryParams : ''}`;

    fetch(url, {
      credentials: "include",
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Network response was not ok");
        }
        return response.json();
      })
      .then((data) => {
        if (!data || !data.features || data.features.length === 0) {
          console.warn("No boundary data available");
          return;
        }
        if (this.boundaryLayer) {
          this.map.removeLayer(this.boundaryLayer);
        }
        this.boundaryLayer = L.geoJSON(data, {
          pane: "boundaries",
          style: {
            color: "#003399",
            weight: 1,
            opacity: 0.3,
            fillOpacity: 0.1,
          },
          onEachFeature: function (feature, layer) {
            layer.bindPopup(feature.properties.ten);
          },
        });
        this.boundaryLayer.addTo(this.map);
        resolve();
      })
      .catch((error) => {
        console.error("Error loading boundaries:", error);
        if (error.status === 401) {
          window.location.href = "/auth/login";
        }
        reject(error);
      });
  });
}
