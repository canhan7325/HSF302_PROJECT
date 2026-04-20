document.addEventListener("DOMContentLoaded", function () {
    const sortBtn = document.getElementById("chapterSortBtn");
    const tableBody = document.querySelector("#chapterTable tbody");

    if (!sortBtn || !tableBody) {
        return;
    }

    let descending = true;

    sortBtn.addEventListener("click", function () {
        const rows = Array.from(tableBody.querySelectorAll("tr")).filter(function (row) {
            return row.children.length > 1;
        });

        rows.sort(function (a, b) {
            const chapterA = parseInt(a.children[0].textContent.replace(/[^0-9]/g, ""), 10) || 0;
            const chapterB = parseInt(b.children[0].textContent.replace(/[^0-9]/g, ""), 10) || 0;
            return descending ? chapterA - chapterB : chapterB - chapterA;
        });

        rows.forEach(function (row) {
            tableBody.appendChild(row);
        });

        descending = !descending;
    });
});

