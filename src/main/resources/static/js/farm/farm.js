let currentPage = 1;
let pageSize = 10; // Default page size

// Ensure pageSize has a valid value
function validatePageSize() {
  const entriesElement = document.getElementById("entries");
  if (entriesElement) {
    pageSize = Number(entriesElement.value);
    if (!pageSize || isNaN(pageSize) || pageSize <= 0) {
      pageSize = 10; // Default page size
    }
  }
}

function updatePaginationControls(data) {
  document.getElementById("currentPage").textContent = currentPage;
  document.getElementById("totalPages").textContent = data.totalPages;

  const prevBtn = document.getElementById("prevBtn");
  const nextBtn = document.getElementById("nextBtn");

  prevBtn.classList.toggle("opacity-50", currentPage <= 1);
  prevBtn.disabled = currentPage <= 1;

  nextBtn.classList.toggle("opacity-50", currentPage >= data.totalPages);
  nextBtn.disabled = currentPage >= data.totalPages;
}

function loadTrangTraiData(page = 1, size = pageSize) {
  validatePageSize();
  const nameFilter = document.getElementById("nameFilter")?.value || "";
  const donViFilter = document.getElementById("donViFilter")?.value || "";
  const searchTerm = document.getElementById("searchInput")?.value || "";
  let url = `/api/v1/trang-trai/paged?page=${page - 1}&size=${size}`;

  if (nameFilter) {
    url += `&tenTrangTrai=${encodeURIComponent(nameFilter)}`;
  }
  if (donViFilter) {
    url += `&donViHanhChinh=${encodeURIComponent(donViFilter)}`;
  }
  if (searchTerm) {
    url += `&tenTrangTrai=${encodeURIComponent(searchTerm)}`;
  }

  fetch(url)
    .then((response) => {
      if (!response.ok) {
        throw new Error(`Server error: ${response.status}`);
      }
      return response.json();
    })
    .then((data) => {
      const tableBody = document.getElementById("trangTraiTableBody");
      tableBody.innerHTML = "";

      data.content.forEach((trangTrai) => {
        const row = document.createElement("tr");
        row.innerHTML = `
                  <td class="border px-4 py-2">${
                    trangTrai.maTrangTrai || ""
                  }</td>
                  <td class="border px-4 py-2">${
                    trangTrai.tenTrangTrai || ""
                  }</td>
                  <td class="border px-4 py-2">${trangTrai.tenChu || ""}</td>
                  <td class="border px-4 py-2">
                    ${trangTrai.soDienThoai || ""}<br>
                    ${trangTrai.email || ""}
                  </td>
                  <td class="border px-4 py-2">${
                    trangTrai.diaChiDayDu || ""
                  }</td>
                  <td class="border px-4 py-2">${trangTrai.dienTich || ""}</td>
                  <td class="border px-4 py-2">${trangTrai.tongDan || ""}</td>
                  <td class="border px-4 py-2">
                    ${
                      trangTrai.trangTraiVatNuois
                        ?.map((vn) => vn.loaiVatNuoi?.tenLoai)
                        .join(", ") || ""
                    }
                  </td>
                  <td class="border px-4 py-2">
                    <span class="px-2 py-1 rounded ${
                      trangTrai.trangThaiHoatDong
                        ? "bg-green-100 text-green-800"
                        : "bg-red-100 text-red-800"
                    }">
                      ${
                        trangTrai.trangThaiHoatDong
                          ? "Hoạt động"
                          : "Ngừng hoạt động"
                      }
                    </span>
                  </td>
                `;
        tableBody.appendChild(row);
      });

      updatePaginationControls(data);
    })
    .catch((error) => console.error("Error:", error));
}

// Ensure elements exist before adding event listeners
const entriesElement = document.getElementById("entries");
const nameFilterElement = document.getElementById("nameFilter");
const donViFilterElement = document.getElementById("donViFilter");
const searchInputElement = document.getElementById("searchInput");
const prevBtnElement = document.getElementById("prevBtn");
const nextBtnElement = document.getElementById("nextBtn");

if (entriesElement) {
  entriesElement.addEventListener("change", function (e) {
    pageSize = Number(e.target.value);
    currentPage = 1;
    loadTrangTraiData(currentPage, pageSize);
  });
}

if (nameFilterElement) {
  nameFilterElement.addEventListener("input", function () {
    currentPage = 1;
    loadTrangTraiData(currentPage, pageSize);
  });
}

if (donViFilterElement) {
  donViFilterElement.addEventListener("change", function () {
    currentPage = 1;
    loadTrangTraiData(currentPage, pageSize);
  });
}

if (searchInputElement) {
  searchInputElement.addEventListener("input", function () {
    currentPage = 1;
    loadTrangTraiData(currentPage, pageSize);
  });
}

