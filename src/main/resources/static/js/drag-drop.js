document.addEventListener("DOMContentLoaded", () => {
    initializeUserAssignment();
});

function initializeUserAssignment() {
    const originalSelect = document.getElementById("usuarioIds");

    if (!originalSelect || typeof Sortable === "undefined") {
        return;
    }

    const mountPoint = originalSelect.closest("[data-user-assignment]");
    if (!mountPoint) {
        return;
    }

    const allUsers = Array.from(originalSelect.options).map((option) => ({
        id: option.value,
        name: option.textContent.trim(),
        photoUrl: option.dataset.photoUrl || "/img/avatar-placeholder.svg",
        selected: option.selected
    }));

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
            <span>Marca y arrastra</span>
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

    mountPoint.innerHTML = "";
    mountPoint.appendChild(container);

    originalSelect.classList.add("d-none");
    originalSelect.setAttribute("multiple", "multiple");
    mountPoint.appendChild(originalSelect);

    const availableList = container.querySelector('[data-list="available"]');
    const assignedList = container.querySelector('[data-list="assigned"]');

    allUsers.forEach((user) => {
        const item = createUserItem(user);
        (user.selected ? assignedList : availableList).appendChild(item);
    });

    initializeSelectableItems(container);
    initializeFilters(container);
    initializeSortableLists(availableList, assignedList, originalSelect, allUsers, container);
    syncSelectFromList(assignedList, originalSelect, allUsers);
    updateCounts(container, availableList, assignedList);
}

function initializeSelectableItems(container) {
    container.addEventListener("click", (event) => {
        const item = event.target.closest(".ff-dual-list-item");
        if (!item) {
            return;
        }

        item.classList.toggle("is-selected");
    });
}

function initializeFilters(container) {
    container.querySelectorAll("[data-filter]").forEach((input) => {
        const listKey = input.dataset.filter;
        const list = container.querySelector(`[data-list="${listKey}"]`);

        input.addEventListener("input", () => {
            const query = input.value.trim().toLowerCase();
            Array.from(list.children).forEach((item) => {
                const name = item.dataset.userName.toLowerCase();
                item.style.display = name.includes(query) ? "" : "none";
            });
        });
    });
}

function initializeSortableLists(availableList, assignedList, originalSelect, allUsers, container) {
    const sortableOptions = {
        group: "users",
        animation: 180,
        easing: "cubic-bezier(0.4, 0, 0.2, 1)",
        ghostClass: "ff-dual-list-ghost",
        chosenClass: "ff-dual-list-chosen",
        dragClass: "ff-dual-list-drag",
        onStart(event) {
            const draggedItem = event.item;

            if (!draggedItem.classList.contains("is-selected")) {
                clearSelection(event.from);
                draggedItem.classList.add("is-selected");
            }
        },
        onEnd(event) {
            moveSelectedCompanions(event);
            clearSelection(event.from);
            clearSelection(event.to);
            syncSelectFromList(assignedList, originalSelect, allUsers);
            updateCounts(container, availableList, assignedList);
        }
    };

    Sortable.create(availableList, sortableOptions);
    Sortable.create(assignedList, sortableOptions);
}

function moveSelectedCompanions(event) {
    if (event.from === event.to || !event.item.classList.contains("is-selected")) {
        return;
    }

    const companions = Array.from(event.from.querySelectorAll(".ff-dual-list-item.is-selected"))
        .filter((item) => item !== event.item);

    if (companions.length === 0) {
        return;
    }

    let insertAfter = event.item;

    companions.forEach((item) => {
        insertAfter.insertAdjacentElement("afterend", item);
        insertAfter = item;
    });
}

function clearSelection(list) {
    if (!list) {
        return;
    }

    list.querySelectorAll(".ff-dual-list-item.is-selected").forEach((item) => {
        item.classList.remove("is-selected");
    });
}

function createUserItem(user) {
    const item = document.createElement("li");
    item.className = "ff-dual-list-item";
    item.dataset.userId = user.id;
    item.dataset.userName = user.name;
    item.innerHTML = `
        <img class="ff-avatar ff-avatar-sm ff-dual-list-avatar"
             src="${escapeHtml(user.photoUrl)}"
             alt="Avatar de ${escapeHtml(user.name)}">
        <span class="ff-dual-list-item-grip" aria-hidden="true">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                <circle cx="9" cy="5" r="1.5"/><circle cx="15" cy="5" r="1.5"/>
                <circle cx="9" cy="12" r="1.5"/><circle cx="15" cy="12" r="1.5"/>
                <circle cx="9" cy="19" r="1.5"/><circle cx="15" cy="19" r="1.5"/>
            </svg>
        </span>
        <span class="ff-dual-list-item-name">${escapeHtml(user.name)}</span>
    `;
    return item;
}

function syncSelectFromList(assignedList, originalSelect, allUsers) {
    const assignedIds = new Set(
        Array.from(assignedList.children).map((item) => item.dataset.userId)
    );

    originalSelect.innerHTML = "";

    allUsers.forEach((user) => {
        const option = document.createElement("option");
        option.value = user.id;
        option.textContent = user.name;
        option.dataset.photoUrl = user.photoUrl;
        if (assignedIds.has(user.id)) {
            option.selected = true;
            option.setAttribute("selected", "selected");
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
