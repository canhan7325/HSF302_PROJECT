(function () {
    const isLoggedIn = window.APP_IS_LOGGED_IN || false;
    if (!isLoggedIn) return;

    function setBookmarked(btn, bookmarked) {
        btn.classList.toggle('is-bookmarked', !!bookmarked);

        const card = btn.closest('.card');
        if (card) {
            card.setAttribute('data-bookmarked', bookmarked ? 'true' : 'false');
        }
    }

    async function toggleBookmark(comicId) {
        const body = new URLSearchParams();
        body.set('comicId', comicId);

        const res = await fetch('/bookmarks/toggle', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
            body
        });

        let data = null;
        try { data = await res.json(); } catch (e) {}

        if (!res.ok || !data || data.ok !== true) {
            const err = data && data.error ? data.error : 'UNKNOWN';

            if (err === 'UNAUTHORIZED') {
                window.location.href = '/login';
                return null;
            }

            throw new Error(err);
        }

        return data;
    }

    document.addEventListener('click', async (e) => {
        const btn = e.target.closest('button.bookmark-btn[data-comic-id]');
        if (!btn) return;

        e.preventDefault();
        e.stopPropagation();

        const comicId = btn.getAttribute('data-comic-id');
        if (!comicId) return;

        try {
            btn.classList.add('is-loading');

            const data = await toggleBookmark(comicId);
            if (!data) return;

            setBookmarked(btn, !!data.bookmarked);

        } catch (err) {
            console.error('Toggle bookmark failed:', err);
        } finally {
            btn.classList.remove('is-loading');
        }
    });

    const initLibraryFilter = () => {
        const STORAGE_KEY = 'bookmarkFilter';
        const DEFAULT_VISIBLE = 12;

        const filterRoot = document.getElementById('bookmarkFilter');
        const grid = document.getElementById('bookmarkGrid');
        const loadMoreBtn = document.querySelector('.load-more button');

        // Nếu không ở trang Library (không có grid) thì thoát phần này
        if (!filterRoot || !grid) return;

        const buttons = Array.from(filterRoot.querySelectorAll('button[data-filter]'));

        function allCards() {
            return Array.from(grid.querySelectorAll('.card[data-status]'));
        }

        function visibleCardsWithFilter(filterValue) {
            const v = (filterValue || 'all').toLowerCase();
            return allCards().filter(card => {
                const status = (card.getAttribute('data-status') || '').toLowerCase();
                const matches = (v === 'all') || (status === v);
                return matches && !card.classList.contains('is-hidden');
            });
        }

        function setActiveByValue(value) {
            const v = (value || 'all').toLowerCase();
            buttons.forEach(b => b.classList.toggle('active', (b.getAttribute('data-filter') || '').toLowerCase() === v));
        }

        function showLoadMore(show) {
            if (!loadMoreBtn) return;
            loadMoreBtn.style.display = show ? '' : 'none';
        }

        function isExpanded() {
            return grid.getAttribute('data-expanded') === 'true';
        }

        function setExpanded(expanded) {
            grid.setAttribute('data-expanded', expanded ? 'true' : 'false');
        }

        function applyCollapse(filterValue) {
            const expanded = isExpanded();
            allCards().forEach(c => c.classList.remove('is-collapsed'));

            if (expanded) {
                showLoadMore(false);
                return;
            }

            const list = visibleCardsWithFilter(filterValue);
            list.forEach((card, idx) => {
                if (idx >= DEFAULT_VISIBLE) card.classList.add('is-collapsed');
            });

            showLoadMore(list.length > DEFAULT_VISIBLE);
        }

        function applyFilter(value) {
            const v = (value || 'all').toLowerCase();
            allCards().forEach(card => {
                const status = (card.getAttribute('data-status') || '').toLowerCase();
                const show = (v === 'all') || (status === v);
                card.classList.toggle('is-hidden', !show);
            });

            setActiveByValue(v);
            applyCollapse(v);

            try {
                localStorage.setItem(STORAGE_KEY, v);
            } catch (e) {}
        }

        filterRoot.addEventListener('click', (e) => {
            const btn = e.target.closest('button[data-filter]');
            if (!btn) return;
            setExpanded(false);
            applyFilter(btn.getAttribute('data-filter'));
        });

        if (loadMoreBtn) {
            loadMoreBtn.addEventListener('click', () => {
                setExpanded(true);
                const current = (filterRoot.querySelector('button.active')?.getAttribute('data-filter')) || 'all';
                applyCollapse(current);
            });
        }

        // Khởi tạo trạng thái ban đầu
        let initial = 'all';
        try {
            const saved = localStorage.getItem(STORAGE_KEY);
            if (saved && ['all', 'reading', 'unread'].includes(saved.toLowerCase())) {
                initial = saved.toLowerCase();
            }
        } catch (e) {}

        setExpanded(false);
        applyFilter(initial);
    };

    const initBookmarkToggle = () => {
        const isLoggedIn = window.APP_IS_LOGGED_IN || false;

        async function toggleBookmark(comicId) {
            const body = new URLSearchParams();
            body.set('comicId', comicId);

            const res = await fetch('/bookmarks/toggle', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                body
            });

            let data = null;
            try { data = await res.json(); } catch (e) {}

            if (!res.ok || !data || data.ok !== true) {
                const err = data?.error || 'UNKNOWN';
                if (err === 'UNAUTHORIZED') {
                    window.location.href = '/login';
                    return null;
                }
                throw new Error(err);
            }
            return data;
        }

        document.addEventListener('click', async (e) => {
            const btn = e.target.closest('button.bookmark-btn[data-comic-id]');
            if (!btn) return;

            if (!isLoggedIn) {
                window.location.href = '/login';
                return;
            }

            e.preventDefault();
            e.stopPropagation();

            const comicId = btn.getAttribute('data-comic-id');
            try {
                btn.classList.add('is-loading');
                const data = await toggleBookmark(comicId);
                if (data) {
                    btn.classList.toggle('is-bookmarked', !!data.bookmarked);
                    // Update attribute cho card nếu cần (dùng cho filter)
                    const card = btn.closest('.card');
                    if (card) card.setAttribute('data-bookmarked', data.bookmarked);
                }
            } catch (err) {
                console.error('Toggle bookmark failed:', err);
            } finally {
                btn.classList.remove('is-loading');
            }
        });
    };

    initLibraryFilter();
    initBookmarkToggle();
})();