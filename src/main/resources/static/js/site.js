document.addEventListener('DOMContentLoaded', function () {
    const taskItems = document.querySelectorAll('.task-name');

    taskItems.forEach(function (item) {
        item.addEventListener('mouseenter', function () {
            this.classList.add('hovered'); // Thêm class 'hovered' khi hover
        });

        item.addEventListener('mouseleave', function () {
            this.classList.remove('hovered'); // Loại bỏ class 'hovered' khi mất hover
        });

        item.addEventListener('click', function () {
            // Kiểm tra xem thẻ li đã được click hay chưa
            if (this.classList.contains('clicked')) {
                this.classList.remove('clicked'); // Nếu đã click, loại bỏ class 'clicked'
            } else {
                // Nếu chưa click, loại bỏ class 'clicked' ở tất cả các thẻ li khác và thêm class 'clicked' vào thẻ li hiện tại
                taskItems.forEach(function (task) {
                    task.classList.remove('clicked');
                });
                this.classList.add('clicked');
            }
        });
    });

    $(document).ready(function () {
        $('#emps').click(function (event) {
            event.preventDefault();

            $.ajax({
                url: '/Nhanvien/Emp-List',
                success: function (data) {
                    $('#main-content').html(data);
                },
                error: function (error) {
                    console.error("Error loading content:", error);
                }
            });
        });
    });
});

// Lưu vị trí scroll
var scrollPosition = $(window).scrollTop();

// Cập nhật nội dung và vị trí scroll
$.ajax({
    url: '/Nhanvien/Index',
    success: function (data) {
        $('#main-content').html(data);
        $(window).scrollTop(scrollPosition);
    },
    error: function (error) {
        console.error("Error loading content:", error);
    }
});
function redirectToDetail(id) {
    window.location.href = 'Display/' + id;
}

function filterData() {
    // Get search string and selected filters
    const searchString = document.getElementById("searchString").value;
    const filterByName = document.getElementById("filterByName").checked;
    const filterById = document.getElementById("filterById").checked;
    const filterByPosition = document.getElementById("filterByPosition").checked;
    const filterByDepartment = document.getElementById("filterByDepartment").checked;

    // Prepare filter logic based on selections
    let filterQuery = "";
    if (filterByName) {
        filterQuery += "n.Ten.contains(@searchString) || ";
    }
    if (filterById) {
        filterQuery += "n.Id.contains(@searchString) || ";
    }
    if (filterByPosition) {
        filterQuery += "n.TenVitri.contains(@searchString) || ";
    }
    if (filterByDepartment) {
        filterQuery += "n.TenPhongBan.contains(@searchString) || ";
    }

    // Remove the last "||" if any filters were selected
    if (filterQuery.length > 0) {
        filterQuery = filterQuery.substring(0, filterQuery.length - 4);
    }

    // Simulate form submission (assuming you have a form for filtering)
    // Replace this with your actual form submission logic if needed
    const form = document.createElement("form");
    form.method = "POST";
    form.action = "@Url.Action("Index", "Info")"; // Replace with your actual action URL
    const searchStringInput = document.createElement("input");
    searchStringInput.type = "hidden";
    searchStringInput.name = "searchString";
    searchStringInput.value = searchString;
    form.appendChild(searchStringInput);

    // Submit the form
    form.submit();
}
