(function () {
    function setMode(mode) {
        if (mode === "horizontal") {
            document.body.classList.add("horizontal-mode");
        } else {
            document.body.classList.remove("horizontal-mode");
        }
    }

    window.toggleReadingMode = function () {
        document.body.classList.toggle("horizontal-mode");
        localStorage.setItem(
            "readingMode",
            document.body.classList.contains("horizontal-mode") ? "horizontal" : "vertical"
        );
    };

    window.nextPage = function () {
        if (!document.body.classList.contains("horizontal-mode")) return;
        const container = document.querySelector(".content");
        if (!container) return;
        container.scrollBy({ left: window.innerWidth, behavior: "smooth" });
    };

    window.prevPage = function () {
        if (!document.body.classList.contains("horizontal-mode")) return;
        const container = document.querySelector(".content");
        if (!container) return;
        container.scrollBy({ left: -window.innerWidth, behavior: "smooth" });
    };

    window.scrollToTop = function () {
        window.scrollTo({ top: 0, behavior: "smooth" });
    };

    // Jump to a specific page index (used by slider tiles)
    window.scrollToPage = function (index) {
        const container = document.querySelector(".content");
        if (!container) return;

        const pages = Array.from(container.querySelectorAll(".page-wrap"));
        const target = pages[index];
        if (!target) return;

        if (document.body.classList.contains("horizontal-mode")) {
            container.scrollTo({ left: target.offsetLeft, behavior: "smooth" });
        } else {
            target.scrollIntoView({ behavior: "smooth", block: "start" });
        }
    };

    function getCurrentPageIndex() {
        const container = document.querySelector(".content");
        if (!container) return 0;
        const pages = Array.from(container.querySelectorAll(".page-wrap"));
        if (pages.length === 0) return 0;

        if (document.body.classList.contains("horizontal-mode")) {
            const x = container.scrollLeft;
            let bestIndex = 0;
            let bestDist = Infinity;
            for (let i = 0; i < pages.length; i++) {
                const dist = Math.abs(pages[i].offsetLeft - x);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestIndex = i;
                }
            }
            return bestIndex;
        }

        // Vertical fallback
        let bestIndex = 0;
        let bestDist = Infinity;
        for (let i = 0; i < pages.length; i++) {
            const rect = pages[i].getBoundingClientRect();
            const dist = Math.abs(rect.top);
            if (dist < bestDist) {
                bestDist = dist;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    function updateSliderUI(currentIndex) {
        const track = document.getElementById("sliderTrack");
        if (!track) return;

        const items = Array.from(track.querySelectorAll(".slider-item"));
        for (let i = 0; i < items.length; i++) {
            items[i].classList.toggle("active", i === currentIndex);
            items[i].classList.toggle("read", i < currentIndex);
        }
    }

    function initSliderInteractions() {
        const track = document.getElementById("sliderTrack");
        if (!track) return;

        // Delegation: avoid mixing inline onclick with JS logic
        track.addEventListener("click", function (e) {
            const item = e.target && e.target.closest ? e.target.closest(".slider-item") : null;
            if (!item) return;
            const index = Number(item.getAttribute("data-index"));
            if (Number.isNaN(index)) return;
            window.scrollToPage(index);
        });
    }

    function bindScrollTracking() {
        let raf = 0;
        function scheduleUpdate() {
            if (raf) return;
            raf = window.requestAnimationFrame(function () {
                raf = 0;
                updateSliderUI(getCurrentPageIndex());
            });
        }

        const container = document.querySelector(".content");
        if (container) {
            container.addEventListener("scroll", scheduleUpdate, { passive: true });
        }
        window.addEventListener("resize", scheduleUpdate);

        // First paint
        scheduleUpdate();
    }

    function initHeaderAutoHide() {
        const header = document.querySelector(".chapter-read-page .mf-header");
        if (!header) return;

        let lastScrollY = window.pageYOffset || document.documentElement.scrollTop || 0;
        let raf = 0;
        const minDelta = 12;
        const showAtTop = 80;

        function setHeaderVisible(visible) {
            header.classList.toggle("is-hidden-on-scroll", !visible);
        }

        function isNavbarExpanded() {
            return !!header.querySelector(".navbar-collapse.show");
        }

        function updateHeaderState() {
            raf = 0;
            const currentY = window.pageYOffset || document.documentElement.scrollTop || 0;

            if (currentY <= showAtTop || isNavbarExpanded()) {
                setHeaderVisible(true);
                lastScrollY = currentY;
                return;
            }

            const delta = currentY - lastScrollY;
            if (Math.abs(delta) < minDelta) return;

            setHeaderVisible(delta < 0);
            lastScrollY = currentY;
        }

        function onScroll() {
            if (raf) return;
            raf = window.requestAnimationFrame(updateHeaderState);
        }

        window.addEventListener("scroll", onScroll, { passive: true });

        const navbarToggler = header.querySelector(".navbar-toggler");
        if (navbarToggler) {
            navbarToggler.addEventListener("click", function () {
                window.setTimeout(function () {
                    if (isNavbarExpanded()) setHeaderVisible(true);
                }, 0);
            });
        }
    }

    function init() {
        initHeaderAutoHide();

        // Restore reading mode
        const saved = localStorage.getItem("readingMode");
        if (saved === "horizontal") setMode("horizontal");

        // Scroll top button
        const scrollBtn = document.getElementById("scrollTopBtn");
        if (scrollBtn) {
            window.addEventListener("scroll", function () {
                const top = document.documentElement.scrollTop || document.body.scrollTop;
                scrollBtn.style.display = top > 300 ? "block" : "none";
            });
        }

        initSliderInteractions();
        bindScrollTracking();
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();
