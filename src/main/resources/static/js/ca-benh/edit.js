document.addEventListener("DOMContentLoaded", function () {
  const urlParams = new URLSearchParams(window.location.search);
  const caBenhId = urlParams.get("id");

  if (!caBenhId) {
    showNotification("Không tìm thấy ID ca bệnh", "error");
    window.location.href = "/ca-benh/list";
    return;
  }

  document.getElementById("caBenhId").textContent = caBenhId;

  // Load ca benh data
  loadCaBenhData(caBenhId).catch((error) => {
    console.error("Error loading ca benh:", error);
    showNotification("Không thể tải thông tin ca bệnh", "error");
    window.location.href = "/ca-benh/list";
  });

  // Form submission
  const form = document.getElementById("editCaBenhForm");
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    await updateCaBenh(caBenhId);
  });

  // Cancel button
  document.getElementById("cancelButton").addEventListener("click", () => {
    window.location.href = "/ca-benh/list";
  });

  // Add status change listener
  const trangThaiSelect = document.getElementById("trangThai");
  if (trangThaiSelect) {
    trangThaiSelect.addEventListener("change", function() {
      updateStatusBadge(this.value);
    });
  }
});

async function loadCaBenhData(id) {
  try {
    const response = await fetch(`/api/v1/ca-benh/${id}`, {
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    populateForm(data);
  } catch (error) {
    console.error("Error:", error);
    throw error;
  }
}

function populateForm(data) {
  try {
    // Farm info
    if (data.trangTrai) {
      document.getElementById("tenChu").textContent =
        data.trangTrai.tenChu || "Không có thông tin";
      document.getElementById("tongDan").textContent =
        data.trangTrai.tongDan || "0";
      document.getElementById("diaChiDayDu").textContent =
        data.trangTrai.diaChiDayDu || "Không có địa chỉ";

      // Display animal types
      if (
        data.trangTrai.danhSachVatNuoi &&
        data.trangTrai.danhSachVatNuoi.length > 0
      ) {
        const vatNuoiList = data.trangTrai.danhSachVatNuoi
          .map((vn) => `${vn.loaiVatNuoi.tenLoai} (${vn.soLuong})`)
          .join(", ");
        document.getElementById("danhSachVatNuoi").textContent = vatNuoiList;
      } else {
        document.getElementById("danhSachVatNuoi").textContent =
          "Không có thông tin";
      }
    }

    // Disease info
    if (data.benh) {
      document.getElementById("tenBenh").textContent = data.benh.tenBenh;

      // Disease warning flags
      const warningFlags = [];
      if (data.benh.canCongBoDich) {
        warningFlags.push("Bệnh cần công bố dịch");
      }
      if (data.benh.canPhongBenhBatBuoc) {
        warningFlags.push("Bệnh cần phòng bắt buộc");
      }
      document.getElementById("warningFlags").textContent =
        warningFlags.join(" • ");

      // Display disease severity badges
      const mucDoBenhsContainer = document.getElementById("mucDoBenhs");
      mucDoBenhsContainer.innerHTML = "";
      if (data.benh.mucDoBenhs) {
        data.benh.mucDoBenhs.forEach((mucDo) => {
          const badge = document.createElement("span");
          badge.className = "px-2 py-1 text-xs font-medium rounded-full";

          switch (mucDo) {
            case "NGUY_HIEM":
              badge.className += " bg-red-100 text-red-800";
              badge.textContent = "Nguy hiểm";
              break;
            case "PHONG_BENH_BAT_BUOC":
              badge.className += " bg-yellow-100 text-yellow-800";
              badge.textContent = "Phòng bệnh bắt buộc";
              break;
            case "BANG_A":
              badge.className += " bg-blue-100 text-blue-800";
              badge.textContent = "Bảng A";
              break;
            default:
              badge.className += " bg-gray-100 text-gray-800";
              badge.textContent = mucDo;
          }

          mucDoBenhsContainer.appendChild(badge);
        });
      }
    }

    // Update status badge with proper null checks
    const statusBadge = document.getElementById("statusBadge");
    if (statusBadge && data.trangThai) {
      let badgeClass = "px-3 py-1 rounded-full text-sm font-medium ";
      let badgeText = "";

      switch (data.trangThai) {
        case "PENDING":
          badgeClass += "bg-yellow-100 text-yellow-800";
          badgeText = "Chờ duyệt";
          break;
        case "APPROVED":
          badgeClass += "bg-green-100 text-green-800";
          badgeText = "Đã duyệt";
          break;
        case "REJECTED":
          badgeClass += "bg-red-100 text-red-800";
          badgeText = "Từ chối";
          break;
        default:
          badgeClass += "bg-gray-100 text-gray-800";
          badgeText = "Không xác định";
      }

      statusBadge.className = badgeClass;
      statusBadge.textContent = badgeText;
    }

    // Fix date format handling for ngayPhatHien
    if (data.ngayPhatHien) {
      // Convert "2024-12-25 15:52:08" to "2024-12-25T15:52"
      const dateTime = data.ngayPhatHien.replace(" ", "T");
      const formattedDateTime = dateTime.substring(0, 16); // Take only up to minutes (YYYY-MM-DDTHH:mm)
      document.getElementById("ngayPhatHien").value = formattedDateTime;
    }

    document.getElementById("soCaNhiemBanDau").value =
      data.soCaNhiemBanDau || 0;
    document.getElementById("soCaTuVongBanDau").value =
      data.soCaTuVongBanDau || 0;
    document.getElementById("moTaBanDau").value = data.moTaBanDau || "";
    document.getElementById("nguyenNhanDuDoan").value =
      data.nguyenNhanDuDoan || "";

    if (data.trangThai) {
      document.getElementById("trangThai").value = data.trangThai;
    }

    // Update status
    const trangThaiSelect = document.getElementById("trangThai");
    if (trangThaiSelect && data.trangThai) {
      trangThaiSelect.value = data.trangThai;
      // Update status badge to match selected value
      updateStatusBadge(data.trangThai);
    }
  } catch (error) {
    console.error("Error in populateForm:", error);
    showNotification("Lỗi khi hiển thị thông tin ca bệnh", "error");
  }
}

async function updateCaBenh(id) {
  try {
    const ngayPhatHien = document.getElementById("ngayPhatHien").value;
    const formattedDate = ngayPhatHien.replace("T", " ") + ":00";

    const formData = {
      ngayPhatHien: formattedDate,
      soCaNhiemBanDau:
        parseInt(document.getElementById("soCaNhiemBanDau").value) || 0,
      soCaTuVongBanDau:
        parseInt(document.getElementById("soCaTuVongBanDau").value) || 0,
      moTaBanDau: document.getElementById("moTaBanDau").value.trim(),
      nguyenNhanDuDoan: document
        .getElementById("nguyenNhanDuDoan")
        .value.trim(),
      trangThai: document.getElementById("trangThai").value,
      // Add these to maintain references
      trangTraiId: id, // Add the trang trai ID
      benhId: null, // The benh ID should come from your data
    };

    const response = await fetch(`/api/v1/ca-benh/${id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
      credentials: "include",
      body: JSON.stringify(formData),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(
        errorData?.message || `HTTP error! status: ${response.status}`
      );
    }

    showNotification("Cập nhật ca bệnh thành công", "success");
    // Update redirection to /ca-benh/list
    window.location.href = "/ca-benh/list";
  } catch (error) {
    console.error("Error updating ca benh:", error);
    showNotification(
      error.message || "Không thể cập nhật ca bệnh. Vui lòng thử lại sau.",
      "error"
    );
  }
}

function showNotification(message, type) {
  alert(message); // Replace with your notification system
}

// Add a helper function to update the status badge
function updateStatusBadge(trangThai) {
  const statusBadge = document.getElementById("statusBadge");
  if (!statusBadge) return;

  let badgeClass = "flex items-center px-4 py-2 rounded-full text-sm font-medium ";
  let icon = '<i class="fas fa-circle-notch mr-2"></i>';
  let text = "";

  switch (trangThai) {
    case "PENDING":
      badgeClass += "bg-yellow-100 text-yellow-800";
      text = "Chờ duyệt";
      break;
    case "APPROVED":
      badgeClass += "bg-green-100 text-green-800";
      icon = '<i class="fas fa-check-circle mr-2"></i>';
      text = "Đã duyệt";
      break;
    case "REJECTED":
      badgeClass += "bg-red-100 text-red-800";
      icon = '<i class="fas fa-times-circle mr-2"></i>';
      text = "Từ chối";
      break;
    default:
      badgeClass += "bg-gray-100 text-gray-800";
      text = "Không xác định";
  }

  statusBadge.className = badgeClass;
  statusBadge.innerHTML = `${icon}<span>${text}</span>`;
}
