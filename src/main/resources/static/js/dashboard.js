document.addEventListener("DOMContentLoaded", () => {
    initializeDashboard();
});

async function initializeDashboard() {
    const dashboard = document.querySelector("[data-dashboard]");

    if (!dashboard || typeof Chart === "undefined") {
        return;
    }

    try {
        const response = await fetch(dashboard.dataset.statsDashboardUrl, {
            headers: {
                Accept: "application/json"
            }
        });

        if (!response.ok) {
            throw new Error(`No se pudieron cargar las estadisticas del dashboard: ${response.status}`);
        }

        const stats = await response.json();
        updateDashboardStats(stats);
        renderPlanChart(stats.usuariosPorPlan || []);
        renderIngresosChart(stats.ingresosMensualesSerie || []);
        renderAsistenciasChart(stats.asistenciasRecientes || []);
        renderAltasChart(stats.altasRecientes || []);
    } catch (error) {
        console.error(error);
        toggleDashboardAlert(true);
        ["planes", "ingresos", "asistencias", "altas"].forEach((chartKey) => toggleChartEmptyState(chartKey, true));
    }
}

function updateDashboardStats(stats) {
    const utils = window.ffUtils;

    const statValues = {
        totalUsuarios: utils.formatInteger(stats.totalUsuarios),
        usuariosActivos: utils.formatInteger(stats.usuariosActivos),
        planesActivos: utils.formatInteger(stats.planesActivos),
        pagosPendientes: utils.formatInteger(stats.pagosPendientes),
        ingresosMensuales: utils.formatCurrency(stats.ingresosMensuales),
        asistenciasHoy: utils.formatInteger(stats.asistenciasHoy),
        rutinasActivas: utils.formatInteger(stats.rutinasActivas)
    };

    Object.entries(statValues).forEach(([key, value]) => {
        document.querySelectorAll(`[data-stat="${key}"]`).forEach((target) => {
            target.textContent = value;
        });
    });
}

function renderPlanChart(planDistribution) {
    if (!Array.isArray(planDistribution) || planDistribution.length === 0) {
        toggleChartEmptyState("planes", true);
        return;
    }

    toggleChartEmptyState("planes", false);
    createChart("planesChart", "doughnut", {
        labels: planDistribution.map((item) => item.plan),
        datasets: [
            {
                data: planDistribution.map((item) => Number(item.totalUsuarios || 0)),
                backgroundColor: [
                    "rgba(22, 163, 74, 0.88)",
                    "rgba(59, 130, 246, 0.88)",
                    "rgba(249, 115, 22, 0.88)",
                    "rgba(148, 163, 184, 0.88)"
                ],
                borderWidth: 0
            }
        ]
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
        datasets: [
            {
                label: "Ingresos",
                data: ingresosMensuales.map((item) => Number(item.total || 0)),
                borderColor: "rgba(22, 163, 74, 1)",
                backgroundColor: "rgba(22, 163, 74, 0.14)",
                fill: true,
                tension: 0.35,
                pointRadius: 3,
                pointHoverRadius: 5
            }
        ]
    }, {
        plugins: {
            legend: {
                display: false
            },
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
        datasets: [
            {
                label: "Asistencias",
                data: asistenciasRecientes.map((item) => Number(item.total || 0)),
                backgroundColor: "rgba(37, 99, 235, 0.8)",
                borderRadius: 10,
                borderSkipped: false,
                maxBarThickness: 32
            }
        ]
    }, {
        plugins: {
            legend: {
                display: false
            }
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
        datasets: [
            {
                label: "Altas",
                data: altasRecientes.map((item) => Number(item.total || 0)),
                borderColor: "rgba(249, 115, 22, 1)",
                backgroundColor: "rgba(249, 115, 22, 0.14)",
                fill: true,
                tension: 0.35,
                pointRadius: 3,
                pointHoverRadius: 5
            }
        ]
    }, {
        plugins: {
            legend: {
                display: false
            }
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
                    ticks: {
                        color: "#475569"
                    },
                    grid: {
                        display: false
                    }
                },
                y: {
                    ticks: {
                        color: "#475569"
                    },
                    grid: {
                        color: "rgba(148, 163, 184, 0.18)"
                    }
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

function toggleDashboardAlert(visible) {
    const alert = document.querySelector("[data-dashboard-alert]");

    if (alert) {
        alert.classList.toggle("d-none", !visible);
    }
}
