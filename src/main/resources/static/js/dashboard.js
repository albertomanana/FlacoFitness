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

    if (initialStats) {
        hydrateDashboard(initialStats, utils);
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
            hydrateDashboard(stats, utils);
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

    window.setTimeout(() => {
        loadDashboard(defaultRange);
    }, 150);
}

function hydrateDashboard(stats, utils) {
    updateDashboardStats(stats, utils);
    updateDashboardRangeLabel(stats.rangoDias || 30);
    renderPlanChart(stats.usuariosPorPlan || []);
    renderIngresosChart(stats.ingresosMensualesSerie || [], utils);
    renderAsistenciasChart(stats.asistenciasRecientes || []);
    renderAltasChart(stats.altasRecientes || []);
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

function updateDashboardStats(stats, utils) {
    animateDashboardStat("totalUsuarios", stats.totalUsuarios, "integer", utils);
    animateDashboardStat("usuariosActivos", stats.usuariosActivos, "integer", utils);
    animateDashboardStat("planesActivos", stats.planesActivos, "integer", utils);
    animateDashboardStat("pagosPendientes", stats.pagosPendientes, "integer", utils);
    animateDashboardStat("pagosVencidos", stats.pagosVencidos, "integer", utils);
    animateDashboardStat("renovacionesProximas", stats.renovacionesProximas, "integer", utils);
    animateDashboardStat("ingresosMensuales", stats.ingresosMensuales, "currency", utils);
    animateDashboardStat("ingresosTotales", stats.ingresosTotales, "currency", utils);
    animateDashboardStat("asistenciasHoy", stats.asistenciasHoy, "integer", utils);
    animateDashboardStat("rutinasActivas", stats.rutinasActivas, "integer", utils);
    animateDashboardStat("staffActivos", stats.staffActivos, "integer", utils);
    animateDashboardStat("trialsPendientes", stats.trialsPendientes, "integer", utils);
    animateDashboardStat("trialsHoy", stats.trialsHoy, "integer", utils);
    animateDashboardStat("sesionesHoy", stats.sesionesHoy, "integer", utils);
    animateDashboardStat("membresiasActivas", stats.membresiasActivas, "integer", utils);
    animateDashboardStat("membresiasVencidas", stats.membresiasVencidas, "integer", utils);
}

function animateDashboardStat(key, rawValue, kind, utils) {
    document.querySelectorAll(`[data-stat="${key}"]`).forEach((target) => {
        const nextValue = Number(rawValue || 0);
        const currentValue = Number(target.dataset.statRaw || 0);

        target.dataset.statRaw = String(nextValue);

        if (currentValue === nextValue) {
            target.textContent = formatDashboardValue(nextValue, kind, utils);
            return;
        }

        const startTime = performance.now();
        const duration = 450;

        function step(timestamp) {
            const progress = Math.min((timestamp - startTime) / duration, 1);
            const eased = 1 - Math.pow(1 - progress, 3);
            const animatedValue = currentValue + ((nextValue - currentValue) * eased);
            target.textContent = formatDashboardValue(animatedValue, kind, utils, progress < 1);

            if (progress < 1) {
                window.requestAnimationFrame(step);
            } else {
                target.textContent = formatDashboardValue(nextValue, kind, utils);
            }
        }

        window.requestAnimationFrame(step);
    });
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

function renderIngresosChart(ingresosMensuales, utils) {
    if (!Array.isArray(ingresosMensuales) || ingresosMensuales.length === 0) {
        toggleChartEmptyState("ingresos", true);
        return;
    }

    toggleChartEmptyState("ingresos", false);
    createChart("ingresosChart", "bar", {
        labels: ingresosMensuales.map((item) => utils.formatPeriod(item.periodo)),
        datasets: [{
            label: "Ingresos",
            data: ingresosMensuales.map((item) => Number(item.total || 0)),
            backgroundColor: "rgba(22, 163, 74, 0.82)",
            hoverBackgroundColor: "rgba(21, 128, 61, 0.92)",
            borderRadius: 14,
            borderSkipped: false,
            maxBarThickness: 42
        }]
    }, {
        plugins: {
            legend: { display: false },
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

function renderAltasChart(altasRecientes) {
    if (!Array.isArray(altasRecientes) || altasRecientes.length === 0) {
        toggleChartEmptyState("altas", true);
        return;
    }

    const utils = createDashboardUtils();

    toggleChartEmptyState("altas", false);
    createChart("altasChart", "line", {
        labels: altasRecientes.map((item) => utils.formatPeriod(item.periodo)),
        datasets: [{
            label: "Altas",
            data: altasRecientes.map((item) => Number(item.total || 0)),
            borderColor: "rgba(249, 115, 22, 1)",
            backgroundColor: "rgba(249, 115, 22, 0.14)",
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
                    color: "#475569"
                }
            },
            tooltip: {
                backgroundColor: "#1d2939",
                titleColor: "#ffffff",
                bodyColor: "#ffffff",
                padding: 12,
                displayColors: false
            }
        }
    };

    if (!radialChart) {
        baseOptions.scales = {
            x: {
                ticks: { color: "#475569" },
                grid: { display: false }
            },
            y: {
                ticks: { color: "#475569" },
                grid: { color: "rgba(148, 163, 184, 0.18)" }
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
