let currentFilters = {
  page: 0,
  size: 10,
  keyword: "",
  loaiVatNuoiId: "",
  trangThai: "",
};

let statisticsData = null;

document.addEventListener("DOMContentLoaded", () => {
  initializeDashboard();
  setupEventListeners();
});

async function initializeDashboard() {
  try {
    await Promise.all([loadLoaiVatNuoi(), loadStatistics(), loadCaBenhData()]);
    setupCharts();
  } catch (err) {
    console.error("Error initializing dashboard:", err);
    showError("Có lỗi xảy ra khi tải dữ liệu");
  }
}

function setupEventListeners() {
  // Search input with debounce
  const searchInput = document.getElementById("searchInput");
  if (searchInput) {
    let debounceTimer;
    searchInput.addEventListener("input", (e) => {
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => {
        currentFilters.keyword = e.target.value;
        currentFilters.page = 0;
        loadCaBenhData();
      }, 300);
    });
  }

  // Filters
  const loaiVatNuoiFilter = document.getElementById("loaiVatNuoiFilter");
  if (loaiVatNuoiFilter) {
    loaiVatNuoiFilter.addEventListener("change", (e) => {
      currentFilters.loaiVatNuoiId = e.target.value;
      currentFilters.page = 0;
      loadCaBenhData();
    });
  }

  const trangThaiFilter = document.getElementById("trangThaiFilter");
  if (trangThaiFilter) {
    trangThaiFilter.addEventListener("change", (e) => {
      currentFilters.trangThai = e.target.value;
      currentFilters.page = 0;
      loadCaBenhData();
    });
  }

  // Page size
  const pageSizeSelect = document.getElementById("pageSizeSelect");
  if (pageSizeSelect) {
    pageSizeSelect.addEventListener("change", (e) => {
      currentFilters.size = parseInt(e.target.value);
      currentFilters.page = 0;
      loadCaBenhData();
    });
  }

  // Add case button
  const btnAddCase = document.getElementById("btnAddCase");
  if (btnAddCase) {
    btnAddCase.addEventListener("click", () => {
      window.location.href = "/ca-benh/create";
    });
  }

  // Add chart toggle handlers
  document.querySelectorAll(".toggle-chart").forEach((button) => {
    button.addEventListener("click", function () {
      const targetId = this.dataset.target;
      const container = document.getElementById(targetId).parentElement;
      const icon = this.querySelector("i");

      if (container.style.display === "none") {
        container.style.display = "block";
        icon.classList.remove("fa-chevron-down");
        icon.classList.add("fa-chevron-up");
      } else {
        container.style.display = "none";
        icon.classList.remove("fa-chevron-up");
        icon.classList.add("fa-chevron-down");
      }
    });
  });
}

async function loadStatistics() {
  try {
    const response = await fetch("/api/v1/ca-benh/thong-ke");
    statisticsData = await response.json();
    updateDashboardStats(statisticsData);
    if (diseaseDistributionChart && caseTrendChart) {
      updateCharts(statisticsData);
    }
  } catch (err) {
    console.error("Error loading statistics:", err);
  }
}

async function loadCaBenhData() {
  try {
    const queryParams = new URLSearchParams(currentFilters);

    const response = await fetch(`/api/v1/ca-benh?${queryParams}`);
    const data = await response.json();

    renderTableRows(data.content);
    updatePagination(data);
    return data;
  } catch (err) {
    console.error("Error loading ca benh data:", err);
    showError("Không thể tải danh sách ca bệnh");
  }
}

