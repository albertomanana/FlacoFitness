document.addEventListener("DOMContentLoaded", () => {
    initializeDashboard();
});

function initializeDashboard() {
    const dashboard = document.querySelector("[data-dashboard]");

    if (!dashboard) {
        return;
    }

    const utils = window.ffUtils || createDashboardUtils();
    const rangeSelect = dashboard.querySelector("[data-dashboard-range]");
    const refreshButton = dashboard.querySelector("[data-dashboard-refresh]");
    const defaultRange = normalizeDashboardRange(Number(dashboard.dataset.defaultRange || 30));
    const initialStats = readInitialDashboardStats();
    let latestStats = initialStats;

    if (initialStats) {
        hydrateDashboard(initialStats, utils, { animateStats: false });
    }

    if (typeof Chart === "undefined") {
        console.error("Chart.js no esta disponible en tiempo de ejecucion.");
        toggleDashboardAlert(true, "No se pudieron cargar los graficos del dashboard.");
        return;
    }

    const loadDashboard = async (rangeValue) => {
        const range = normalizeDashboardRange(rangeValue || defaultRange);
        toggleDashboardLoading(true);

        try {
            const stats = await fetchDashboardStats(dashboard.dataset.statsDashboardUrl, range);
            latestStats = stats;
            hydrateDashboard(stats, utils, { animateStats: true });
            toggleDashboardAlert(false);
            updateDashboardTimestamp();
        } catch (error) {
            console.error(error);
            toggleDashboardAlert(true, "No se pudieron actualizar todas las metricas del dashboard.");
        } finally {
            toggleDashboardLoading(false);
        }
    };

    if (rangeSelect) {
        rangeSelect.value = String(defaultRange);
        rangeSelect.addEventListener("change", (event) => {
            loadDashboard(Number(event.target.value));
        });
    }

    if (refreshButton) {
        refreshButton.addEventListener("click", () => {
            loadDashboard(rangeSelect ? Number(rangeSelect.value) : defaultRange);
        });
    }

    window.addEventListener("ff:themechange", () => {
        if (!latestStats) {
            return;
        }
        hydrateDashboard(latestStats, utils, { animateStats: false });
    });

    window.setTimeout(() => {
        loadDashboard(defaultRange);
    }, 150);
}

function hydrateDashboard(stats, utils, options = {}) {
    updateDashboardStats(stats, utils, options);
    updateDashboardRangeLabel(stats.rangoDias || 30);
    renderPlanChart(stats.usuariosPorPlan || []);
    renderIngresosGastosChart(stats.ingresosMensualesSerie || [], stats.gastosMensualesSerie || [], utils);
    renderAsistenciasChart(stats.asistenciasRecientes || []);
}

function readInitialDashboardStats() {
    const script = document.getElementById("dashboardInitialStats");

    if (!script || !script.textContent.trim()) {
        return null;
    }

    try {
        return JSON.parse(script.textContent);
    } catch (error) {
        console.error("No se pudo leer el estado inicial del dashboard.", error, script.textContent);
        return null;
    }
}

async function fetchDashboardStats(baseUrl, range) {
    const url = new URL(baseUrl, window.location.origin);
    url.searchParams.set("rangoDias", String(normalizeDashboardRange(range)));

    const response = await fetch(url, {
        headers: {
            Accept: "application/json"
        }
    });

    if (!response.ok) {
        throw new Error(`No se pudieron cargar las estadisticas del dashboard: ${response.status}`);
    }

    return response.json();
}

function normalizeDashboardRange(range) {
    const parsed = Number(range || 30);

    if (Number.isNaN(parsed)) {
        return 30;
    }

    return Math.max(7, Math.min(parsed, 365));
}

