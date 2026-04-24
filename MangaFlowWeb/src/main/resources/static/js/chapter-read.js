(function () {
    let currentPageIndex = 0;

    function setMode(mode) {
        if (mode === "horizontal") {
            document.body.classList.add("horizontal-mode");
        } else {
            document.body.classList.remove("horizontal-mode");
        }
    }

    function showToast(message, type = "error") {
        let toast = document.getElementById("mf-toast");

        if (!toast) {
            toast = document.createElement("div");
            toast.id = "mf-toast";
            document.body.appendChild(toast);
        }

        toast.className = `mf-toast ${type}`;
        toast.innerText = message;

        requestAnimationFrame(() => {
            toast.classList.add("show");
        });

        clearTimeout(toast._timeout);
        toast._timeout = setTimeout(() => {
            toast.classList.remove("show");
        }, 2500);
    }

    function ajaxGet(url) {
        return new Promise((resolve, reject) => {
            const xhr = new XMLHttpRequest();
            xhr.open('GET', url);
            xhr.responseType = 'blob';

            xhr.onload = function () {
                if (xhr.status >= 200 && xhr.status < 300) {
                    resolve(xhr.response);
                } else {
                    reject(new Error('DOWNLOAD_FAILED'));
                }
            };

            xhr.onerror = () => reject(new Error('NETWORK_ERROR'));

            xhr.send();
        });
    }

    window.toggleReadingMode = function () {
        const isHorizontal = document.body.classList.toggle("horizontal-mode");
        localStorage.setItem("readingMode", isHorizontal ? "horizontal" : "vertical");

        if (isHorizontal) {
            initBookMode(); // <--- THÊM DÒNG NÀY ĐỂ RESET CLASS KHI BẬT NGANG
        }
    };

    window.nextPage = function () {
        if (!document.body.classList.contains("horizontal-mode")) return;

        const pages = document.querySelectorAll(".page-wrap");
        if (currentPageIndex < pages.length - 1) {
            const current = pages[currentPageIndex];

            current.classList.remove("active");
            current.classList.add("flipped");

            currentPageIndex++;

            const next = pages[currentPageIndex];
            next.classList.add("active");
            next.classList.remove("upcoming");

            if (pages[currentPageIndex + 1]) {
                pages[currentPageIndex + 1].classList.add("upcoming");
            }

            if (typeof preloadBuffer === 'function') preloadBuffer(currentPageIndex, 2);
            updateSliderUI(currentPageIndex);
        }
    };

    window.prevPage = function () {
        if (!document.body.classList.contains("horizontal-mode")) return;

        const pages = document.querySelectorAll(".page-wrap");
        if (currentPageIndex > 0) {
            const current = pages[currentPageIndex];
            const prev = pages[currentPageIndex - 1];

            // Trang hiện tại lùi về trạng thái chờ
            current.classList.remove("active");
            current.classList.add("upcoming");

            currentPageIndex--;

            // Trang cũ lật ngược lại
            prev.classList.remove("flipped");
            prev.classList.add("active");

            if (pages[currentPageIndex + 2]) {
                pages[currentPageIndex + 2].classList.remove("upcoming");
            }
            updateSliderUI(currentPageIndex);
        }
    };

    window.scrollToTop = function () {
        window.scrollTo({ top: 0, behavior: "smooth" });
    };

    // Jump to a specific page index (used by slider tiles)
    window.scrollToPage = function (index) {
        if (!document.body.classList.contains("horizontal-mode")) {
            // Chế độ dọc: Vẫn dùng scrollIntoView
            const pages = document.querySelectorAll(".page-wrap");
            if (pages[index]) pages[index].scrollIntoView({ behavior: "smooth" });
            return;
        }

        const pages = document.querySelectorAll(".page-wrap");
        if (index < 0 || index >= pages.length) return;

        // Cập nhật biến chỉ mục toàn cục
        currentPageIndex = index;

        // Cập nhật class cho tất cả các trang dựa trên chỉ mục mới
        pages.forEach((page, i) => {
            page.classList.remove("active", "flipped", "upcoming");
            page.style.zIndex = ""; // Xóa các z-index inline cũ nếu có

            if (i < index) {
                page.classList.add("flipped");
            } else if (i === index) {
                page.classList.add("active");
            } else if (i === index + 1) {
                page.classList.add("upcoming");
            }
        });

        // Cập nhật giao diện thanh slide ngay lập tức
        updateSliderUI(currentPageIndex);

        // Tải ảnh cho trang mới
        if (typeof preloadBuffer === 'function') preloadBuffer(currentPageIndex, 2);
    };

    function getCurrentPageIndex() {
        if (document.body.classList.contains("horizontal-mode")) {
            return currentPageIndex;
        }
        // Chế độ dọc: Giữ nguyên logic cũ
        const container = document.querySelector(".content");
        if (!container) return 0;
        const pages = Array.from(container.querySelectorAll(".page-wrap"));
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
        items.forEach((item, i) => {
            item.classList.toggle("active", i === currentIndex);
            item.classList.toggle("read", i < currentIndex);
        });
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

    function initDownloadPdf() {
        const btn = document.querySelector(".right-actions .download-pdf-btn");
        if (!btn) return;

        btn.addEventListener("click", async function () {

            const isLoggedIn = window.APP_IS_LOGGED_IN;
            const tier = window.APP_TIER;
            const chapterId = btn.getAttribute("data-chapter-id");

            if (!chapterId) return;

            if (!isLoggedIn) {
                showToast("Bạn cần đăng nhập để tải PDF. Bấm Đăng nhập ở góc trên để tiếp tục", "info");
                return;
            }

            if (tier !== "silver" && tier !== "gold") {
                showToast("Bạn cần gói Silver hoặc Gold. Vào Gói Hội Viên để nâng cấp", "info");
                return;
            }

            btn.classList.add("is-loading");

            try {
                const url = `/api/chapters/${encodeURIComponent(chapterId)}/pdf`;

                const blob = await ajaxGet(url);

                const a = document.createElement("a");
                a.href = URL.createObjectURL(blob);
                a.download = `chapter-${chapterId}.pdf`;

                document.body.appendChild(a);
                a.click();
                a.remove();

                URL.revokeObjectURL(a.href);

            } catch (err) {
                showToast("Tải PDF thất bại (kiểm tra quyền hoặc mạng)", "error");
            } finally {
                btn.classList.remove("is-loading");
            }
        });
    }

    function initLazyLoading() {
        const lazyImages = document.querySelectorAll('.lazy-comic');
        const allPageWraps = document.querySelectorAll('.page-wrap');

        // Hàm thực hiện việc tải ảnh thực tế
        const loadImage = (img) => {
            const src = img.getAttribute('data-src');
            if (!src || img.src === src) return;

            img.src = src;
            img.onload = () => {
                img.classList.add('loaded');
                const loader = img.parentElement.querySelector('.page-loader-spinner');
                if (loader) loader.remove();
            };
        };

        // Hàm tải trước (Buffer): Tải trang hiện tại và n trang tiếp theo
        const preloadBuffer = (currentIndex, bufferCount = 2) => {
            for (let i = 0; i <= bufferCount; i++) {
                const targetIndex = currentIndex + i;
                if (targetIndex < allPageWraps.length) {
                    const nextImg = allPageWraps[targetIndex].querySelector('.lazy-comic');
                    if (nextImg) loadImage(nextImg);
                }
            }
        };

        // Khởi tạo Intersection Observer
        const observerOptions = {
            root: null, // null là quan sát theo viewport
            rootMargin: '500px 0px', // Tải trước khi người dùng cuộn tới 500px (cho dọc)
            threshold: 0.01
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const wrap = entry.target;
                    const index = parseInt(wrap.getAttribute('data-index'));

                    // Khi thấy trang X, tải trang X và n trang tiếp theo (Buffer)
                    preloadBuffer(index, 2);

                    // Nếu muốn tối ưu tuyệt đối, có thể ngừng quan sát trang này sau khi đã load
                    // observer.unobserve(wrap);
                }
            });
        }, observerOptions);

        allPageWraps.forEach(wrap => observer.observe(wrap));
    }

    function initBookMode() {
        const pages = document.querySelectorAll(".page-wrap");
        if (pages.length === 0) return;

        currentPageIndex = 0;
        pages.forEach((page, index) => {
            // Reset sạch sẽ tất cả style inline và class cũ
            page.style.zIndex = "";
            page.style.opacity = "";
            page.classList.remove("active", "flipped", "upcoming");

            if (index === 0) {
                page.classList.add("active");
            } else if (index === 1) {
                page.classList.add("upcoming");
            }
        });
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
            if (blockedKeys.includes(e.keyCode) || e.key === 'PrintScreen' ||
                (e.ctrlKey && (e.shiftKey && [73, 67, 74].includes(e.keyCode))) || // Ctrl+Shift+I/C/J
                (e.ctrlKey && [85, 80, 83].includes(e.keyCode)) || // Ctrl+U/P/S
                ((e.metaKey || e.osKey) && e.shiftKey && e.keyCode === 83) // Win+Shift+S
            ) {
                e.preventDefault();
                showSecurityOverlayImmediate();
                
                // Cố gắng xóa clipboard ngay lập tức
                if (navigator.clipboard && navigator.clipboard.writeText) {
                    navigator.clipboard.writeText("Nội dung được bảo vệ - MangaFlow");
                }
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
            // Xử lý riêng cho PrtSc ở sự kiện keyup vì một số trình duyệt kích hoạt ở đây
            if (e.keyCode === 44 || e.key === 'PrintScreen') {
                showSecurityOverlayImmediate();
                if (navigator.clipboard && navigator.clipboard.writeText) {
                    navigator.clipboard.writeText("Nội dung được bảo vệ - MangaFlow");
                }
                return;
            }
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
        if (saved === "horizontal") {
            document.body.classList.add("horizontal-mode");
            initBookMode(); // <--- THÊM DÒNG NÀY VÀO ĐÂY
        }

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
        initDownloadPdf();
        initLazyLoading();
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();
