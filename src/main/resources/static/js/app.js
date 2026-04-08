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
        initializeRevealBlocks();
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

    function initializeRevealBlocks() {
        const revealTargets = document.querySelectorAll(".ff-animate-in, .ff-surface-card, .ff-section-header");

        if (revealTargets.length === 0) {
            return;
        }

        if (!("IntersectionObserver" in window)) {
            revealTargets.forEach((element) => element.classList.add("is-visible"));
            return;
        }

        const observer = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                if (!entry.isIntersecting) {
                    return;
                }

                entry.target.classList.add("is-visible");
                observer.unobserve(entry.target);
            });
        }, {
            threshold: 0.15,
            rootMargin: "0px 0px -40px 0px"
        });

        revealTargets.forEach((element) => {
            element.classList.add("ff-reveal");
            observer.observe(element);
        });
    }
})();