function renderTableRows(caBenhs) {
  const tbody = document.getElementById("caBenhTableBody");
  const emptyState = document.getElementById("emptyState");

  if (!tbody) return;

  if (!caBenhs || caBenhs.length === 0) {
    tbody.innerHTML = "";
    emptyState?.classList.remove("hidden");
    return;
  }

  emptyState?.classList.add("hidden");
  tbody.innerHTML = caBenhs
    .map(
      (caBenh) => `
    <tr class="hover:bg-gray-50 transition-colors group cursor-pointer" data-id="${
      caBenh.id
    }">
      <td class="px-4 py-4 align-top">
        <div class="flex items-start space-x-3">
          <div class="flex-shrink-0 w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center">
            <i class="fas fa-virus text-blue-600"></i>
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-gray-900 truncate">
              ${caBenh.benh?.tenBenh || "N/A"}
            </p>
            <p class="text-xs text-gray-500 mt-1">Mã: #${caBenh.id}</p>
            ${
              caBenh.benh?.mucDoBenhs
                ? `
              <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium mt-1 ${getMucDoStyle(
                caBenh.benh.mucDoBenhs[0]
              )}">
                ${getMucDoLabel(caBenh.benh.mucDoBenhs[0])}
              </span>
            `
                : ""
            }
          </div>
        </div>
      </td>
      <td class="px-4 py-4 align-top">
        <div class="space-y-1">
          <p class="text-sm font-medium text-gray-900">${
            caBenh.trangTrai?.tenTrangTrai || "N/A"
          }</p>
          <p class="text-xs text-gray-500 max-w-xs truncate">${
            caBenh.trangTrai?.diaChiDayDu || ""
          }</p>
          <div class="flex flex-wrap gap-1">
            ${
              caBenh.trangTrai?.trangTraiVatNuois
                ?.map(
                  (ttv) => `
              <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800">
                ${ttv.loaiVatNuoi.tenLoai}: ${ttv.soLuong}
              </span>
            `
                )
                .join("") || ""
            }
          </div>
        </div>
      </td>
      <td class="px-4 py-4 align-top">
        <div class="space-y-2">
          <div class="flex items-center justify-between">
            <span class="text-xs text-gray-500">Tổng ca:</span>
            <span class="text-sm font-medium">${
              caBenh.soCaNhiemBanDau || 0
            }</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-xs text-gray-500">Tử vong:</span>
            <span class="text-sm font-medium text-red-600">${
              caBenh.soCaTuVongBanDau || 0
            }</span>
          </div>
        </div>
      </td>
      <td class="px-4 py-4 align-top">
        <div class="space-y-2">
          <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getTrangThaiStyle(
            caBenh.trangThai
          )}">
            ${getTrangThaiLabel(caBenh.trangThai)}
          </span>
          <div class="text-xs text-gray-500">
            <p>Phát hiện: ${formatDate(caBenh.ngayPhatHien)}</p>
            <p>Cập nhật: ${formatDate(caBenh.ngayTao)}</p>
          </div>
        </div>
      </td>
      <td class="px-4 py-4 align-top text-right">
        <div class="opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-end space-x-2">
          <button onclick="viewDetails(${
            caBenh.id
          }, event)" class="text-blue-600 hover:text-blue-900" title="Xem chi tiết">
            <i class="fas fa-eye"></i>
          </button>
          <button onclick="editCaBenh(${
            caBenh.id
          }, event)" class="text-yellow-600 hover:text-yellow-900" title="Chỉnh sửa">
            <i class="fas fa-edit"></i>
          </button>
          ${
            caBenh.trangThai === "PENDING"
              ? `
            <button onclick="approveCaBenh(${caBenh.id}, event)" class="text-green-600 hover:text-green-900" title="Duyệt">
              <i class="fas fa-check"></i>
            </button>
          `
              : ""
          }
          <button onclick="deleteCaBenh(${
            caBenh.id
          }, event)" class="text-red-600 hover:text-red-900" title="Xóa">
            <i class="fas fa-trash"></i>
          </button>
        </div>
      </td>
    </tr>
  `
    )
    .join("");

  // Row click handler
  tbody.querySelectorAll("tr").forEach((row) => {
    row.addEventListener("click", function (e) {
      if (!e.target.closest("button")) {
        viewDetails(this.dataset.id);
      }
    });
  });
}

function getMucDoLabel(mucDo) {
  const labels = {
    NANG: '<span class="text-red-600">Nặng</span>',
    TRUNG_BINH: '<span class="text-yellow-600">Trung bình</span>',
    NHE: '<span class="text-green-600">Nhẹ</span>',
  };
  return labels[mucDo] || mucDo;
}

function getTrangThaiStyle(trangThai) {
  const styles = {
    DANG_DIEU_TRI: "bg-yellow-100 text-yellow-800",
    DA_KHOI: "bg-green-100 text-green-800",
    NGHI_NGO: "bg-gray-100 text-gray-800",
  };
  return styles[trangThai] || "bg-gray-100 text-gray-800";
}

