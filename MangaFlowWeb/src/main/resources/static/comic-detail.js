document.addEventListener("DOMContentLoaded", function () {
    const sortBtn = document.getElementById("chapterSortBtn");
    const tableBody = document.querySelector("#chapterTable tbody");
    const tableContainer = document.getElementById("chapterTableContainer");

    if (!tableBody) {
        return;
    }

    const chapterRows = Array.from(tableBody.querySelectorAll("tr")).filter(function (row) {
        return row.children.length > 1;
    });
    const BATCH_SIZE = 10;
    let visibleCount = Math.min(BATCH_SIZE, chapterRows.length);

    function renderVisibleRows() {
        chapterRows.forEach(function (row, index) {
            row.style.display = index < visibleCount ? "" : "none";
        });
    }

    function showNextBatch() {
        if (visibleCount >= chapterRows.length) {
            return;
        }
        visibleCount = Math.min(visibleCount + BATCH_SIZE, chapterRows.length);
        renderVisibleRows();
    }

    renderVisibleRows();

    if (chapterRows.length > visibleCount) {
        let loading = false;

        const onContainerScroll = function () {
            if (loading || visibleCount >= chapterRows.length || !tableContainer) {
                return;
            }

            const remain = tableContainer.scrollHeight - tableContainer.scrollTop - tableContainer.clientHeight;
            const nearBottom = remain < 120;
            if (!nearBottom) {
                return;
            }

            loading = true;
            showNextBatch();
            loading = false;

            if (visibleCount >= chapterRows.length) {
                tableContainer.removeEventListener("scroll", onContainerScroll);
                window.removeEventListener("scroll", onWindowScroll);
            }
        };

        const onWindowScroll = function () {
            if (!tableContainer || loading || visibleCount >= chapterRows.length) {
                return;
            }
            const rect = tableContainer.getBoundingClientRect();
            if (rect.top < window.innerHeight && rect.bottom > 0) {
                onContainerScroll();
            }
        };

        if (tableContainer) {
            tableContainer.addEventListener("scroll", onContainerScroll, { passive: true });
        }
        window.addEventListener("scroll", onWindowScroll, { passive: true });
    }

    if (!sortBtn) {
        return;
    }

    let descending = true;

    sortBtn.addEventListener("click", function () {
        chapterRows.sort(function (a, b) {
            const chapterA = parseInt(a.children[0].textContent.replace(/[^0-9]/g, ""), 10) || 0;
            const chapterB = parseInt(b.children[0].textContent.replace(/[^0-9]/g, ""), 10) || 0;
            return descending ? chapterA - chapterB : chapterB - chapterA;
        });

        chapterRows.forEach(function (row) {
            tableBody.appendChild(row);
        });

        renderVisibleRows();

        descending = !descending;
    });
});

