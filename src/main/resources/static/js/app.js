(() => {
    const SPLASH_SESSION_KEY = "flacofitness:splash-seen:v1";
    const THEME_STORAGE_KEY = "flacofitness:theme:v1";
    const LIGHT_THEME = "light";
    const DARK_THEME = "dark";
    const PAGE_TRANSITION_DELAY = 140;
    const PAGE_VISIBILITY_FAILSAFE_DELAY = 1800;
    const motionQuery = window.matchMedia("(prefers-reduced-motion: reduce)");

    document.documentElement.classList.add("ff-motion-enabled");
    initializeThemeState();

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
    primeSplashVisibility();

    document.addEventListener("DOMContentLoaded", () => {
        initializeSplashScreen();
        initializePageTransitions();
        initializeGlobalLoadingOverlay();
        updateCurrentYear();
        initializeClickableRows();
        initializeRevealBlocks();
        initializeScrollableRails();
        initializePaymentFormAssistant();
        initializeThemeToggle();
    });

    window.addEventListener("pageshow", () => {
        document.body.classList.remove("ff-page-exiting");
        revealCurrentPage();
    });

    window.addEventListener("load", () => {
        forceShellVisibility();
    }, { once: true });

    window.setTimeout(() => {
        forceShellVisibility();
    }, PAGE_VISIBILITY_FAILSAFE_DELAY);

    function primeSplashVisibility() {
        const splash = document.querySelector("[data-app-splash]");

        if (!splash || !shouldSkipSplash()) {
            return;
        }

        splash.classList.add("ff-app-splash-hidden");
        document.body.classList.add("ff-splash-skipped");
    }

    function initializeSplashScreen() {
        const splash = document.querySelector("[data-app-splash]");

        if (!splash || shouldSkipSplash()) {
            hideSplashImmediately(splash);
            revealCurrentPage();
            return;
        }

        document.body.classList.add("ff-splash-active");
        splash.setAttribute("aria-hidden", "false");
        rememberSplashSeen();

        window.setTimeout(() => {
            splash.classList.add("ff-app-splash-leaving");
            document.body.classList.remove("ff-splash-active");
            let finished = false;

            const finish = () => {
                if (finished) {
                    return;
                }
                finished = true;
                splash.classList.add("ff-app-splash-hidden");
                splash.setAttribute("aria-hidden", "true");
                revealCurrentPage();
            };

            splash.addEventListener("transitionend", finish, { once: true });
            window.setTimeout(finish, 420);
        }, 950);
    }

    function hideSplashImmediately(splash) {
        if (!splash) {
            return;
        }

        splash.classList.add("ff-app-splash-hidden");
        splash.setAttribute("aria-hidden", "true");
        document.body.classList.add("ff-splash-skipped");
    }

    function revealCurrentPage() {
        window.requestAnimationFrame(() => {
            document.body.classList.add("ff-page-ready");
        });
    }

    function forceShellVisibility() {
        const splash = document.querySelector("[data-app-splash]");
        revealCurrentPage();
        deactivateGlobalLoading();

        if (!splash || splash.classList.contains("ff-app-splash-hidden")) {
            return;
        }

        hideSplashImmediately(splash);
    }

    function shouldSkipSplash() {
        if (motionQuery.matches) {
            return true;
        }

        try {
            return window.sessionStorage.getItem(SPLASH_SESSION_KEY) === "true";
        } catch (error) {
            return true;
        }
    }

    function rememberSplashSeen() {
        try {
            window.sessionStorage.setItem(SPLASH_SESSION_KEY, "true");
        } catch (error) {
            // If sessionStorage is blocked, the splash remains harmlessly per page load.
        }
    }

    function initializePageTransitions() {
        document.addEventListener("click", (event) => {
            const link = event.target.closest("a[href]");

            if (!link || !shouldTransitionLink(event, link)) {
                return;
            }

            event.preventDefault();
            activateGlobalLoading();
            navigateWithTransition(link.href);
        });
    }

    function initializeGlobalLoadingOverlay() {
        const overlay = document.querySelector("[data-app-loading-overlay]");
        if (!overlay) {
            return;
        }

        document.addEventListener("submit", (event) => {
            const form = event.target;
            if (!(form instanceof HTMLFormElement)) {
                return;
            }

            if (form.hasAttribute("data-no-loading")) {
                return;
            }

            window.setTimeout(() => {
                activateGlobalLoading();
            }, 0);
        });

        window.addEventListener("pageshow", () => {
            deactivateGlobalLoading();
        });
    }

    function activateGlobalLoading() {
        document.body.classList.add("ff-global-loading");
    }

    function deactivateGlobalLoading() {
        document.body.classList.remove("ff-global-loading");
    }

    function initializeThemeState() {
        const storedTheme = readStoredTheme();
        applyTheme(storedTheme || LIGHT_THEME, false);
    }

    function initializeThemeToggle() {
        const toggle = document.querySelector("[data-theme-toggle]");
        if (!toggle) {
            return;
        }

        syncThemeToggle(toggle, getCurrentTheme());
        toggle.addEventListener("click", () => {
            const nextTheme = getCurrentTheme() === DARK_THEME ? LIGHT_THEME : DARK_THEME;
            applyTheme(nextTheme, true);
            syncThemeToggle(toggle, nextTheme);
        });

        window.addEventListener("ff:themechange", (event) => {
            syncThemeToggle(toggle, event.detail?.theme || getCurrentTheme());
        });
    }

    function syncThemeToggle(toggle, theme) {
        const isDark = theme === DARK_THEME;
        toggle.setAttribute("aria-label", isDark ? "Activar modo claro" : "Activar modo oscuro");
        toggle.setAttribute("title", isDark ? "Cambiar a claro" : "Cambiar a oscuro");
        toggle.classList.toggle("is-dark", isDark);
    }

    function applyTheme(theme, persist) {
        const nextTheme = theme === DARK_THEME ? DARK_THEME : LIGHT_THEME;
        document.documentElement.setAttribute("data-theme", nextTheme);
        document.body.classList.toggle("ff-theme-dark", nextTheme === DARK_THEME);

        if (persist) {
            storeTheme(nextTheme);
        }

        window.dispatchEvent(new CustomEvent("ff:themechange", {
            detail: { theme: nextTheme }
        }));
    }

    function getCurrentTheme() {
        return document.documentElement.getAttribute("data-theme") === DARK_THEME ? DARK_THEME : LIGHT_THEME;
    }

    function readStoredTheme() {
        try {
            const stored = window.localStorage.getItem(THEME_STORAGE_KEY);
            return stored === DARK_THEME ? DARK_THEME : LIGHT_THEME;
        } catch (error) {
            return LIGHT_THEME;
        }
    }

    function storeTheme(theme) {
        try {
            window.localStorage.setItem(THEME_STORAGE_KEY, theme);
        } catch (error) {
            // Ignore storage failures gracefully.
        }
    }

    function shouldTransitionLink(event, link) {
        if (event.defaultPrevented || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) {
            return false;
        }

        if (link.target || link.download || link.hasAttribute("data-no-transition") || link.hasAttribute("data-bs-toggle")) {
            return false;
        }

        const rawHref = link.getAttribute("href") || "";
        if (rawHref.startsWith("#") || rawHref.startsWith("javascript:")) {
            return false;
        }

        const url = new URL(link.href, window.location.href);

        if (url.origin !== window.location.origin || !["http:", "https:"].includes(url.protocol)) {
            return false;
        }

        const currentUrl = new URL(window.location.href);

        if (url.pathname === currentUrl.pathname && url.search === currentUrl.search && url.hash) {
            return false;
        }

        return url.href !== currentUrl.href;
    }

    function navigateWithTransition(destination) {
        if (!destination || motionQuery.matches) {
            window.location.href = destination;
            return;
        }

        document.body.classList.add("ff-page-exiting");
        window.setTimeout(() => {
            window.location.href = destination;
        }, PAGE_TRANSITION_DELAY);
    }

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

                navigateWithTransition(row.dataset.rowHref);
            });

            row.addEventListener("keydown", (event) => {
                if (event.key !== "Enter" && event.key !== " ") {
                    return;
                }

                event.preventDefault();
                navigateWithTransition(row.dataset.rowHref);
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

    function initializeScrollableRails() {
        document.querySelectorAll("[data-scroll-rail]").forEach((rail) => {
            const railId = rail.getAttribute("id");
            if (!railId) {
                return;
            }

            const prevButtons = document.querySelectorAll(`[data-scroll-rail-prev][data-scroll-rail-target="${railId}"]`);
            const nextButtons = document.querySelectorAll(`[data-scroll-rail-next][data-scroll-rail-target="${railId}"]`);

            const scrollByCard = (direction) => {
                const card = rail.querySelector(":scope > *");
                const gap = Number.parseFloat(window.getComputedStyle(rail).columnGap || window.getComputedStyle(rail).gap || "0");
                const offset = card ? card.getBoundingClientRect().width + gap : rail.clientWidth * 0.8;
                rail.scrollBy({
                    left: offset * direction,
                    behavior: motionQuery.matches ? "auto" : "smooth"
                });
            };

            prevButtons.forEach((button) => {
                button.addEventListener("click", () => scrollByCard(-1));
            });

            nextButtons.forEach((button) => {
                button.addEventListener("click", () => scrollByCard(1));
            });
        });
    }
})();