function updateDashboardStats(stats, utils, options = {}) {
    const animate = options.animateStats !== false;

    animateDashboardStat("totalUsuarios", stats.totalUsuarios, "integer", utils, animate);
    animateDashboardStat("usuariosActivos", stats.usuariosActivos, "integer", utils, animate);
    animateDashboardStat("planesActivos", stats.planesActivos, "integer", utils, animate);
    animateDashboardStat("pagosPendientes", stats.pagosPendientes, "integer", utils, animate);
    animateDashboardStat("pagosVencidos", stats.pagosVencidos, "integer", utils, animate);
    animateDashboardStat("renovacionesProximas", stats.renovacionesProximas, "integer", utils, animate);
    animateDashboardStat("ingresosMensuales", stats.ingresosMensuales, "currency", utils, animate);
    animateDashboardStat("gastoMesActual", stats.gastoMesActual, "currency", utils, animate);
    animateDashboardStat("beneficioEstimado", stats.beneficioEstimado, "currency", utils, animate);
    animateDashboardStat("ingresosTotales", stats.ingresosTotales, "currency", utils, animate);
    animateDashboardStat("asistenciasHoy", stats.asistenciasHoy, "integer", utils, animate);
    animateDashboardStat("rutinasActivas", stats.rutinasActivas, "integer", utils, animate);
    animateDashboardStat("staffActivos", stats.staffActivos, "integer", utils, animate);
    animateDashboardStat("trialsPendientes", stats.trialsPendientes, "integer", utils, animate);
    animateDashboardStat("trialsHoy", stats.trialsHoy, "integer", utils, animate);
    animateDashboardStat("trialsSemana", stats.trialsSemana, "integer", utils, animate);
    animateDashboardStat("sesionesHoy", stats.sesionesHoy, "integer", utils, animate);
    animateDashboardStat("membresiasActivas", stats.membresiasActivas, "integer", utils, animate);
    animateDashboardStat("membresiasVencidas", stats.membresiasVencidas, "integer", utils, animate);
    animateDashboardStat("maquinasFueraServicio", stats.maquinasFueraServicio, "integer", utils, animate);
    animateDashboardStat("materialesBajoStock", stats.materialesBajoStock, "integer", utils, animate);
    animateDashboardStat("maquinasRevisionProxima", stats.maquinasRevisionProxima, "integer", utils, animate);
}

function animateDashboardStat(key, rawValue, kind, utils, animate = true) {
    document.querySelectorAll(`[data-stat="${key}"]`).forEach((target) => {
        const nextValue = Number(rawValue || 0);
        const currentValue = Number(target.dataset.statRaw || 0);

        cancelDashboardStatAnimation(target);
        target.dataset.statRaw = String(nextValue);

        if (!animate || currentValue === nextValue || shouldReduceDashboardMotion()) {
            target.textContent = formatDashboardValue(nextValue, kind, utils);
            return;
        }

        const startTime = performance.now();
        const duration = 980;

        function step(timestamp) {
            const progress = Math.min((timestamp - startTime) / duration, 1);
            const eased = easeOutQuart(progress);
            const animatedValue = currentValue + ((nextValue - currentValue) * eased);
            target.textContent = formatDashboardValue(animatedValue, kind, utils, progress < 1);

            if (progress < 1) {
                target.dataset.statAnimationFrame = String(window.requestAnimationFrame(step));
            } else {
                target.textContent = formatDashboardValue(nextValue, kind, utils);
                delete target.dataset.statAnimationFrame;
            }
        }

        target.dataset.statAnimationFrame = String(window.requestAnimationFrame(step));
    });
}

function cancelDashboardStatAnimation(target) {
    const frameId = Number(target.dataset.statAnimationFrame);

    if (frameId) {
        window.cancelAnimationFrame(frameId);
        delete target.dataset.statAnimationFrame;
    }
}

function easeOutQuart(progress) {
    return 1 - Math.pow(1 - progress, 4);
}

function shouldReduceDashboardMotion() {
    return window.matchMedia("(prefers-reduced-motion: reduce)").matches;
}

function formatDashboardValue(value, kind, utils, isAnimating = false) {
    const numericValue = Number(value || 0);

    if (kind === "currency") {
        return utils.formatCurrency(isAnimating ? numericValue.toFixed(2) : numericValue);
    }

    return utils.formatInteger(Math.round(numericValue));
}

function updateDashboardRangeLabel(range) {
    document.querySelectorAll("[data-dashboard-range-label]").forEach((target) => {
        target.textContent = String(range);
    });
}

function updateDashboardTimestamp() {
    const now = new Date();
    const time = new Intl.DateTimeFormat("es-ES", {
        hour: "2-digit",
        minute: "2-digit"
    }).format(now);

    document.querySelectorAll("[data-dashboard-updated-at]").forEach((target) => {
        target.textContent = `Ultima actualizacion: ${time}`;
    });
}

