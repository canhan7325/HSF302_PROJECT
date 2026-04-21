(function () {
    const wrapper = document.querySelector('.mf-header-search');
    if (!wrapper) {
        return;
    }

    const input = wrapper.querySelector('#comicSearchInput');
    const suggestions = wrapper.querySelector('#comicSearchSuggestions');
    const endpoint = wrapper.getAttribute('data-search-endpoint') || '/api/comics/search';

    if (!input || !suggestions) {
        return;
    }

    let debounceTimer = null;

    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function hideSuggestions() {
        suggestions.classList.add('d-none');
        suggestions.innerHTML = '';
    }

    function renderSuggestions(items) {
        if (!items || items.length === 0) {
            suggestions.innerHTML = '<div class="mf-search-empty">Khong tim thay truyen phu hop</div>';
            suggestions.classList.remove('d-none');
            return;
        }

        suggestions.innerHTML = items.map(function (item) {
            const safeTitle = escapeHtml(item.title || 'Khong co tieu de');
            const safeSlug = encodeURIComponent(item.slug || '');
            const safeCover = escapeHtml(item.coverImg || '');
            const coverHtml = safeCover
                ? '<img class="mf-search-item-cover" src="' + safeCover + '" alt="' + safeTitle + '">'
                : '<div class="mf-search-item-cover mf-search-item-cover-placeholder"></div>';

            return '<a class="mf-search-item" href="/comic/' + safeSlug + '">' +
                coverHtml +
                '<span class="mf-search-item-title">' + safeTitle + '</span>' +
                '</a>';
        }).join('');

        suggestions.classList.remove('d-none');
    }

    async function fetchSuggestions(keyword) {
        const response = await fetch(endpoint + '?q=' + encodeURIComponent(keyword) + '&limit=8', {
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            hideSuggestions();
            return;
        }

        const items = await response.json();
        renderSuggestions(items);
    }

    input.addEventListener('input', function () {
        const keyword = input.value.trim();
        clearTimeout(debounceTimer);

        if (!keyword) {
            hideSuggestions();
            return;
        }

        debounceTimer = setTimeout(function () {
            fetchSuggestions(keyword).catch(function () {
                hideSuggestions();
            });
        }, 250);
    });

    document.addEventListener('click', function (event) {
        if (!wrapper.contains(event.target)) {
            hideSuggestions();
        }
    });

    input.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            hideSuggestions();
            input.blur();
        }
    });
})();
