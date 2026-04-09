document.addEventListener("DOMContentLoaded", () => {
    initializeDashboard();
});

async function initializeDashboard() {
    const dashboard = document.querySelector("[data-dashboard]");

    if (!dashboard) {
        return;
    }

    if (typeof Chart === "undefined") {
        console.error("Chart.js no esta disponible en tiempo de ejecucion.");
        toggleDashboardAlert(true, "No se pudieron cargar los graficos del dashboard.");
        return;
    }

    const rangeSelect = dashboard.querySelector("[data-dashboard-range]");
    const refreshButton = dashboard.querySelector("[data-dashboard-refresh]");
    const defaultRange = Number(dashboard.dataset.defaultRange || 30);

    const loadDashboard = async (rangeValue) => {
        const range = normalizeDashboardRange(rangeValue || defaultRange);
        toggleDashboardLoading(true);

        try {
            const stats = await fetchDashboardStats(dashboard.dataset.statsDashboardUrl, range);
            updateDashboardStats(stats);
            updateDashboardRangeLabel(stats.rangoDias || range);
            window.requestAnimationFrame(() => {
                renderPlanChart(stats.usuariosPorPlan || []);
                renderIngresosChart(stats.ingresosMensualesSerie || []);
                renderAsistenciasChart(stats.asistenciasRecientes || []);
                renderAltasChart(stats.altasRecientes || []);
            });
            toggleDashboardAlert(false);
            updateDashboardTimestamp();
        } catch (error) {
            console.error(error);
            toggleDashboardAlert(true, "No se pudieron actualizar todas las metricas del dashboard.");
            ["planes", "ingresos", "asistencias", "altas"].forEach((chartKey) => toggleChartEmptyState(chartKey, true));
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

    await loadDashboard(defaultRange);
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

function updateDashboardStats(stats) {
    animateDashboardStat("totalUsuarios", stats.totalUsuarios, "integer");
    animateDashboardStat("usuariosActivos", stats.usuariosActivos, "integer");
    animateDashboardStat("planesActivos", stats.planesActivos, "integer");
    animateDashboardStat("pagosPendientes", stats.pagosPendientes, "integer");
    animateDashboardStat("pagosVencidos", stats.pagosVencidos, "integer");
    animateDashboardStat("renovacionesProximas", stats.renovacionesProximas, "integer");
    animateDashboardStat("ingresosMensuales", stats.ingresosMensuales, "currency");
    animateDashboardStat("ingresosTotales", stats.ingresosTotales, "currency");
    animateDashboardStat("asistenciasHoy", stats.asistenciasHoy, "integer");
    animateDashboardStat("rutinasActivas", stats.rutinasActivas, "integer");
}

function animateDashboardStat(key, rawValue, kind) {
    document.querySelectorAll(`[data-stat="${key}"]`).forEach((target) => {
        const nextValue = Number(rawValue || 0);
        const currentValue = Number(target.dataset.statRaw || 0);

        target.dataset.statRaw = String(nextValue);

        if (currentValue === nextValue) {
            target.textContent = formatDashboardValue(nextValue, kind);
            return;
        }

        const startTime = performance.now();
        const duration = 550;

        function step(timestamp) {
            const progress = Math.min((timestamp - startTime) / duration, 1);
            const eased = 1 - Math.pow(1 - progress, 3);
            const animatedValue = currentValue + ((nextValue - currentValue) * eased);
            target.textContent = formatDashboardValue(animatedValue, kind, progress < 1);

            if (progress < 1) {
                window.requestAnimationFrame(step);
            } else {
                target.textContent = formatDashboardValue(nextValue, kind);
            }
        }

        window.requestAnimationFrame(step);
    });
}

function formatDashboardValue(value, kind, isAnimating = false) {
    const utils = window.ffUtils;
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

function renderIngresosChart(ingresosMensuales) {
    if (!Array.isArray(ingresosMensuales) || ingresosMensuales.length === 0) {
        toggleChartEmptyState("ingresos", true);
        return;
    }

    toggleChartEmptyState("ingresos", false);
    createChart("ingresosChart", "line", {
        labels: ingresosMensuales.map((item) => window.ffUtils.formatPeriod(item.periodo)),
        datasets: [{
            label: "Ingresos",
            data: ingresosMensuales.map((item) => Number(item.total || 0)),
            borderColor: "rgba(22, 163, 74, 1)",
            backgroundColor: "rgba(22, 163, 74, 0.14)",
            fill: true,
            tension: 0.35,
            pointRadius: 3,
            pointHoverRadius: 5
        }]
    }, {
        plugins: {
            legend: { display: false },
            tooltip: {
                callbacks: {
                    label(context) {
                        return window.ffUtils.formatCurrency(context.parsed.y);
                    }
                }
            }
        },
        scales: {
            y: {
                beginAtZero: true,
                ticks: {
                    callback(value) {
                        return window.ffUtils.formatCurrency(value);
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
    createChart("asistenciasChart", "bar", {
        labels: asistenciasRecientes.map((item) => window.ffUtils.formatShortDate(item.fecha)),
        datasets: [{
            label: "Asistencias",
            data: asistenciasRecientes.map((item) => Number(item.total || 0)),
            backgroundColor: "rgba(37, 99, 235, 0.8)",
            borderRadius: 10,
            borderSkipped: false,
            maxBarThickness: 32
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

    toggleChartEmptyState("altas", false);
    createChart("altasChart", "line", {
        labels: altasRecientes.map((item) => window.ffUtils.formatPeriod(item.periodo)),
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

    if (!canvas) {
        return;
    }

    const existingChart = Chart.getChart(canvas);
    if (existingChart) {
        existingChart.destroy();
    }

    new Chart(canvas, {
        type,
        data,
        options: {
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
            },
            scales: {
                x: {
                    ticks: { color: "#475569" },
                    grid: { display: false }
                },
                y: {
                    ticks: { color: "#475569" },
                    grid: { color: "rgba(148, 163, 184, 0.18)" }
                }
            },
            ...options
        }
    });
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
