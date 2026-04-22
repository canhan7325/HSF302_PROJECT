(function () {
    // Lấy từ biến global do Thymeleaf inject
    const isLoggedIn = window.APP_IS_LOGGED_IN || false;
    if (!isLoggedIn) return;

    function setBookmarked(btn, bookmarked) {
        btn.classList.toggle('is-bookmarked', !!bookmarked);

        // optional: update card attribute nếu có
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
})();