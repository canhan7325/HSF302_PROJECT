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

	let draggingIndex = null;

	const syncInputFiles = () => {
		const dt = new DataTransfer();
		files.forEach((f) => dt.items.add(f));
		input.files = dt.files;
	};

	const moveFile = (fromIndex, toIndex) => {
		if (fromIndex == null || toIndex == null) return;
		if (fromIndex === toIndex) return;
		if (fromIndex < 0 || fromIndex >= files.length) return;
		if (toIndex < 0 || toIndex >= files.length) return;

		const next = files.slice();
		const [picked] = next.splice(fromIndex, 1);
		next.splice(toIndex, 0, picked);
		files = next;
		syncInputFiles();
		renderPreview();
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
			item.setAttribute('draggable', 'true');
			item.dataset.index = String(idx);
			item.title = 'Drag to reorder';

			item.addEventListener('dragstart', (e) => {
				draggingIndex = idx;
				item.classList.add('is-dragging');
				try {
					// Required in some browsers to start DnD
					e.dataTransfer?.setData('text/plain', String(idx));
				} catch (_) {}
				e.dataTransfer && (e.dataTransfer.effectAllowed = 'move');
			});

			item.addEventListener('dragend', () => {
				item.classList.remove('is-dragging');
				draggingIndex = null;
				previewGrid.querySelectorAll('.is-drop-target').forEach((el) => el.classList.remove('is-drop-target'));
			});

			item.addEventListener('dragover', (e) => {
				e.preventDefault();
				item.classList.add('is-drop-target');
				e.dataTransfer && (e.dataTransfer.dropEffect = 'move');
			});

			item.addEventListener('dragleave', () => {
				item.classList.remove('is-drop-target');
			});

			item.addEventListener('drop', (e) => {
				e.preventDefault();
				item.classList.remove('is-drop-target');

				const toIndex = Number(item.dataset.index);
				let fromIndex = draggingIndex;
				if (fromIndex == null) {
					const raw = e.dataTransfer?.getData('text/plain');
					if (raw != null && raw !== '') fromIndex = Number(raw);
				}
				if (Number.isNaN(fromIndex) || Number.isNaN(toIndex)) return;
				moveFile(fromIndex, toIndex);
			});

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

	// Drag & drop visual state (dropzone file add)
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