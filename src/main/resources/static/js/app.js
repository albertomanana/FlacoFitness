(() => {
    const utils = {
        formatInteger(value) {
            return new Intl.NumberFormat("es-ES", {
                maximumFractionDigits: 0
            }).format(Number(value || 0));
        },
        formatCurrency(value) {
            return new Intl.NumberFormat("es-ES", {
                style: "currency",
                currency: "EUR",
                maximumFractionDigits: 2
            }).format(Number(value || 0));
        },
        formatPeriod(period) {
            if (!period || !period.includes("-")) {
                return period || "";
            }

            const [year, month] = period.split("-");
            const date = new Date(Number(year), Number(month) - 1, 1);

            return new Intl.DateTimeFormat("es-ES", {
                month: "short",
                year: "numeric"
            }).format(date);
        },
        formatShortDate(dateValue) {
            if (!dateValue) {
                return "";
            }

            return new Intl.DateTimeFormat("es-ES", {
                day: "2-digit",
                month: "short"
            }).format(new Date(`${dateValue}T00:00:00`));
        },
        debounce(callback, delay = 150) {
            let timeoutId = null;

            return (...args) => {
                window.clearTimeout(timeoutId);
                timeoutId = window.setTimeout(() => callback(...args), delay);
            };
        }
    };

    window.ffUtils = utils;

    document.addEventListener("DOMContentLoaded", () => {
        updateCurrentYear();
        initializeTopbarSearch();
    });

    function updateCurrentYear() {
        document.querySelectorAll("[data-current-year]").forEach((element) => {
            element.textContent = String(new Date().getFullYear());
        });
    }

    function initializeTopbarSearch() {
        const searchInput = document.getElementById("topbarSearch");
        if (!searchInput) {
            return;
        }

        const syncSearch = utils.debounce((query) => {
            if (!Array.isArray(window.ffTables) || window.ffTables.length === 0) {
                return;
            }

            window.ffTables.forEach((tableInstance) => {
                tableInstance.search(query).draw();
            });
        }, 120);

        searchInput.addEventListener("input", (event) => {
            syncSearch(event.target.value.trim());
        });
    }
})();
