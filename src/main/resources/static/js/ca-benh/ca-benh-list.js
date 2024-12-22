document.addEventListener("DOMContentLoaded", () => {
  initializeDashboard();
});

async function initializeDashboard() {
  try {
    loadLoaiVatNuoi();
    setupFilters();
    await loadCaBenhData();
    await loadDashboardStats();
    setupCharts();
  } catch (err) {
    console.error("Error initializing dashboard:", err);
    showError("Có lỗi xảy ra khi tải dữ liệu");
  }
}

async function loadCaBenhData(page = 0, filters = {}) {
  try {
    const queryParams = new URLSearchParams({
      page: page,
      size: 10,
      ...filters,
    });

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
  if (!tbody) return;

  tbody.innerHTML = caBenhs
    .map(
      (caBenh) => `
        <tr class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap">${caBenh.id}</td>
            <td class="px-6 py-4">${caBenh.benh?.tenBenh || ""}</td>
            <td class="px-6 py-4">${
              caBenh.vatNuoi?.loaiVatNuoi?.tenLoai || ""
            }</td>
            <td class="px-6 py-4">${getMucDoLabel(caBenh.mucDo)}</td>
            <td class="px-6 py-4">
                <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getTrangThaiStyle(
                  caBenh.trangThai
                )}">
                    ${getTrangThaiLabel(caBenh.trangThai)}
                </span>
            </td>
            <td class="px-6 py-4">${formatDate(caBenh.ngayBatDau)}</td>
            <td class="px-6 py-4 text-right text-sm font-medium">
                <button onclick="viewDetails(${
                  caBenh.id
                })" class="text-blue-600 hover:text-blue-900 mr-2">
                    <i class="fas fa-eye"></i>
                </button>
                <button onclick="editCaBenh(${
                  caBenh.id
                })" class="text-yellow-600 hover:text-yellow-900 mr-2">
                    <i class="fas fa-edit"></i>
                </button>
                <button onclick="deleteCaBenh(${
                  caBenh.id
                })" class="text-red-600 hover:text-red-900">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
    `
    )
    .join("");
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
  fetch(`/api/v1/ca-benh?page=${page}`)
    .then((res) => res.json())
    .then((pageData) => {
      renderTableRows(pageData.content || []);
      renderPagination(pageData.totalPages, pageData.number);
    })
    .catch((err) => {
      console.error(err);
    });
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

function setupFilters() {
  const searchInput = document.getElementById("searchInput");
  const loaiVatNuoiFilter = document.getElementById("loaiVatNuoiFilter");
  let debounceTimer;

  searchInput.addEventListener("input", (e) => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      fetch(
        `/api/v1/ca-benh?keyword=${e.target.value}&loaiVatNuoiId=${loaiVatNuoiFilter.value}`
      )
        .then((res) => res.json())
        .then((pageData) => {
          renderTableRows(pageData.content || []);
          renderPagination(pageData.totalPages, pageData.number);
        })
        .catch((err) => {
          console.error(err);
        });
    }, 300);
  });

  loaiVatNuoiFilter.addEventListener("change", (e) => {
    fetch(
      `/api/v1/ca-benh?keyword=${searchInput.value}&loaiVatNuoiId=${e.target.value}`
    )
      .then((res) => res.json())
      .then((pageData) => {
        renderTableRows(pageData.content || []);
        renderPagination(pageData.totalPages, pageData.number);
      })
      .catch((err) => {
        console.error(err);
      });
  });
}

async function loadDashboardStats() {
  try {
    const response = await fetch("/api/v1/ca-benh/thong-ke");
    const stats = await response.json();

    // Update dashboard statistics
    document.getElementById("totalCases").textContent = stats.totalCases || 0;
    document.getElementById("activeCases").textContent = stats.activeCases || 0;
    document.getElementById("recoveredCases").textContent =
      stats.recoveredCases || 0;
    document.getElementById("severeCases").textContent = stats.severeCases || 0;
  } catch (err) {
    console.error("Error loading dashboard stats:", err);
  }
}

function updatePagination(data) {
  const totalPages = data.totalPages;
  const currentPage = data.number;
  const totalElements = data.totalElements;
  const size = data.size;

  // Update record count display
  const fromRecord = currentPage * size + 1;
  const toRecord = Math.min((currentPage + 1) * size, totalElements);

  document.getElementById("fromRecord").textContent = fromRecord;
  document.getElementById("toRecord").textContent = toRecord;
  document.getElementById("totalRecords").textContent = totalElements;

  // Update pagination buttons
  const paginationDesktop = document.getElementById("paginationDesktop");
  let paginationHtml = "";

  // Previous button
  paginationHtml += `
        <button onclick="changePage(${currentPage - 1})" 
                class="relative inline-flex items-center px-2 py-2 rounded-l-md border ${
                  currentPage === 0
                    ? "bg-gray-100 cursor-not-allowed"
                    : "bg-white hover:bg-gray-50"
                }"
                ${currentPage === 0 ? "disabled" : ""}>
            <span class="sr-only">Previous</span>
            <i class="fas fa-chevron-left"></i>
        </button>
    `;

  // Page numbers
  for (let i = 0; i < totalPages; i++) {
    paginationHtml += `
            <button onclick="changePage(${i})"
                    class="relative inline-flex items-center px-4 py-2 border ${
                      i === currentPage
                        ? "bg-blue-50 text-blue-600"
                        : "bg-white hover:bg-gray-50"
                    }">
                ${i + 1}
            </button>
        `;
  }

  // Next button
  paginationHtml += `
        <button onclick="changePage(${currentPage + 1})"
                class="relative inline-flex items-center px-2 py-2 rounded-r-md border ${
                  currentPage >= totalPages - 1
                    ? "bg-gray-100 cursor-not-allowed"
                    : "bg-white hover:bg-gray-50"
                }"
                ${currentPage >= totalPages - 1 ? "disabled" : ""}>
            <span class="sr-only">Next</span>
            <i class="fas fa-chevron-right"></i>
        </button>
    `;

  paginationDesktop.innerHTML = paginationHtml;

  // Update mobile pagination
  document.getElementById("prevPageMobile").disabled = currentPage === 0;
  document.getElementById("nextPageMobile").disabled =
    currentPage >= totalPages - 1;
}

function setupCharts() {
  setupDiseaseDistributionChart();
  setupCaseTrendChart();
}

async function setupDiseaseDistributionChart() {
  try {
    const response = await fetch("/api/v1/ca-benh/thong-ke");
    const data = await response.json();

    const ctx = document
      .getElementById("diseaseDistributionChart")
      .getContext("2d");
    new Chart(ctx, {
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
    new Chart(ctx, {
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
