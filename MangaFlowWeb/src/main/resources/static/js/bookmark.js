(function () {
    // Biến toàn cục giả lập từ server-side
    const isLoggedIn = window.APP_IS_LOGGED_IN || false;

    /**
     * AJAX POST dùng XMLHttpRequest
     */
    function ajaxPost(url, params) {
        return new Promise((resolve, reject) => {
            const xhr = new XMLHttpRequest();
            xhr.open('POST', url);
            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded;charset=UTF-8');
            xhr.onload = function () {
                let data = null;
                try { data = JSON.parse(xhr.responseText); } catch (e) {}
                if (xhr.status >= 200 && xhr.status < 300 && data?.ok === true) {
                    resolve(data);
                } else {
                    reject(new Error(data?.error || 'UNKNOWN'));
                }
            };
            xhr.onerror = () => reject(new Error('NETWORK_ERROR'));
            xhr.send(params.toString());
        });
    }

    /**
     * Logic Filter & Load More
     */
    const initLibraryFilter = () => {
        const STORAGE_KEY = 'bookmarkFilter';
        const DEFAULT_VISIBLE = 12;

        const filterRoot = document.getElementById('bookmarkFilter');
        const grid = document.getElementById('bookmarkGrid');
        const loadMoreBtn = document.querySelector('.load-more button');

        if (!filterRoot || !grid) return;

        const buttons = Array.from(filterRoot.querySelectorAll('button[data-filter]'));
        const getCards = () => Array.from(grid.querySelectorAll('.card[data-status]'));

        function applyFilter(value) {
            const v = (value || 'all').toLowerCase();
            const cards = getCards();
            const expanded = grid.getAttribute('data-expanded') === 'true';

            // 1. Lọc theo trạng thái
            cards.forEach(card => {
                const status = (card.getAttribute('data-status') || '').toLowerCase();
                const matches = (v === 'all') || (status === v);
                card.classList.toggle('is-hidden', !matches);
                card.classList.remove('is-collapsed');
            });

            // 2. Xử lý "Xem thêm"
            const visibleList = cards.filter(c => !c.classList.contains('is-hidden'));
            if (!expanded) {
                visibleList.forEach((card, idx) => {
                    if (idx >= DEFAULT_VISIBLE) card.classList.add('is-collapsed');
                });
                if (loadMoreBtn) loadMoreBtn.style.display = visibleList.length > DEFAULT_VISIBLE ? '' : 'none';
            } else if (loadMoreBtn) {
                loadMoreBtn.style.display = 'none';
            }

            // 3. UI
            buttons.forEach(b => b.classList.toggle('active', (b.getAttribute('data-filter') || '').toLowerCase() === v));
            try { localStorage.setItem(STORAGE_KEY, v); } catch (e) {}
        }

        filterRoot.addEventListener('click', (e) => {
            const btn = e.target.closest('button[data-filter]');
            if (!btn) return;
            grid.setAttribute('data-expanded', 'false');
            applyFilter(btn.getAttribute('data-filter'));
        });

        if (loadMoreBtn) {
            loadMoreBtn.addEventListener('click', () => {
                grid.setAttribute('data-expanded', 'true');
                const activeFilter = filterRoot.querySelector('button.active')?.getAttribute('data-filter') || 'all';
                applyFilter(activeFilter);
            });
        }

        // Khởi tạo
        let initial = localStorage.getItem(STORAGE_KEY) || 'all';
        applyFilter(initial);
    };

    /**
     * Logic Click Bookmark
     */
    document.addEventListener('click', async (e) => {
        const btn = e.target.closest('button.bookmark-btn[data-comic-id]');
        if (!btn) return;

        if (!isLoggedIn) {
            window.location.href = '/login';
            return;
        }

        const comicId = btn.getAttribute('data-comic-id');
        const body = new URLSearchParams();
        body.set('comicId', comicId);

        try {
            btn.classList.add('is-loading');
            const data = await ajaxPost('/bookmarks/toggle', body);

            btn.classList.toggle('is-bookmarked', !!data.bookmarked);
            const card = btn.closest('.card');
            if (card) card.setAttribute('data-bookmarked', !!data.bookmarked);

        } catch (err) {
            if (err.message === 'UNAUTHORIZED') window.location.href = '/login';
            else console.error('Lỗi:', err);
        } finally {
            btn.classList.remove('is-loading');
        }
    });

    // Chạy khởi tạo filter
    initLibraryFilter();
})();