if (prevBtnElement) {
  prevBtnElement.addEventListener("click", function (e) {
    e.preventDefault();
    if (currentPage > 1) {
      currentPage--;
      loadTrangTraiData(currentPage, pageSize);
    }
  });
}

if (nextBtnElement) {
  nextBtnElement.addEventListener("click", function (e) {
    e.preventDefault();
    const totalPages = Number(
      document.getElementById("totalPages").textContent
    );
    if (currentPage < totalPages) {
      currentPage++;
      loadTrangTraiData(currentPage, pageSize);
    }
  });
}

// Add new function to load administrative units
function loadDonViHanhChinh() {
  fetch("/api/v1/don-vi-hanh-chinh")
    .then((response) => response.json())
    .then((data) => {
      const donViFilter = document.getElementById("donViFilter");
      if (donViFilter) {
        // Keep the "Tất cả" option
        donViFilter.innerHTML = '<option value="">Tất cả</option>';

        // Add options for each administrative unit
        data.forEach((donVi) => {
          const option = document.createElement("option");
          option.value = donVi.id;
          option.textContent = donVi.ten;
          donViFilter.appendChild(option);
        });
      }
    })
    .catch((error) =>
      console.error("Error loading administrative units:", error)
    );
}

// Modify initial load to include administrative units
function initializePage() {
  loadDonViHanhChinh();
  loadTrangTraiData(currentPage, pageSize);
}

// Replace the existing initial load call with initializePage
initializePage();

// Modal functionality
const addFarmBtn = document.getElementById("addFarmBtn");
const addFarmModal = document.getElementById("addFarmModal");
const closeModalBtn = document.getElementById("closeModalBtn");

addFarmBtn.addEventListener("click", () => {
  addFarmModal.classList.remove("hidden");
});

closeModalBtn.addEventListener("click", () => {
  addFarmModal.classList.add("hidden");
});

// Handle form submission
document
  .getElementById("addFarmForm")
  .addEventListener("submit", function (event) {
    event.preventDefault();
    const formData = new FormData(this);

    fetch("/api/v1/trang-trai", {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
      body: JSON.stringify(Object.fromEntries(formData)),
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Lỗi khi thêm trang trại");
        }
        return response.json();
      })
      .then((data) => {
        // Close the modal
        addFarmModal.classList.add("hidden");
        // Reload the data or update the table
        loadTrangTraiData(currentPage, pageSize);
      })
      .catch((error) => {
        console.error("Error:", error);
        // Display error message if needed
      });
  });

// Load administrative units for cascading selects
function loadTinhThanh() {
  fetch("/api/v1/don-vi-hanh-chinh/cap/tinh")
    .then((response) => response.json())
    .then((data) => {
      const select = document.getElementById("tinhThanh");
      select.innerHTML = '<option value="">Chọn Tỉnh/Thành phố</option>';
      data.forEach((tinh) => {
        select.innerHTML += `<option value="${tinh.id}">${tinh.ten}</option>`;
      });
    });
}

function loadQuanHuyen(tinhId) {
  const quanHuyenSelect = document.getElementById("quanHuyen");
  const phuongXaSelect = document.getElementById("phuongXa");
  
  quanHuyenSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';
  phuongXaSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
  
  if (!tinhId) {
    quanHuyenSelect.disabled = true;
    phuongXaSelect.disabled = true;
    return;
  }

  fetch(`/api/v1/don-vi-hanh-chinh/parent/${tinhId}`)
    .then((response) => response.json())
    .then((data) => {
      quanHuyenSelect.disabled = false;
      data.forEach((huyen) => {
        quanHuyenSelect.innerHTML += `<option value="${huyen.id}">${huyen.ten}</option>`;
      });
    });
}

function loadPhuongXa(huyenId) {
  const phuongXaSelect = document.getElementById("phuongXa");
  phuongXaSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
  
  if (!huyenId) {
    phuongXaSelect.disabled = true;
    return;
  }

  fetch(`/api/v1/don-vi-hanh-chinh/parent/${huyenId}`)
    .then((response) => response.json())
    .then((data) => {
      phuongXaSelect.disabled = false;
      data.forEach((xa) => {
        phuongXaSelect.innerHTML += `<option value="${xa.id}">${xa.ten}</option>`;
      });
    });
}

// Add event listeners for cascading selects
document.getElementById("tinhThanh").addEventListener("change", function() {
  loadQuanHuyen(this.value);
  document.getElementById("donViHanhChinhId").value = this.value;
});

document.getElementById("quanHuyen").addEventListener("change", function() {
  loadPhuongXa(this.value);
  document.getElementById("donViHanhChinhId").value = this.value;
});

document.getElementById("phuongXa").addEventListener("change", function() {
  document.getElementById("donViHanhChinhId").value = this.value;
});

// Initialize tỉnh/thành select on modal open
document.getElementById("addFarmBtn").addEventListener("click", function() {
  loadTinhThanh();
});
