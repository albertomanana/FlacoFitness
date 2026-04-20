document.addEventListener("DOMContentLoaded", () => {
    initializeQuickCheckInPanel();
});

function initializeQuickCheckInPanel() {
    const panel = document.querySelector("[data-checkin-panel]");
    if (!panel) {
        return;
    }

    const searchInput = panel.querySelector("[data-checkin-search]");
    const selectVisibleButton = panel.querySelector("[data-checkin-select-visible]");
    const clearButton = panel.querySelector("[data-checkin-clear]");
    const submitButton = panel.querySelector("[data-checkin-submit]");
    const form = panel.querySelector("form");
    const counterTargets = document.querySelectorAll("[data-checkin-count]");
    const checkboxes = () => Array.from(panel.querySelectorAll("[data-checkin-user]"));
    const items = () => Array.from(panel.querySelectorAll("[data-checkin-item]"));

    const syncCardState = () => {
        checkboxes().forEach((checkbox) => {
            const card = checkbox.closest("[data-checkin-item]");
            if (!card) {
                return;
            }

            card.classList.toggle("is-selected", checkbox.checked);
        });
    };

    const updateSummary = () => {
        const selectedCount = checkboxes().filter((checkbox) => checkbox.checked).length;
        const label = selectedCount === 1 ? "1 seleccionado" : `${selectedCount} seleccionados`;

        counterTargets.forEach((target) => {
            target.textContent = label;
        });

        if (submitButton) {
            submitButton.disabled = selectedCount === 0;
            submitButton.classList.toggle("disabled", selectedCount === 0);
        }

        syncCardState();
    };

    const filterItems = () => {
        const query = (searchInput?.value || "").trim().toLowerCase();

        items().forEach((item) => {
            const hayCoincidencia = !query || (item.dataset.searchText || "").includes(query);
            item.classList.toggle("d-none", !hayCoincidencia);
        });
    };

    panel.addEventListener("change", (event) => {
        if (!event.target.matches("[data-checkin-user]")) {
            return;
        }

        updateSummary();
    });

    if (searchInput) {
        searchInput.addEventListener("input", filterItems);
    }

    if (selectVisibleButton) {
        selectVisibleButton.addEventListener("click", () => {
            items()
                    .filter((item) => !item.classList.contains("d-none"))
                    .forEach((item) => {
                        const checkbox = item.querySelector("[data-checkin-user]");
                        if (checkbox) {
                            checkbox.checked = true;
                        }
                    });
            updateSummary();
        });
    }

    if (clearButton) {
        clearButton.addEventListener("click", () => {
            checkboxes().forEach((checkbox) => {
                checkbox.checked = false;
            });
            updateSummary();
        });
    }

    if (form && submitButton) {
        form.addEventListener("submit", () => {
            if (submitButton.disabled) {
                return;
            }

            submitButton.disabled = true;
            submitButton.classList.add("disabled");
            submitButton.textContent = "Registrando...";
        });
    }

    filterItems();
    updateSummary();
}