function toggleDashboardLoading(loading) {
    const refreshButton = document.querySelector("[data-dashboard-refresh]");

    if (!refreshButton) {
        return;
    }

    refreshButton.disabled = loading;
    refreshButton.classList.toggle("is-loading", loading);
}

function renderPlanChart(planDistribution) {
    if (!Array.isArray(planDistribution) || planDistribution.length === 0) {
        toggleChartEmptyState("planes", true);
        return;
    }

    toggleChartEmptyState("planes", false);
    createChart("planesChart", "doughnut", {
        labels: planDistribution.map((item) => item.plan),
        datasets: [{
            data: planDistribution.map((item) => Number(item.totalUsuarios || 0)),
            backgroundColor: [
                "rgba(22, 163, 74, 0.88)",
                "rgba(59, 130, 246, 0.88)",
                "rgba(249, 115, 22, 0.88)",
                "rgba(148, 163, 184, 0.88)"
            ],
            borderWidth: 0
        }]
    }, {
        plugins: {
            legend: {
                position: "bottom"
            }
        }
    });
}

function renderIngresosGastosChart(ingresosMensuales, gastosMensuales, utils) {
    const ingresos = Array.isArray(ingresosMensuales) ? ingresosMensuales : [];
    const gastos = Array.isArray(gastosMensuales) ? gastosMensuales : [];

    if (ingresos.length === 0 && gastos.length === 0) {
        toggleChartEmptyState("ingresosGastos", true);
        return;
    }

    const ingresosByPeriod = new Map(ingresos.map((item) => [item.periodo, Number(item.total || 0)]));
    const gastosByPeriod = new Map(gastos.map((item) => [item.periodo, Number(item.total || 0)]));
    const orderedPeriods = buildContinuousPeriods(
        Array.from(new Set([...ingresosByPeriod.keys(), ...gastosByPeriod.keys()])).sort()
    );

    toggleChartEmptyState("ingresosGastos", false);
    createChart("ingresosGastosChart", "bar", {
        labels: orderedPeriods.map((period) => utils.formatPeriod(period)),
        datasets: [
            {
                label: "Ingresos",
                data: orderedPeriods.map((period) => ingresosByPeriod.get(period) || 0),
                backgroundColor: "rgba(22, 163, 74, 0.82)",
                hoverBackgroundColor: "rgba(21, 128, 61, 0.92)",
                borderRadius: 12,
                borderSkipped: false,
                maxBarThickness: 34
            },
            {
                label: "Gastos",
                data: orderedPeriods.map((period) => gastosByPeriod.get(period) || 0),
                backgroundColor: "rgba(239, 68, 68, 0.72)",
                hoverBackgroundColor: "rgba(220, 38, 38, 0.84)",
                borderRadius: 12,
                borderSkipped: false,
                maxBarThickness: 34
            }
        ]
    }, {
        plugins: {
            legend: { display: true, position: "bottom" },
            tooltip: {
                callbacks: {
                    label(context) {
                        return utils.formatCurrency(context.parsed.y);
                    }
                }
            }
        },
        scales: {
            y: {
                beginAtZero: true,
                ticks: {
                    callback(value) {
                        return utils.formatCurrency(value);
                    }
                }
            }
        }
    });
}

function buildContinuousPeriods(periods) {
    if (!Array.isArray(periods) || periods.length === 0) {
        return [];
    }

    const sorted = periods
        .map(parsePeriod)
        .filter((item) => item !== null)
        .sort((a, b) => (a.year - b.year) || (a.month - b.month));

    if (sorted.length === 0) {
        return periods;
    }

    const first = sorted[0];
    const last = sorted[sorted.length - 1];
    const result = [];

    let year = first.year;
    let month = first.month;

    while (year < last.year || (year === last.year && month <= last.month)) {
        result.push(`${year}-${String(month).padStart(2, "0")}`);
        month += 1;
        if (month > 12) {
            month = 1;
            year += 1;
        }
    }

    return result;
}

function parsePeriod(period) {
    if (!period || !period.includes("-")) {
        return null;
    }

    const [yearRaw, monthRaw] = period.split("-");
    const year = Number(yearRaw);
    const month = Number(monthRaw);

    if (!Number.isInteger(year) || !Number.isInteger(month) || month < 1 || month > 12) {
        return null;
    }

    return { year, month };
}

