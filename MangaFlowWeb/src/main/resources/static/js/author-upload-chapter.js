(() => {
	const dropzone = document.getElementById('dropzone');
	const input = document.getElementById('pages');
	const selectBtn = document.getElementById('selectFilesBtn');
	const clearBtn = document.getElementById('clearFilesBtn');
	const previewWrap = document.getElementById('previewWrap');
	const previewGrid = document.getElementById('previewGrid');
	const scheduleBtn = document.getElementById('scheduleBtn');
	const scheduleWrap = document.getElementById('scheduleWrap');

	// This script should be safe if included on other pages.
	if (!dropzone || !input || !previewWrap || !previewGrid) return;

	/** @type {File[]} */
	let files = [];

	const syncInputFiles = () => {
		const dt = new DataTransfer();
		files.forEach((f) => dt.items.add(f));
		input.files = dt.files;
	};

	const renderPreview = () => {
		previewGrid.innerHTML = '';
		if (!files.length) {
			previewWrap.hidden = true;
			return;
		}

		previewWrap.hidden = false;
		files.forEach((file, idx) => {
			const url = URL.createObjectURL(file);

			const item = document.createElement('div');
			item.className = 'mf-uc-preview-item';

			const img = document.createElement('img');
			img.src = url;
			img.alt = file.name;
			img.onload = () => URL.revokeObjectURL(url);

			const badge = document.createElement('div');
			badge.className = 'mf-uc-preview-badge';
			badge.textContent = `Page ${idx + 1}`;

			item.appendChild(img);
			item.appendChild(badge);
			previewGrid.appendChild(item);
		});
	};

	const addFiles = (newFiles) => {
		const list = Array.from(newFiles || []);
		if (!list.length) return;

		// De-dupe by (name,size,lastModified)
		const seen = new Set(files.map((f) => `${f.name}__${f.size}__${f.lastModified}`));
		list.forEach((f) => {
			const key = `${f.name}__${f.size}__${f.lastModified}`;
			if (!seen.has(key)) {
				files.push(f);
				seen.add(key);
			}
		});

		syncInputFiles();
		renderPreview();
	};

	const clearFiles = () => {
		files = [];
		syncInputFiles();
		renderPreview();
	};

	const openPicker = () => input.click();

	// Open file picker
	selectBtn?.addEventListener('click', (e) => {
		e.preventDefault();
		openPicker();
	});

	dropzone.addEventListener('click', (e) => {
		const target = /** @type {HTMLElement} */ (e.target);
		if (target?.closest && target.closest('#selectFilesBtn')) return;
		openPicker();
	});

	dropzone.addEventListener('keydown', (e) => {
		if (e.key === 'Enter' || e.key === ' ') {
			e.preventDefault();
			openPicker();
		}
	});

	// Input change
	input.addEventListener('change', () => {
		addFiles(input.files);
	});

	// Clear button
	clearBtn?.addEventListener('click', (e) => {
		e.preventDefault();
		clearFiles();
	});

	// Drag & drop visual state
	['dragenter', 'dragover'].forEach((evt) => {
		dropzone.addEventListener(evt, (e) => {
			e.preventDefault();
			e.stopPropagation();
			dropzone.classList.add('is-dragover');
		});
	});

	['dragleave', 'drop'].forEach((evt) => {
		dropzone.addEventListener(evt, (e) => {
			e.preventDefault();
			e.stopPropagation();
			dropzone.classList.remove('is-dragover');
		});
	});

	dropzone.addEventListener('drop', (e) => {
		const dt = e.dataTransfer;
		if (!dt) return;
		addFiles(dt.files);
	});

	// Schedule toggle
	if (scheduleBtn && scheduleWrap) {
		scheduleBtn.addEventListener('click', () => {
			scheduleWrap.hidden = !scheduleWrap.hidden;
		});
	}
})();