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
        initializeClickableRows();
        initializeRevealBlocks();
        initializePaymentFormAssistant();
    });

    function updateCurrentYear() {
        document.querySelectorAll("[data-current-year]").forEach((element) => {
            element.textContent = String(new Date().getFullYear());
        });
    }

    function initializeClickableRows() {
        document.querySelectorAll("[data-row-href]").forEach((row) => {
            row.addEventListener("click", (event) => {
                if (event.target.closest("a, button, form, input, select, textarea, label")) {
                    return;
                }

                window.location.href = row.dataset.rowHref;
            });

            row.addEventListener("keydown", (event) => {
                if (event.key !== "Enter" && event.key !== " ") {
                    return;
                }

                event.preventDefault();
                window.location.href = row.dataset.rowHref;
            });
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
            if (element.classList.contains("ff-chart-card") || element.querySelector("canvas")) {
                element.classList.add("is-visible");
                return;
            }

            element.classList.add("ff-reveal");
            observer.observe(element);
        });
    }

    function initializePaymentFormAssistant() {
        const form = document.querySelector("[data-payment-form]");
        if (!form) {
            return;
        }

        const userSelect = form.querySelector("[data-payment-user]");
        const planSelect = form.querySelector("[data-payment-plan]");
        const dueDateInput = form.querySelector("[data-payment-due-date]");
        const paymentDateInput = form.querySelector("[data-payment-date]");
        const statusSelect = form.querySelector("[data-payment-status]");
        const amountTarget = document.querySelector("[data-payment-amount]");
        const planNameTarget = document.querySelector("[data-payment-plan-name]");
        const duePreviewTarget = document.querySelector("[data-payment-due-preview]");

        if (!userSelect || !planSelect || !amountTarget || !planNameTarget || !dueDateInput || !paymentDateInput || !statusSelect || !duePreviewTarget) {
            return;
        }

        const toIsoDate = (dateValue) => {
            if (!dateValue) {
                return "";
            }

            const date = new Date(dateValue);
            if (Number.isNaN(date.getTime())) {
                return "";
            }

            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, "0");
            const day = String(date.getDate()).padStart(2, "0");
            return `${year}-${month}-${day}`;
        };

        const sumarDias = (fechaIso, dias) => {
            if (!fechaIso) {
                return "";
            }

            const base = new Date(`${fechaIso}T00:00:00`);
            if (Number.isNaN(base.getTime())) {
                return "";
            }

            base.setDate(base.getDate() + Number(dias || 0));
            return toIsoDate(base);
        };

        const updateSummary = () => {
            const selectedPlanOption = planSelect.options[planSelect.selectedIndex];
            const selectedUserOption = userSelect.options[userSelect.selectedIndex];
            const explicitPlanId = planSelect.value;

            if (!explicitPlanId && selectedUserOption?.dataset.planId) {
                planSelect.value = selectedUserOption.dataset.planId;
            }

            const currentPlanOption = planSelect.options[planSelect.selectedIndex];
            const sourceOption = currentPlanOption?.value ? currentPlanOption : selectedUserOption;
            const planName = sourceOption?.dataset.planNombre || "Se usara el plan asociado al usuario";
            const planPrice = sourceOption?.dataset.planPrecio;
            const planDuration = sourceOption?.dataset.planDuracion;
            const nextPayment = selectedUserOption?.dataset.nextPayment;

            planNameTarget.textContent = planName;
            amountTarget.textContent = planPrice
                ? utils.formatCurrency(planPrice)
                : "Se derivara automaticamente al guardar";

            const suggestedDueDate = nextPayment || sumarDias(toIsoDate(new Date()), planDuration || 30);

            if (!dueDateInput.value && suggestedDueDate) {
                dueDateInput.value = suggestedDueDate;
            }

            duePreviewTarget.textContent = dueDateInput.value
                ? utils.formatShortDate(dueDateInput.value)
                : "Se propone segun plan y proximo cobro del usuario";

            if (statusSelect.value === "PAGADO" && !paymentDateInput.value) {
                paymentDateInput.value = toIsoDate(new Date());
            }

            if (statusSelect.value !== "PAGADO") {
                paymentDateInput.value = "";
            }
        };

        userSelect.addEventListener("change", updateSummary);
        planSelect.addEventListener("change", updateSummary);
        dueDateInput.addEventListener("change", updateSummary);
        statusSelect.addEventListener("change", updateSummary);
        updateSummary();
    }
})();
