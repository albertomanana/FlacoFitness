(() => {
    const BROWSER_TOKEN_STORAGE_KEY = "flacofitness:browser-token:v1";
    const motionQuery = window.matchMedia("(prefers-reduced-motion: reduce)");

    document.documentElement.classList.add("ff-motion-enabled");

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
        revealCurrentPage();
        updateCurrentYear();
        initializeClickableRows();
        initializeRevealBlocks();
        initializeScrollableRails();
        initializePasswordToggles();
        initializePaymentFormAssistant();
        initializePayrollBuilder();
        initializeUserFormAssistant();
        initializeSelectSearch();
        initializeBrowserTokenMirror();
        initializeFab();
        initializeGlobalSearch();
        initializeUxMemoryActions();
        initializeUxEmptyStateActions();
        initializePersistentFilters();
        initializeInternalChat();
    });

    window.addEventListener("pageshow", () => {
        document.body.classList.remove("ff-page-exiting");
        revealCurrentPage();
    });

    window.addEventListener("load", () => {
        revealCurrentPage();
    }, { once: true });

    function revealCurrentPage() {
        window.requestAnimationFrame(() => {
            document.body.classList.add("ff-page-ready");
        });
    }

    function navigateWithTransition(destination) {
        window.location.href = destination;
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

        // Keep pages instant: no observers over every card/list row.
        revealTargets.forEach((element) => element.classList.add("is-visible"));
    }

    function initializePasswordToggles() {
        document.querySelectorAll("[data-password-toggle]").forEach((button) => {
            if (button.dataset.passwordToggleReady === "true") {
                return;
            }

            const targetId = button.getAttribute("data-password-toggle");
            const input = targetId
                ? document.getElementById(targetId)
                : button.closest(".ff-password-field")?.querySelector("input[type='password'], input[type='text']");

            if (!input) {
                return;
            }

            button.addEventListener("click", () => {
                const isPassword = input.type === "password";
                input.type = isPassword ? "text" : "password";
                button.setAttribute("aria-label", isPassword ? "Ocultar contraseña" : "Mostrar contraseña");

                const icon = button.querySelector("i");
                if (icon) {
                    icon.classList.toggle("fa-eye", !isPassword);
                    icon.classList.toggle("fa-eye-slash", isPassword);
                }
            });

            button.dataset.passwordToggleReady = "true";
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

        const staffSelect = root.querySelector("[name='staffPerfil.id']");
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

            const originalOptions = Array.from(select.options).map((option) => ({
                value: option.value,
                text: option.textContent || "",
                disabled: option.disabled
            }));

            const renderOptions = (term) => {
                const normalizedTerm = String(term || "").trim().toLowerCase();
                const selectedValue = select.value;
                const placeholder = originalOptions[0];
                const matches = originalOptions.slice(1).filter((option) =>
                    !normalizedTerm || option.text.toLowerCase().includes(normalizedTerm)
                );

                select.innerHTML = "";

                if (placeholder) {
                    const placeholderOption = new Option(placeholder.text, placeholder.value, false, !selectedValue);
                    placeholderOption.disabled = placeholder.disabled;
                    select.add(placeholderOption);
                }

                if (matches.length === 0) {
                    select.add(new Option(input.dataset.noResults || "Sin coincidencias", ""));
                    select.value = "";
                    return;
                }

                matches.forEach((option) => {
                    const next = new Option(option.text, option.value, false, option.value === selectedValue);
                    next.disabled = option.disabled;
                    select.add(next);
                });

                if (matches.some((option) => option.value === selectedValue)) {
                    select.value = selectedValue;
                }
            };

            input.addEventListener("input", () => renderOptions(input.value));
            renderOptions(input.value);
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

    function initializeInternalChat() {
        const panel = document.querySelector("[data-internal-chat]");
        const launcher = document.querySelector("[data-chat-launcher]");
        if (!panel) {
            return;
        }

        const form = panel.querySelector("[data-chat-form]");
        const input = panel.querySelector("[data-chat-input]");
        const messages = panel.querySelector("[data-chat-messages]");
        const closeButton = panel.querySelector("[data-chat-close]");

        const addMessage = (text, type = "assistant", actions = []) => {
            if (!messages) {
                return;
            }

            const actionMarkup = actions.length
                ? `<div class="ff-chat-actions mt-2">${actions.map((action) => `
                    <a class="btn btn-sm btn-outline-success" href="${escapeHtml(action.url)}">${escapeHtml(action.label)}</a>
                `).join("")}</div>`
                : "";

            messages.insertAdjacentHTML("beforeend", `
                <div class="ff-chat-message ${type === "user" ? "is-user" : ""}">
                    <div>${escapeHtml(text)}</div>
                    ${actionMarkup}
                </div>
            `);
            messages.scrollTop = messages.scrollHeight;
        };

        if (launcher) {
            launcher.addEventListener("click", () => {
                panel.classList.toggle("is-open");
                if (panel.classList.contains("is-open")) {
                    input?.focus();
                }
            });
        }

        if (panel.dataset.chatPage === "true" || panel.hasAttribute("data-chat-page")) {
            panel.classList.add("is-open");
        }

        closeButton?.addEventListener("click", () => panel.classList.remove("is-open"));

        form?.addEventListener("submit", async (event) => {
            event.preventDefault();
            const question = input?.value.trim();
            if (!question) {
                return;
            }

            addMessage(question, "user");
            input.value = "";

            try {
                const response = await fetch("/api/chat/consulta", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ message: question })
                });
                const payload = await response.json();
                addMessage(payload.message || "No he encontrado una respuesta clara.", "assistant", payload.actions || []);
            } catch (_error) {
                addMessage("No he podido responder ahora. Prueba de nuevo en unos segundos.");
            }
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