function getTrangThaiLabel(trangThai) {
  const labels = {
    DANG_DIEU_TRI: "Đang điều trị",
    DA_KHOI: "Đã khỏi",
    NGHI_NGO: "Nghi ngờ",
  };
  return labels[trangThai] || trangThai;
}

function formatDate(dateString) {
  if (!dateString) return "";
  return new Date(dateString).toLocaleDateString("vi-VN");
}

// Add these utility functions for better error handling and user feedback
function showError(message) {
  // Implement your error notification here
  alert(message);
}

function showSuccess(message) {
  // Implement your success notification here
  alert(message);
}

function renderPagination(totalPages, currentPage) {
  const pagination = document.getElementById("pagination");
  let paginationHtml = "";

  for (let i = 0; i < totalPages; i++) {
    paginationHtml += `
      <button 
        onclick="changePage(${i})" 
        class="${
          i === currentPage
            ? "bg-blue-500 text-white"
            : "bg-gray-200 text-gray-700"
        } 
        px-3 py-1 rounded hover:bg-blue-600 hover:text-white">
        ${i + 1}
      </button>
    `;
  }

  pagination.innerHTML = paginationHtml;
}

function changePage(page) {
  if (page < 0) return;
  currentFilters.page = page;
  loadCaBenhData();
}

function loadLoaiVatNuoi() {
  fetch("/api/v1/loai-vat-nuoi")
    .then((res) => res.json())
    .then((data) => {
      const select = document.getElementById("loaiVatNuoiFilter");
      data.forEach((loai) => {
        const option = document.createElement("option");
        option.value = loai.id;
        option.textContent = loai.tenLoai;
        select.appendChild(option);
      });
    })
    .catch((err) => {
      console.error(err);
    });
}

async function loadDashboardStats() {
  try {
    const response = await fetch("/api/v1/ca-benh/thong-ke");
    const stats = await response.json();

    // Update general stats
    document.getElementById("totalCases").textContent =
      stats.generalStats.totalCases;
    document.getElementById("activeCases").textContent =
      stats.generalStats.activeCases;
    document.getElementById("recoveredCases").textContent =
      stats.generalStats.recoveredCases;
    document.getElementById("severeCases").textContent =
      stats.generalStats.severeCases;

    // Update monthly growth rate if exists
    const growthRateElement = document.querySelector("#totalCases + p span");
    if (
      growthRateElement &&
      stats.generalStats.monthlyGrowthRate !== undefined
    ) {
      const growthRate = stats.generalStats.monthlyGrowthRate;
      growthRateElement.textContent = `${
        growthRate > 0 ? "+" : ""
      }${growthRate}%`;
      growthRateElement.className =
        growthRate > 0 ? "text-green-500" : "text-red-500";
    }

    // Directly update charts without calling separate update functions
    if (diseaseDistributionChart) {
      diseaseDistributionChart.data.labels =
        stats.diseaseDistribution?.map((d) => d.name) || [];
      diseaseDistributionChart.data.datasets[0].data =
        stats.diseaseDistribution?.map((d) => d.count) || [];
      diseaseDistributionChart.update();
    }

    if (caseTrendChart) {
      caseTrendChart.data.labels = stats.caseTrend?.map((d) => d.date) || [];
      caseTrendChart.data.datasets[0].data =
        stats.caseTrend?.map((d) => d.count) || [];
      caseTrendChart.update();
    }
  } catch (err) {
    console.error("Error loading dashboard stats:", err);
  }
}

// Thêm biến để lưu trữ instance của chart
let diseaseDistributionChart = null;
let caseTrendChart = null;

async function setupDiseaseDistributionChart() {
  try {
    const response = await fetch("/api/v1/ca-benh/thong-ke");
    const data = await response.json();

    const ctx = document
      .getElementById("diseaseDistributionChart")
      .getContext("2d");

    // Hủy chart cũ nếu tồn tại
    if (diseaseDistributionChart) {
      diseaseDistributionChart.destroy();
    }

    diseaseDistributionChart = new Chart(ctx, {
      type: "pie",
      data: {
        labels: data.diseaseDistribution?.map((d) => d.name) || [],
        datasets: [
          {
            data: data.diseaseDistribution?.map((d) => d.count) || [],
            backgroundColor: [
              "#4F46E5",
              "#10B981",
              "#F59E0B",
              "#EF4444",
              "#8B5CF6",
              "#EC4899",
              "#6366F1",
            ],
          },
        ],
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            position: "right",
          },
          title: {
            display: true,
            text: "Phân bố theo loại bệnh",
          },
        },
      },
    });
  } catch (err) {
    console.error("Error setting up disease distribution chart:", err);
  }
}

