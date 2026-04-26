(() => {
    const SPLASH_SESSION_KEY = "flacofitness:splash-seen:v1";
    const THEME_STORAGE_KEY = "flacofitness:theme:v1";
    const BROWSER_TOKEN_STORAGE_KEY = "flacofitness:browser-token:v1";
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
        initializePayrollBuilder();
        initializeUserFormAssistant();
        initializeSelectSearch();
        initializePasswordToggles();
        initializeThemeToggle();
        initializeBrowserTokenMirror();
        initializeFab();
        initializeGlobalSearch();
        initializeUxMemoryActions();
        initializeUxEmptyStateActions();
        initializePersistentFilters();
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
        applyTheme(storedTheme || DARK_THEME, false);
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

    function initializePasswordToggles() {
        document.querySelectorAll("[data-password-toggle]").forEach((button) => {
            const field = button.closest(".ff-password-field");
            const input = field?.querySelector("[data-password-input]");

            if (!input) {
                return;
            }

            button.addEventListener("click", () => {
                const shouldShow = input.type === "password";
                input.type = shouldShow ? "text" : "password";
                button.setAttribute("aria-label", shouldShow ? "Ocultar contraseña" : "Mostrar contraseña");
                const icon = button.querySelector("i");
                if (icon) {
                    icon.classList.toggle("fa-eye", !shouldShow);
                    icon.classList.toggle("fa-eye-slash", shouldShow);
                }
            });
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
            if (stored === DARK_THEME || stored === LIGHT_THEME) {
                return stored;
            }
            return null;
        } catch (error) {
            return null;
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

    function initializePayrollBuilder() {
        const root = document.querySelector("[data-payroll-builder]");
        if (!root) {
            return;
        }

        const staffSelect = root.querySelector("[name='staffPerfilId']")
            || root.querySelector("[name='staffPerfil.id']");
        const periodInput = root.querySelector("[name='periodo']");
        const baseInput = root.querySelector("[data-payroll-base]");
        const bonusInput = root.querySelector("[data-payroll-bonus]");
        const deductionsInput = root.querySelector("[data-payroll-deductions]");
        const roleTarget = root.querySelector("[data-payroll-role]");
        const nameTarget = root.querySelector("[data-payroll-name]");
        const periodTarget = root.querySelector("[data-payroll-period]");
        const baseTarget = root.querySelector("[data-payroll-base-text]");
        const bonusTarget = root.querySelector("[data-payroll-bonus-text]");
        const deductionsTarget = root.querySelector("[data-payroll-deductions-text]");
        const netTarget = root.querySelector("[data-payroll-net-text]");
        const referenceTarget = root.querySelector("[data-payroll-reference]");

        if (!staffSelect || !periodInput || !baseInput || !bonusInput || !deductionsInput) {
            return;
        }

        const parseAmount = (value) => {
            const parsed = Number.parseFloat(value || "0");
            return Number.isFinite(parsed) ? parsed : 0;
        };

        const updatePreview = () => {
            const selectedOption = staffSelect.options[staffSelect.selectedIndex];
            const selectedLabel = selectedOption?.dataset.displayName
                || selectedOption?.textContent?.split(" - ")[0]?.trim()
                || "Selecciona un staff";
            const role = selectedOption?.dataset.role || "STAFF";
            const period = periodInput.value || "Periodo sin definir";

            if (selectedOption?.dataset.base && !baseInput.value) {
                baseInput.value = selectedOption.dataset.base;
            }
            if (selectedOption?.dataset.bonus && !bonusInput.value) {
                bonusInput.value = selectedOption.dataset.bonus;
            }
            if (selectedOption?.dataset.deducciones && !deductionsInput.value) {
                deductionsInput.value = selectedOption.dataset.deducciones;
            }

            const base = parseAmount(baseInput.value);
            const bonus = parseAmount(bonusInput.value);
            const deductions = parseAmount(deductionsInput.value);
            const net = base + bonus - deductions;

            roleTarget && (roleTarget.textContent = role);
            nameTarget && (nameTarget.textContent = selectedLabel);
            periodTarget && (periodTarget.textContent = period);
            baseTarget && (baseTarget.textContent = utils.formatCurrency(parseAmount(baseInput.value)));
            bonusTarget && (bonusTarget.textContent = utils.formatCurrency(parseAmount(bonusInput.value)));
            deductionsTarget && (deductionsTarget.textContent = utils.formatCurrency(parseAmount(deductionsInput.value)));
            netTarget && (netTarget.textContent = utils.formatCurrency(net));
            referenceTarget && (referenceTarget.textContent = selectedOption?.value
                ? `NOM-${String(period).replace("-", "")}-${selectedOption.value}`
                : "se genera al guardar");
        };

        [staffSelect, periodInput, baseInput, bonusInput, deductionsInput].forEach((element) => {
            element.addEventListener("change", updatePreview);
            element.addEventListener("input", updatePreview);
        });

        updatePreview();
    }

    function initializeUserFormAssistant() {
        const form = document.querySelector("[data-ff-user-form]");
        if (!form) {
            return;
        }

        const emailInput = form.querySelector("[data-ff-username-source]");
        const usernameInput = form.querySelector("[data-ff-username-input]");
        const photoInput = form.querySelector("[data-ff-photo-input]");
        const photoPreview = form.querySelector("[data-ff-photo-preview]");
        const photoPreviewSecondary = form.querySelector("[data-ff-photo-preview-secondary]");
        const namePreview = form.querySelector("[data-ff-name-preview]");
        const emailPreview = form.querySelector("[data-ff-email-preview]");
        const usernamePreview = form.querySelector("[data-ff-username-preview]");
        const firstNameInput = form.querySelector("[name='nombre']");
        const lastNameInput = form.querySelector("[name='apellidos']");

        if (!emailInput || !usernameInput) {
            return;
        }

        const originalPhoto = photoPreview?.getAttribute("src") || photoPreviewSecondary?.getAttribute("src") || "";
        let usernameTouched = usernameInput.value.trim().length > 0;

        const slugifyUsername = (value) => String(value || "")
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .replace(/@.*$/, "")
            .replace(/[^a-zA-Z0-9._-]/g, "")
            .toLowerCase();

        const updatePreview = () => {
            const fullName = [firstNameInput?.value, lastNameInput?.value]
                .map((part) => String(part || "").trim())
                .filter(Boolean)
                .join(" ");

            if (namePreview) {
                namePreview.textContent = fullName || "Nuevo usuario";
            }
            if (emailPreview) {
                emailPreview.textContent = emailInput.value.trim() || "correo@ejemplo.com";
            }
            if (usernamePreview) {
                usernamePreview.textContent = `@${usernameInput.value.trim() || "username"}`;
            }
        };

        const syncGeneratedUsername = () => {
            if (!usernameTouched) {
                usernameInput.value = slugifyUsername(emailInput.value.trim());
            }
            updatePreview();
        };

        const updatePhotoPreview = (file) => {
            const nextSource = !file ? originalPhoto : URL.createObjectURL(file);
            if (photoPreview) {
                photoPreview.src = nextSource;
            }
            if (photoPreviewSecondary) {
                photoPreviewSecondary.src = nextSource;
            }
        };

        usernameInput.addEventListener("input", () => {
            usernameTouched = usernameInput.value.trim().length > 0;
            updatePreview();
        });

        emailInput.addEventListener("input", syncGeneratedUsername);
        firstNameInput?.addEventListener("input", updatePreview);
        lastNameInput?.addEventListener("input", updatePreview);
        photoInput?.addEventListener("change", () => updatePhotoPreview(photoInput.files?.[0]));

        syncGeneratedUsername();
        updatePreview();
    }

    function initializeSelectSearch() {
        document.querySelectorAll("[data-ff-select-search]").forEach((input) => {
            const targetId = input.dataset.selectSearchTarget;
            const select = targetId ? document.getElementById(targetId) : null;
            if (!select) {
                return;
            }

            const normalize = (value) => String(value || "")
                .normalize("NFD")
                .replace(/[\u0300-\u036f]/g, "")
                .toLowerCase()
                .trim();

            const options = Array.from(select.options).map((option) => {
                option.dataset.ffSearchText = normalize(option.textContent || "");
                return option;
            });

            let feedback = document.querySelector(`[data-ff-select-search-feedback="${targetId}"]`);
            if (!feedback) {
                feedback = document.createElement("div");
                feedback.className = "form-text text-warning d-none";
                feedback.dataset.ffSelectSearchFeedback = targetId;
                select.insertAdjacentElement("afterend", feedback);
            }

            const renderOptions = () => {
                const term = normalize(input.value);
                const selectedValue = select.value;
                let visibleCount = 0;

                options.forEach((option, index) => {
                    if (index === 0 || option.value === selectedValue) {
                        option.hidden = false;
                        return;
                    }
                    const visible = !term || option.dataset.ffSearchText.includes(term);
                    option.hidden = !visible;
                    if (visible) {
                        visibleCount++;
                    }
                });

                feedback.textContent = input.dataset.noResults || "Sin coincidencias para la busqueda actual.";
                feedback.classList.toggle("d-none", visibleCount > 0 || !term);
            };

            input.addEventListener("input", renderOptions);
            renderOptions();
        });
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

    function initializeBrowserTokenMirror() {
        const browserToken = readCookie("ff_browser_token");
        if (!browserToken) {
            return;
        }

        try {
            window.localStorage.setItem(BROWSER_TOKEN_STORAGE_KEY, browserToken);
        } catch (error) {
            // Ignore storage failures gracefully.
        }
    }

    function initializeFab() {
        const fab = document.querySelector("[data-fab]");
        if (!fab) {
            return;
        }

        const trigger = fab.querySelector("[data-fab-trigger]");
        const menu = fab.querySelector("[data-fab-menu]");
        if (!trigger || !menu) {
            return;
        }

        const closeFab = () => fab.classList.remove("is-open");

        trigger.addEventListener("click", () => {
            fab.classList.toggle("is-open");
        });

        document.addEventListener("click", (event) => {
            if (!fab.contains(event.target)) {
                closeFab();
            }
        });

        document.addEventListener("keydown", (event) => {
            if (event.key === "Escape") {
                closeFab();
            }
        });
    }

    function initializeGlobalSearch() {
        const root = document.querySelector("[data-global-search]");
        if (!root) {
            return;
        }

        const input = root.querySelector("[data-global-search-input]");
        const results = root.querySelector("[data-global-search-results]");
        if (!input || !results) {
            return;
        }

        const closeResults = () => {
            results.innerHTML = "";
            results.classList.add("d-none");
        };

        const renderResults = (payload) => {
            const groups = Array.isArray(payload?.groups) ? payload.groups : [];
            if (groups.length === 0) {
                closeResults();
                return;
            }

            results.innerHTML = groups.map((group) => `
                <section class="ff-search-results-group">
                    <header>${escapeHtml(group.label || "Resultados")}</header>
                    ${(group.items || []).map((item) => `
                        <a class="ff-search-results-item" href="${item.url}">
                            <span>
                                <strong>${escapeHtml(item.title || "Resultado")}</strong>
                                <small>${escapeHtml(item.subtitle || "")}</small>
                            </span>
                            <em>${escapeHtml(item.group || "")}</em>
                        </a>
                    `).join("")}
                </section>
            `).join("");
            results.classList.remove("d-none");
        };

        const debouncedSearch = utils.debounce(async () => {
            const query = input.value.trim();
            if (query.length < 2) {
                closeResults();
                return;
            }

            try {
                const response = await fetch(`/api/busqueda/global?q=${encodeURIComponent(query)}`, {
                    headers: { Accept: "application/json" }
                });
                if (!response.ok) {
                    closeResults();
                    return;
                }
                renderResults(await response.json());
            } catch (error) {
                console.error(error);
                closeResults();
            }
        }, 180);

        input.addEventListener("input", debouncedSearch);
        document.addEventListener("click", (event) => {
            if (!root.contains(event.target)) {
                closeResults();
            }
        });
        input.addEventListener("keydown", (event) => {
            if (event.key === "Escape") {
                closeResults();
            }
        });
    }

    function initializeUxMemoryActions() {
        document.querySelectorAll("[data-ux-tooltip]").forEach((tooltip) => {
            const dismissButton = tooltip.querySelector("[data-ux-tooltip-dismiss]");
            const moduleKey = tooltip.dataset.moduleKey;
            if (!dismissButton || !moduleKey) {
                return;
            }

            dismissButton.addEventListener("click", async () => {
                await postUxState("/ux/tooltip/seen", { moduleKey });
                tooltip.remove();
            });
        });
    }

    function initializeUxEmptyStateActions() {
        document.querySelectorAll("[data-ux-empty-state]").forEach((panel) => {
            const dismissButton = panel.querySelector("[data-ux-empty-dismiss]");
            const moduleKey = panel.dataset.moduleKey;
            if (!dismissButton || !moduleKey) {
                return;
            }

            dismissButton.addEventListener("click", async () => {
                await postUxState("/ux/empty-state/dismiss", { moduleKey });
                panel.remove();
            });
        });
    }

    function initializePersistentFilters() {
        document.querySelectorAll("form[data-filter-memory-key]").forEach((form) => {
            const key = form.dataset.filterMemoryKey;
            if (!key) {
                return;
            }

            restoreFormState(form, key);
            form.addEventListener("change", () => persistFormState(form, key));
            form.addEventListener("submit", () => persistFormState(form, key));
        });
    }

    async function postUxState(url, payload) {
        const body = new URLSearchParams(payload);
        try {
            await fetch(url, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                },
                body: body.toString()
            });
        } catch (error) {
            console.error(error);
        }
    }

    function persistFormState(form, key) {
        const values = {};
        new FormData(form).forEach((value, field) => {
            values[field] = value;
        });

        try {
            window.localStorage.setItem(`ff:filters:${key}`, JSON.stringify(values));
        } catch (error) {
            // Ignore storage failures gracefully.
        }
    }

    function restoreFormState(form, key) {
        try {
            const raw = window.localStorage.getItem(`ff:filters:${key}`);
            if (!raw) {
                return;
            }

            const values = JSON.parse(raw);
            Object.entries(values).forEach(([field, value]) => {
                const target = form.elements.namedItem(field);
                if (target && "value" in target) {
                    target.value = value;
                }
            });
        } catch (error) {
            // Ignore storage failures gracefully.
        }
    }

    function readCookie(name) {
        const prefix = `${name}=`;
        return document.cookie.split(";").map((value) => value.trim())
            .find((value) => value.startsWith(prefix))
            ?.slice(prefix.length) || null;
    }

    function escapeHtml(value) {
        return String(value || "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#39;");
    }
})();
