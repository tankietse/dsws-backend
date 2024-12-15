// File: app.js

const baseUrl = 'http://localhost:8081/api/benh';

// Fetch list of diseases and populate the table
async function fetchBenhList() {
    try {
        const response = await fetch(baseUrl);
        const data = await response.json();

        const tbody = document.querySelector('#benh-table tbody');
        tbody.innerHTML = '';

        data.content.forEach(benh => {
            const row = document.createElement('tr');

            row.innerHTML = `
                <td>${benh.id}</td>
                <td>${benh.tenBenh}</td>
                <td>${benh.moTa}</td>
                <td>${benh.tacNhanGayBenh}</td>
                <td>${benh.trieuChung}</td>
                <td>${benh.thoiGianUBenh}</td>
                <td>${benh.phuongPhapChanDoan}</td>
                <td>${benh.bienPhapPhongNgua}</td>
                <td>
                    <button class="edit-btn" onclick="editBenh(${benh.id})">Sửa</button>
                    <button class="delete-btn" onclick="deleteBenh(${benh.id})">Xóa</button>
                </td>
            `;
            row.addEventListener('click', () => viewBenhDetails(benh.id));

            tbody.appendChild(row);
        });
    } catch (error) {
        console.error('Error fetching benh list:', error);
    }
}

// Fetch disease details
async function viewBenhDetails(id) {
    try {
        window.location.href = `detail.html?id=${id}`;
    } catch (error) {
        console.error('Error viewing benh details:', error);
    }
}

// Delete disease
async function deleteBenh(id) {
    try {
        if (confirm('Bạn có chắc chắn muốn xóa bệnh này?')) {
            await fetch(`${baseUrl}/${id}`, {
                method: 'DELETE'
            });
            alert('Xóa bệnh thành công!');
            fetchBenhList();
        }
    } catch (error) {
        console.error('Error deleting benh:', error);
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.querySelector('input[name="keyword"]');
    const clearSearchBtn = document.createElement('button');
    clearSearchBtn.innerHTML = '&times;';
    clearSearchBtn.classList.add('absolute', 'right-10', 'top-0', 'mt-2', 'mr-1', 'text-gray-500', 'hover:text-red-500');
    clearSearchBtn.style.display = 'none';

    if (searchInput) {
        const parentDiv = searchInput.parentElement;
        parentDiv.appendChild(clearSearchBtn);

        // Hiển thị/ẩn nút xóa
        searchInput.addEventListener('input', function() {
            clearSearchBtn.style.display = this.value ? 'block' : 'none';
        });

        // Xóa nội dung tìm kiếm
        clearSearchBtn.addEventListener('click', function() {
            searchInput.value = '';
            window.location.href = '/benh';
        });
    }
});

// Initialize page
fetchBenhList();