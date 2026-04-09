/* ─────────────────────────────────────────────────────────────
   FlacoFitness — Drag & Drop User Assignment (SortableJS)
   Replaces the native <select multiple> with a dual-list
   interface where users can be dragged between "available"
   and "assigned" panels.
   ───────────────────────────────────────────────────────────── */

document.addEventListener("DOMContentLoaded", () => {
    initializeUserAssignment();
});

function initializeUserAssignment() {
    const originalSelect = document.getElementById("usuarioIds");

    if (!originalSelect || typeof Sortable === "undefined") {
        return;
    }

    /* Build data from the original <select> */
    const allUsers = [];
    const selectedIds = new Set();

    Array.from(originalSelect.options).forEach((opt) => {
        allUsers.push({ id: opt.value, name: opt.textContent.trim() });
        if (opt.selected) {
            selectedIds.add(opt.value);
        }
    });

    /* Hide the original select */
    const mountPoint = originalSelect.closest("[data-user-assignment]");
    if (!mountPoint) {
        return;
    }

    /* Build the dual-list UI */
    const container = document.createElement("div");
    container.className = "ff-dual-list";
    container.innerHTML = `
        <div class="ff-dual-list-panel">
            <div class="ff-dual-list-header">
                <h5 class="ff-dual-list-title">Disponibles</h5>
                <span class="ff-dual-list-count" data-count="available">0</span>
            </div>
            <div class="ff-dual-list-search">
                <input type="text" placeholder="Filtrar usuarios..." data-filter="available" autocomplete="off">
            </div>
            <ul class="ff-dual-list-items" data-list="available"></ul>
        </div>
        <div class="ff-dual-list-divider">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
                 fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="7 8 3 12 7 16"/>
                <polyline points="17 8 21 12 17 16"/>
                <line x1="3" y1="12" x2="21" y2="12"/>
            </svg>
            <span>Arrastra</span>
        </div>
        <div class="ff-dual-list-panel ff-dual-list-panel--assigned">
            <div class="ff-dual-list-header">
                <h5 class="ff-dual-list-title">Asignados</h5>
                <span class="ff-dual-list-count ff-dual-list-count--active" data-count="assigned">0</span>
            </div>
            <div class="ff-dual-list-search">
                <input type="text" placeholder="Filtrar asignados..." data-filter="assigned" autocomplete="off">
            </div>
            <ul class="ff-dual-list-items" data-list="assigned"></ul>
        </div>
    `;

    /* Replace select content with dual-list */
    mountPoint.innerHTML = "";
    mountPoint.appendChild(container);

    /* Keep the hidden select for form submission */
    originalSelect.classList.add("d-none");
    originalSelect.setAttribute("multiple", "multiple");
    mountPoint.appendChild(originalSelect);

    /* Populate lists */
    const availableList = container.querySelector('[data-list="available"]');
    const assignedList = container.querySelector('[data-list="assigned"]');

    allUsers.forEach((user) => {
        const li = createUserItem(user);
        if (selectedIds.has(user.id)) {
            assignedList.appendChild(li);
        } else {
            availableList.appendChild(li);
        }
    });

    /* Init SortableJS on both lists */
    const sortableOptions = {
        group: "users",
        animation: 200,
        easing: "cubic-bezier(0.4, 0, 0.2, 1)",
        ghostClass: "ff-dual-list-ghost",
        chosenClass: "ff-dual-list-chosen",
        dragClass: "ff-dual-list-drag",
        delay: 80,
        delayOnTouchOnly: true,
        touchStartThreshold: 4,
        fallbackOnBody: true,
        swapThreshold: 0.65,
        onEnd() {
            syncSelectFromList(assignedList, originalSelect, allUsers);
            updateCounts(container, availableList, assignedList);
        },
    };

    Sortable.create(availableList, sortableOptions);
    Sortable.create(assignedList, sortableOptions);

    /* Initial state */
    syncSelectFromList(assignedList, originalSelect, allUsers);
    updateCounts(container, availableList, assignedList);

    /* Filter search */
    container.querySelectorAll("[data-filter]").forEach((input) => {
        const listKey = input.dataset.filter;
        const list = container.querySelector(`[data-list="${listKey}"]`);

        input.addEventListener("input", () => {
            const query = input.value.trim().toLowerCase();
            Array.from(list.children).forEach((li) => {
                const name = li.textContent.toLowerCase();
                li.style.display = name.includes(query) ? "" : "none";
            });
        });
    });
}

function createUserItem(user) {
    const li = document.createElement("li");
    li.className = "ff-dual-list-item";
    li.dataset.userId = user.id;
    li.innerHTML = `
        <span class="ff-dual-list-item-grip">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24"
                 fill="currentColor">
                <circle cx="9" cy="5" r="1.5"/><circle cx="15" cy="5" r="1.5"/>
                <circle cx="9" cy="12" r="1.5"/><circle cx="15" cy="12" r="1.5"/>
                <circle cx="9" cy="19" r="1.5"/><circle cx="15" cy="19" r="1.5"/>
            </svg>
        </span>
        <span class="ff-dual-list-item-name">${escapeHtml(user.name)}</span>
    `;
    return li;
}

function syncSelectFromList(assignedList, originalSelect, allUsers) {
    /* Clear all options and re-create based on assigned list */
    originalSelect.innerHTML = "";

    const assignedIds = new Set(
        Array.from(assignedList.children).map((li) => li.dataset.userId)
    );

    allUsers.forEach((user) => {
        const option = document.createElement("option");
        option.value = user.id;
        option.textContent = user.name;
        if (assignedIds.has(user.id)) {
            option.selected = true;
        }
        originalSelect.appendChild(option);
    });
}

function updateCounts(container, availableList, assignedList) {
    const availableCount = container.querySelector('[data-count="available"]');
    const assignedCount = container.querySelector('[data-count="assigned"]');

    if (availableCount) {
        availableCount.textContent = String(availableList.children.length);
    }
    if (assignedCount) {
        assignedCount.textContent = String(assignedList.children.length);
    }
}

function escapeHtml(text) {
    const div = document.createElement("div");
    div.appendChild(document.createTextNode(text));
    return div.innerHTML;
}