function renderAsistenciasChart(asistenciasRecientes) {
    if (!Array.isArray(asistenciasRecientes) || asistenciasRecientes.length === 0) {
        toggleChartEmptyState("asistencias", true);
        return;
    }

    toggleChartEmptyState("asistencias", false);
    createChart("asistenciasChart", "line", {
        labels: asistenciasRecientes.map((item) => createDashboardUtils().formatShortDate(item.fecha)),
        datasets: [{
            label: "Asistencias",
            data: asistenciasRecientes.map((item) => Number(item.total || 0)),
            borderColor: "rgba(37, 99, 235, 0.92)",
            backgroundColor: "rgba(37, 99, 235, 0.16)",
            fill: true,
            tension: 0.35,
            pointRadius: 3,
            pointHoverRadius: 5
        }]
    }, {
        plugins: {
            legend: { display: false }
        },
        scales: {
            y: {
                beginAtZero: true,
                ticks: {
                    precision: 0
                }
            }
        }
    });
}

function createChart(canvasId, type, data, options) {
    const canvas = document.getElementById(canvasId);

    if (!canvas || typeof Chart === "undefined") {
        return;
    }

    const existingChart = Chart.getChart(canvas);
    if (existingChart) {
        existingChart.destroy();
    }

    const radialChart = isRadialChart(type);
    const theme = getChartTheme();
    const baseOptions = {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
            mode: "index",
            intersect: false
        },
        animation: {
            duration: 650,
            easing: "easeOutQuart"
        },
        plugins: {
            legend: {
                labels: {
                    usePointStyle: true,
                    boxWidth: 8,
                    color: theme.textSecondary
                }
            },
            tooltip: {
                backgroundColor: theme.tooltipBg,
                titleColor: theme.tooltipText,
                bodyColor: theme.tooltipText,
                padding: 12,
                displayColors: false
            }
        }
    };

    if (!radialChart) {
        baseOptions.scales = {
            x: {
                ticks: { color: theme.textSecondary },
                grid: { display: false }
            },
            y: {
                ticks: { color: theme.textSecondary },
                grid: { color: theme.grid }
            }
        };
    }

    new Chart(canvas, {
        type,
        data,
        options: mergeChartOptions(baseOptions, options, radialChart)
    });
}

function isRadialChart(type) {
    return type === "doughnut" || type === "pie" || type === "polarArea";
}

function mergeChartOptions(baseOptions, customOptions, radialChart) {
    const mergedOptions = {
        ...baseOptions,
        ...customOptions,
        plugins: {
            ...(baseOptions.plugins || {}),
            ...(customOptions?.plugins || {})
        }
    };

    if (!radialChart) {
        mergedOptions.scales = {
            ...(baseOptions.scales || {}),
            ...(customOptions?.scales || {})
        };
    } else {
        delete mergedOptions.scales;
    }

    return mergedOptions;
}

function toggleChartEmptyState(chartKey, isEmpty) {
    const emptyState = document.querySelector(`[data-chart-empty="${chartKey}"]`);
    const canvas = document.getElementById(`${chartKey}Chart`);

    if (emptyState) {
        emptyState.classList.toggle("d-none", !isEmpty);
    }

    if (canvas) {
        canvas.classList.toggle("d-none", isEmpty);
    }
}

function toggleDashboardAlert(visible, message) {
    const alert = document.querySelector("[data-dashboard-alert]");

    if (alert) {
        alert.classList.toggle("d-none", !visible);
        if (visible && message) {
            const body = alert.querySelector("[data-dashboard-alert-body]");
            if (body) {
                body.textContent = message;
            }
        }
    }
}

function getChartTheme() {
    const styles = window.getComputedStyle(document.documentElement);

    return {
        textSecondary: (styles.getPropertyValue("--ff-text-muted") || "#475569").trim(),
        grid: (styles.getPropertyValue("--ff-chart-grid") || "rgba(148, 163, 184, 0.18)").trim(),
        tooltipBg: (styles.getPropertyValue("--ff-chart-tooltip-bg") || "#1d2939").trim(),
        tooltipText: (styles.getPropertyValue("--ff-chart-tooltip-text") || "#ffffff").trim()
    };
}

function createDashboardUtils() {
    return {
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
        }
    };
}