async function setupCaseTrendChart() {
  try {
    const response = await fetch("/api/v1/ca-benh/thong-ke");
    const data = await response.json();

    const ctx = document.getElementById("caseTrendChart").getContext("2d");

    // Hủy chart cũ nếu tồn tại
    if (caseTrendChart) {
      caseTrendChart.destroy();
    }

    caseTrendChart = new Chart(ctx, {
      type: "line",
      data: {
        labels: data.caseTrend?.map((d) => d.date) || [],
        datasets: [
          {
            label: "Số ca bệnh",
            data: data.caseTrend?.map((d) => d.count) || [],
            borderColor: "#4F46E5",
            tension: 0.1,
          },
        ],
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            position: "top",
          },
          title: {
            display: true,
            text: "Xu hướng ca bệnh theo thời gian",
          },
        },
        scales: {
          y: {
            beginAtZero: true,
          },
        },
      },
    });
  } catch (err) {
    console.error("Error setting up case trend chart:", err);
  }
}

function updatePagination(data) {
  const controls = document.getElementById("paginationControls");
  if (!controls) return;

  const totalPages = Math.ceil(data.totalElements / data.size);
  const currentPage = data.number;

  let html = "";

  // Previous button
  html += `
    <button 
      onclick="changePage(${currentPage - 1})"
      class="px-3 py-1 rounded-md ${
        currentPage <= 0
          ? "bg-gray-100 cursor-not-allowed"
          : "bg-white hover:bg-gray-50"
      }"
      ${currentPage <= 0 ? "disabled" : ""}>
      <i class="fas fa-chevron-left"></i>
    </button>
  `;

  // Page numbers
  const startPage = Math.max(0, currentPage - 2);
  const endPage = Math.min(totalPages - 1, currentPage + 2);

  for (let i = startPage; i <= endPage; i++) {
    html += `
      <button 
        onclick="changePage(${i})"
        class="px-3 py-1 rounded-md ${
          i === currentPage
            ? "bg-blue-500 text-white"
            : "bg-white hover:bg-gray-50"
        }">
        ${i + 1}
      </button>
    `;
  }

  // Next button
  html += `
    <button 
      onclick="changePage(${currentPage + 1})"
      class="px-3 py-1 rounded-md ${
        currentPage >= totalPages - 1
          ? "bg-gray-100 cursor-not-allowed"
          : "bg-white hover:bg-gray-50"
      }"
      ${currentPage >= totalPages - 1 ? "disabled" : ""}>
      <i class="fas fa-chevron-right"></i>
    </button>
  `;

  controls.innerHTML = html;

  // Update record info
  const fromRecord = document.getElementById("fromRecord");
  const toRecord = document.getElementById("toRecord");
  const totalRecords = document.getElementById("totalRecords");

  if (fromRecord)
    fromRecord.textContent = data.numberOfElements
      ? currentPage * data.size + 1
      : 0;
  if (toRecord)
    toRecord.textContent = Math.min(
      (currentPage + 1) * data.size,
      data.totalElements
    );
  if (totalRecords) totalRecords.textContent = data.totalElements;
}

function setupCharts() {
  setupDiseaseDistributionChart();
  setupCaseTrendChart();
}

function navigateToEdit(id) {
  window.location.href = `/ca-benh/edit?id=${id}`;
}

function editCaBenh(id, event) {
  if (event) {
    event.stopPropagation();
  }
  navigateToEdit(id);
}

function viewDetails(id) {
  event.stopPropagation();
  // Implement view details logic
}

function deleteCaBenh(id) {
  event.stopPropagation();
  if (confirm("Bạn có chắc chắn muốn xóa ca bệnh này?")) {
    // Implement delete logic
  }
}

// Add new style helpers
function getMucDoStyle(mucDo) {
  const styles = {
    NANG: "bg-red-100 text-red-800",
    TRUNG_BINH: "bg-yellow-100 text-yellow-800",
    NHE: "bg-green-100 text-green-800",
  };
  return styles[mucDo] || "bg-gray-100 text-gray-800";
}

