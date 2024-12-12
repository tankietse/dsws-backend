document.addEventListener("DOMContentLoaded", () => {
  fetch("/api/v1/ca-benh/thong-ke")
    .then((response) => response.json())
    .then((data) => {
      renderCharts(data);
    })
    .catch((error) => {
      console.error("Error fetching statistics:", error);
    });
});

function renderCharts(data) {
  // Use a charting library like Chart.js to render statistics
  const ctxCases = document.getElementById("casesChart").getContext("2d");
  const casesChart = new Chart(ctxCases, {
    // ...chart configuration using data...
  });

  const ctxSpread = document.getElementById("spreadRateChart").getContext("2d");
  const spreadChart = new Chart(ctxSpread, {
    // ...chart configuration using data...
  });
}
