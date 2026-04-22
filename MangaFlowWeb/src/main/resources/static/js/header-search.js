(function () {
    const wrapper = document.querySelector('.mf-header-search') || document;
    const input = document.getElementById('searchInput') || document.getElementById('comicSearchInput');
    const dropdown = document.getElementById('searchResultDropdown') || document.getElementById('comicSearchSuggestions');
    const endpoint = (wrapper.getAttribute && wrapper.getAttribute('data-search-endpoint')) || '/api/comics/search';

    if (!input || !dropdown) {
        return;
    }

    let debounceTimer;

    function debounce(callback, delay) {
        return function () {
            const context = this;
            const args = arguments;
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(function () {
                callback.apply(context, args);
            }, delay);
        };
    }

    function clearAndHideDropdown() {
        dropdown.innerHTML = '';
        dropdown.classList.add('d-none');
    }

    function showDropdown() {
        dropdown.classList.remove('d-none');
    }

    function createNoResultItem() {
        const item = document.createElement('div');
        item.className = 'list-group-item text-muted';
        item.textContent = 'No results found';
        return item;
    }

    function createResultItem(comic) {
        const item = document.createElement('button');
        item.type = 'button';
        item.className = 'list-group-item list-group-item-action d-flex align-items-center gap-2';

        const thumbnail = document.createElement('img');
        thumbnail.src = comic.coverImage || comic.coverImg || 'data:,';
        thumbnail.alt = comic.title || 'Comic';
        thumbnail.width = 36;
        thumbnail.height = 48;
        thumbnail.style.objectFit = 'cover';
        thumbnail.className = 'rounded';

        const title = document.createElement('span');
        title.textContent = comic.title || 'Untitled comic';

        item.appendChild(thumbnail);
        item.appendChild(title);
        item.addEventListener('click', function () {
            if (comic.slug) {
                window.location.href = '/comic/' + encodeURIComponent(comic.slug);
                return;
            }
            window.location.href = '/comic-detail/' + encodeURIComponent(comic.id);
        });

        return item;
    }

    async function fetchResults(query) {
        const response = await fetch(endpoint + '?query=' + encodeURIComponent(query), {
            headers: { Accept: 'application/json' }
        });

        if (!response.ok) {
            clearAndHideDropdown();
            return;
        }

        const results = await response.json();
        dropdown.innerHTML = '';
        dropdown.classList.add('list-group');

        if (!Array.isArray(results) || results.length === 0) {
            dropdown.appendChild(createNoResultItem());
            showDropdown();
            return;
        }

        results.forEach(function (comic) {
            dropdown.appendChild(createResultItem(comic));
        });
        showDropdown();
    }

    input.addEventListener('input', debounce(function () {
        const query = input.value.trim();
        if (!query) {
            clearAndHideDropdown();
            return;
        }

        fetchResults(query).catch(function () {
            clearAndHideDropdown();
        });
    }, 300));

    document.addEventListener('click', function (event) {
        if (!input.contains(event.target) && !dropdown.contains(event.target)) {
            clearAndHideDropdown();
        }
    });

    input.addEventListener('keydown', function (event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            const query = input.value.trim();
            if (query) {
                window.location.href = '/search-comic?q=' + encodeURIComponent(query);
            }
            return;
        }

        if (event.key === 'Escape') {
            clearAndHideDropdown();
            input.blur();
        }
    });
})();
