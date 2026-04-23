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

    function initSecurity() {
        // Kiểm tra quyền (Chỉ chạy cho người dùng thường)
        if (window.APP_CAN_USE_DEVTOOLS) {
            console.warn("SECURITY: Disabled for Admin/Author.");
            return;
        }
        console.log("SECURITY: Aggressive protection enabled.");

        // 1. Vòng lặp Debugger (Chống F12 cực mạnh)
        setInterval(function() {
            (function() { return false; }["constructor"]("debugger")["call"]());
        }, 200);

        // 2. Chặn chuột phải và các tổ hợp phím
        document.addEventListener('contextmenu', e => e.preventDefault());
        document.addEventListener('keydown', function(e) {
            // Chặn F12, Ctrl+Shift+I/C/J, Ctrl+U, Ctrl+P, Ctrl+S, PrintScreen
            const blockedKeys = [123, 44]; // F12, PrintScreen
            if (blockedKeys.includes(e.keyCode) || 
                (e.ctrlKey && (e.shiftKey && [73, 67, 74].includes(e.keyCode))) || // Ctrl+Shift+I/C/J
                (e.ctrlKey && [85, 80, 83].includes(e.keyCode)) || // Ctrl+U/P/S
                ((e.metaKey || e.osKey) && e.shiftKey && e.keyCode === 83) // Win+Shift+S
            ) {
                e.preventDefault();
                showSecurityOverlayImmediate();
                return false;
            }
        });

        // 3. Bảo vệ nội dung khi mất Focus hoặc Chuột rời khỏi trang
        const showSecurityOverlay = () => {
            if (!document.getElementById('security-blur-overlay')) {
                const overlay = document.createElement('div');
                overlay.id = 'security-blur-overlay';
                overlay.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.95);z-index:999999;display:flex;align-items:center;justify-content:center;color:#fff;font-family:\"Plus Jakarta Sans\", \"Manrope\", sans-serif;text-align:center;padding:20px;pointer-events:none;backdrop-filter:blur(25px);-webkit-backdrop-filter:blur(25px);';
                overlay.innerHTML = `
                    <div style="max-width: 650px;">
                        <div style="font-family:\"Plus Jakarta Sans\", sans-serif; font-size:42px;font-weight:900;color:#ff3333;margin-bottom:20px;letter-spacing:1px;text-transform:uppercase;text-shadow: 0 0 20px rgba(255,0,0,0.4);">CẢNH BÁO BẢN QUYỀN</div>
                        <div style="font-size:22px;font-weight:700;margin-bottom:15px;line-height:1.4;color:#f8fafc;">Hành động chụp ảnh/quay phim màn hình đã bị chặn.</div>
                        <div style="font-size:15px;color:#94a3b8;font-weight:500;">Hệ thống bảo vệ nội dung đang hoạt động. Vui lòng quay lại tab để tiếp tục.</div>
                    </div>
                `;
                document.body.appendChild(overlay);
            }
        };

        let securityTimeout = null;

        const hideSecurityOverlayWithDelay = () => {
            if (securityTimeout) clearTimeout(securityTimeout);
            securityTimeout = setTimeout(() => {
                const overlay = document.getElementById('security-blur-overlay');
                if (overlay) overlay.remove();
                securityTimeout = null;
            }, 1000); 
        };

        const showSecurityOverlayImmediate = () => {
            if (securityTimeout) {
                clearTimeout(securityTimeout);
                securityTimeout = null;
            }
            showSecurityOverlay();
        };

        // Kiểm tra liên tục ở tốc độ 60fps (Cực nhanh)
        function fastSecurityCheck() {
            if (window.APP_CAN_USE_DEVTOOLS) return;

            if (!document.hasFocus() || document.visibilityState !== 'visible') {
                showSecurityOverlayImmediate();
                // Xóa clipboard liên tục
                if (navigator.clipboard && navigator.clipboard.writeText) {
                    navigator.clipboard.writeText("Protected Content");
                }
            }
            requestAnimationFrame(fastSecurityCheck);
        }
        requestAnimationFrame(fastSecurityCheck);

        // Chặn tức thì khi nhấn phím bổ trợ (Shift, Win, Alt)
        window.addEventListener('keydown', e => {
            if (e.shiftKey || e.metaKey || e.altKey || e.ctrlKey) {
                showSecurityOverlayImmediate();
            }
        });
        window.addEventListener('keyup', e => {
            if (!e.shiftKey && !e.metaKey && !e.altKey && !e.ctrlKey) {
                hideSecurityOverlayWithDelay();
            }
        });

        window.addEventListener('blur', showSecurityOverlayImmediate);
        window.addEventListener('focus', hideSecurityOverlayWithDelay);
        document.addEventListener('mouseleave', showSecurityOverlayImmediate);
        document.addEventListener('mouseenter', hideSecurityOverlayWithDelay);

        // 4. Chặn kéo thả ảnh
        document.addEventListener('dragstart', e => {
            if (e.target.tagName === 'IMG') e.preventDefault();
        });
    }

    function init() {
        initSecurity();
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