function updateDashboardStats(stats) {
  // Update general stats
  const elements = {
    totalCases: document.getElementById("totalCases"),
    activeCases: document.getElementById("activeCases"),
    recoveredCases: document.getElementById("recoveredCases"),
    severeCases: document.getElementById("severeCases"),
  };

  if (stats.generalStats) {
    Object.keys(elements).forEach((key) => {
      if (elements[key]) {
        elements[key].textContent = stats.generalStats[key] || 0;
      }
    });
  }

  // Update status distribution
  updateStatusDistribution(stats.statusDistribution);

  // Update monthly comparison
  updateMonthlyComparison(stats.monthlyComparison);

  // Update top farms
  updateTopFarms(stats.topFarms);
}

function updateStatusDistribution(distribution) {
  const container = document.getElementById("statusDistribution");
  if (!container || !distribution) return;

  const statusColors = {
    PENDING: "bg-yellow-100 text-yellow-800",
    APPROVED: "bg-green-100 text-green-800",
    REJECTED: "bg-red-100 text-red-800",
  };

  container.innerHTML = Object.entries(distribution)
    .map(
      ([status, count]) => `
      <div class="flex items-center justify-between">
        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
          statusColors[status] || "bg-gray-100 text-gray-800"
        }">
          ${getTrangThaiLabel(status)}
        </span>
        <span class="text-sm font-medium text-gray-900">${count}</span>
      </div>
    `
    )
    .join("");
}

function updateMonthlyComparison(comparison) {
  const container = document.getElementById("monthlyComparison");
  if (!container || !comparison) return;

  const growthRate = comparison.growthRate;
  const growthClass = growthRate >= 0 ? "text-green-600" : "text-red-600";

  container.innerHTML = `
    <div class="grid grid-cols-2 gap-4">
      <div>
        <p class="text-sm text-gray-500">Tháng này</p>
        <p class="text-lg font-semibold">${comparison.currentMonth.count}</p>
        <p class="text-xs text-gray-500">Ca nhiễm: ${
          comparison.currentMonth.infected
        }</p>
      </div>
      <div>
        <p class="text-sm text-gray-500">Tháng trước</p>
        <p class="text-lg font-semibold">${comparison.previousMonth.count}</p>
        <p class="text-xs text-gray-500">Ca nhiễm: ${
          comparison.previousMonth.infected
        }</p>
      </div>
    </div>
    <div class="mt-2">
      <p class="text-sm font-medium ${growthClass}">
        ${growthRate >= 0 ? "↑" : "↓"} ${Math.abs(growthRate).toFixed(
    1
  )}% so với tháng trước
      </p>
    </div>
  `;
}

function updateTopFarms(farms) {
  const container = document.getElementById("topFarms");
  if (!container || !farms) return;

  container.innerHTML = farms
    .slice(0, 5)
    .map(
      (farm, index) => `
      <div class="flex items-center justify-between py-1">
        <div class="flex items-center">
          <span class="text-sm font-medium text-gray-900 mr-2">${
            index + 1
          }.</span>
          <div>
            <p class="text-sm font-medium text-gray-900">Trang trại của: ${
              farm.tenChu
            }</p>
            <p class="text-xs text-gray-500">Tổng: ${
              farm.totalCases
            } ca bệnh</p>
          </div>
        </div>
        <div class="text-right">
          <span class="text-xs font-medium ${
            farm.activeCases > 0 ? "text-red-600" : "text-green-600"
          }">
           Đang điều trị (${farm.activeCases}) 
          </span>
        </div>
      </div>
    `
    )
    .join("");
}

// Add new function for case approval
async function approveCaBenh(id, event) {
  event.stopPropagation();
  if (confirm("Bạn có chắc chắn muốn duyệt ca bệnh này?")) {
    try {
      const response = await fetch(
        `/api/v1/ca-benh/${id}/duyet?approved=true`,
        {
          method: "PUT",
        }
      );
      if (response.ok) {
        showSuccess("Duyệt ca bệnh thành công");
        loadCaBenhData();
      } else {
        showError("Có lỗi xảy ra khi duyệt ca bệnh");
      }
    } catch (err) {
      console.error("Error approving case:", err);
      showError("Có lỗi xảy ra khi duyệt ca bệnh");
    }
  }
}
