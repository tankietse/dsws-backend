const baseUrl = "/api/v1/benh";
const pageSize = 10;
let currentPage = 0;

// Fetch list of diseases with pagination and filters
async function fetchBenhList(page = 0, filters = {}) {
  try {
    const { keyword, loaiVatNuoiId } = filters;
    let url;

    // Use search endpoint if there's a keyword
    if (keyword && keyword.trim()) {
      url = `${baseUrl}/search?keyword=${encodeURIComponent(keyword)}`;
    }
    // Use animal type filter endpoint if specified
    else if (loaiVatNuoiId) {
      url = `${baseUrl}/by-loai-vat-nuoi/${loaiVatNuoiId}`;
    }
    // Default to paginated endpoint
    else {
      url = `${baseUrl}/page?page=${page}&size=${pageSize}`;
    }

    const response = await fetch(url, {
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();

    // Handle different response structures
    if (Array.isArray(data)) {
      // For search and animal type filter endpoints that return arrays
      renderBenhTable(data);
      // Set single page pagination since we have all results
      renderPagination(1, 0);
    } else {
      // For paginated endpoint
      renderBenhTable(data.content);
      renderPagination(data.totalPages, data.number);
    }
  } catch (error) {
    console.error("Error fetching benh list:", error);
    showError("Không thể tải danh sách bệnh");
  }
}

// Render table data
function renderBenhTable(benhs) {
  const tbody = document.querySelector("#benh-table tbody");
  tbody.innerHTML = benhs
    .map(
      (benh) => `
        <tr class="hover:bg-gray-50 cursor-pointer" onclick="window.location.href='/benh/edit/${
          benh.id
        }'">
            <td class="px-4 py-2 border">${benh.id}</td>
            <td class="px-4 py-2 border">${benh.tenBenh}</td>
            <td class="px-4 py-2 border">${benh.moTa || ""}</td>
            <td class="px-4 py-2 border">${benh.tacNhanGayBenh || ""}</td>
            <td class="px-4 py-2 border">${benh.trieuChung || ""}</td>
            <td class="px-4 py-2 border">${benh.thoiGianUBenh || ""}</td>
            <td class="px-4 py-2 border">${benh.phuongPhapChanDoan || ""}</td>
            <td class="px-4 py-2 border">${benh.bienPhapPhongNgua || ""}</td>
            <td class="px-4 py-2 border">
                <div class="flex gap-2" onclick="event.stopPropagation()">
                    <a href="/benh/edit/${benh.id}" 
                       class="bg-yellow-500 hover:bg-yellow-700 text-white font-bold py-1 px-2 rounded">
                        <i class="fas fa-edit"></i> Sửa
                    </a>
                    <button onclick="deleteBenh(${benh.id})" 
                            class="bg-red-500 hover:bg-red-700 text-white font-bold py-1 px-2 rounded">
                        <i class="fas fa-trash"></i> Xóa
                    </button>
                </div>
            </td>
        </tr>
    `
    )
    .join("");

  // Update counts
  document.getElementById("currentCount").textContent = benhs.length;

  // Update total count if available
  if (benhs.totalElements) {
    document.getElementById("totalCount").textContent = benhs.totalElements;
  }
}

// Render pagination
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

// Change page
function changePage(page) {
  currentPage = page;
  fetchBenhList(page, {
    keyword: document.getElementById("searchInput").value,
    loaiVatNuoiId: document.getElementById("loaiVatNuoiFilter").value,
  });
}

// Delete disease
async function deleteBenh(id) {
  if (!confirm("Bạn có chắc chắn muốn xóa bệnh này?")) return;

  try {
    const response = await fetch(`${baseUrl}/${id}`, {
      method: "DELETE",
    });

    if (response.ok) {
      fetchBenhList(currentPage);
    } else {
      throw new Error("Không thể xóa bệnh");
    }
  } catch (error) {
    console.error("Error deleting benh:", error);
    showError("Không thể xóa bệnh");
  }
}

// Setup search functionality
function setupSearch() {
  const searchInput = document.getElementById("searchInput");
  const clearButton = document.getElementById("clearSearch");

  let debounceTimer;

  searchInput.addEventListener("input", (e) => {
    const value = e.target.value;
    clearButton.style.display = value ? "block" : "none";

    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      fetchBenhList(0, {
        keyword: value,
        loaiVatNuoiId: document.getElementById("loaiVatNuoiFilter").value,
      });
    }, 300);
  });

  clearButton.addEventListener("click", () => {
    searchInput.value = "";
    clearButton.style.display = "none";
    fetchBenhList(0);
  });
}

// Show error message
function showError(message) {
  // Implement error notification here
  alert(message);
}

// Add new function to load animal types
async function loadLoaiVatNuoi() {
  try {
    const response = await fetch("/api/v1/loai-vat-nuoi");
    const data = await response.json();
    const select = document.getElementById("loaiVatNuoiFilter");

    data.forEach((loai) => {
      const option = document.createElement("option");
      option.value = loai.id;
      option.textContent = loai.tenLoai;
      select.appendChild(option);
    });
  } catch (error) {
    console.error("Error loading animal types:", error);
    showError("Không thể tải danh sách loại vật nuôi");
  }
}

// Add reset filters function
function resetFilters() {
  document.getElementById("searchInput").value = "";
  document.getElementById("loaiVatNuoiFilter").value = "";
  fetchBenhList(0);
}

// Update the initialization
document.addEventListener("DOMContentLoaded", () => {
  loadLoaiVatNuoi();
  setupFilters();
  fetchBenhList(0); // Always start from first page
});

// Setup filter handlers
function setupFilters() {
  const searchInput = document.getElementById("searchInput");
  const loaiVatNuoiFilter = document.getElementById("loaiVatNuoiFilter");
  let debounceTimer;

  // Search input handler
  searchInput.addEventListener("input", (e) => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      const filters = {
        keyword: e.target.value,
        loaiVatNuoiId: loaiVatNuoiFilter.value,
      };
      fetchBenhList(0, filters);
    }, 300);
  });

  // Animal type filter handler
  loaiVatNuoiFilter.addEventListener("change", (e) => {
    const filters = {
      keyword: searchInput.value,
      loaiVatNuoiId: e.target.value,
    };
    fetchBenhList(0, filters);
  });
}
