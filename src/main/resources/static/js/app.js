document.addEventListener("DOMContentLoaded", () => {
    const yearElements = document.querySelectorAll("[data-current-year]");

    yearElements.forEach((element) => {
        element.textContent = String(new Date().getFullYear());
    });

    initializeDashboardCharts();
    initTableSearch();
});

/* ── Inline table search ── */
function initTableSearch() {
    const searches = [
        { inputId: "usuariosSearch",    tableId: "usuariosTable" },
        { inputId: "pagosSearch",       tableId: "pagosTable" },
        { inputId: "asistenciasSearch", tableId: "asistenciasTable" },
        { inputId: "rutinasSearch",     tableId: "rutinasTable" }
    ];

    searches.forEach(({ inputId, tableId }) => {
        const input = document.getElementById(inputId);
        const table = document.getElementById(tableId);
        if (!input || !table) return;

        input.addEventListener("input", () => {
            const query = input.value.toLowerCase().trim();
            const rows  = table.querySelectorAll("tbody tr");
            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                row.style.display = query === "" || text.includes(query) ? "" : "none";
            });
        });
    });
}

async function initializeDashboardCharts() {
    const dashboard = document.querySelector("[data-dashboard]");

    if (!dashboard || typeof Chart === "undefined") {
        return;
    }

    const statsUrls = {
        usuarios: dashboard.dataset.statsUsuariosUrl,
        pagos: dashboard.dataset.statsPagosUrl,
        asistencias: dashboard.dataset.statsAsistenciasUrl,
        rutinas: dashboard.dataset.statsRutinasUrl
    };

    const results = await Promise.allSettled([
        fetchJson(statsUrls.usuarios),
        fetchJson(statsUrls.pagos),
        fetchJson(statsUrls.asistencias),
        fetchJson(statsUrls.rutinas)
    ]);

    const [usuariosResult, pagosResult, asistenciasResult, rutinasResult] = results;

    if (results.some((result) => result.status === "rejected")) {
        showDashboardAlert();
    }

    if (usuariosResult.status === "fulfilled") {
        updateDashboardStats({
            usuariosTotales: usuariosResult.value.totalUsuarios,
            usuariosActivos: usuariosResult.value.usuariosActivos
        });
        renderUsuariosChart(usuariosResult.value);
    } else {
        toggleChartEmptyState("usuarios", true);
    }

    if (pagosResult.status === "fulfilled") {
        updateDashboardStats({
            ingresosRegistrados: pagosResult.value.ingresosTotales,
            pagosPendientes: pagosResult.value.pagosPendientes
        });
        renderIngresosChart(pagosResult.value);
    } else {
        toggleChartEmptyState("ingresos", true);
    }

    if (asistenciasResult.status === "fulfilled") {
        const totalAsistencias = Array.isArray(asistenciasResult.value.asistenciasPorDia)
            ? asistenciasResult.value.asistenciasPorDia.reduce((sum, item) => sum + Number(item.total || 0), 0)
            : null;

        updateDashboardStats({
            asistenciasRegistradas: totalAsistencias
        });
        renderAsistenciasChart(asistenciasResult.value);
    } else {
        toggleChartEmptyState("asistencias", true);
    }

    if (rutinasResult.status === "fulfilled") {
        updateDashboardStats({
            rutinasActivas: rutinasResult.value.rutinasActivas
        });
    }
}

async function fetchJson(url) {
    const response = await fetch(url, {
        headers: {
            Accept: "application/json"
        }
    });

    if (!response.ok) {
        throw new Error(`Error al cargar ${url}: ${response.status}`);
    }

    return response.json();
}

function updateDashboardStats(stats) {
    Object.entries(stats).forEach(([key, value]) => {
        if (value === null || value === undefined) {
            return;
        }

        const target = document.querySelector(`[data-stat="${key}"]`);

        if (!target) {
            return;
        }

        if (target.dataset.format === "currency") {
            target.textContent = formatCurrency(value);
            return;
        }

        target.textContent = formatInteger(value);
    });
}

function renderUsuariosChart(usuariosStats) {
    const total = Number(usuariosStats.totalUsuarios || 0);
    const activos = Number(usuariosStats.usuariosActivos || 0);
    const inactivos = Math.max(total - activos, 0);

    if (total === 0) {
        toggleChartEmptyState("usuarios", true);
        return;
    }

    toggleChartEmptyState("usuarios", false);
    createChart("usuariosChart", "bar", {
        labels: ["Activos", "Inactivos"],
        datasets: [
            {
                label: "Usuarios",
                data: [activos, inactivos],
                backgroundColor: ["rgba(25, 122, 86, 0.88)", "rgba(102, 112, 133, 0.42)"],
                borderRadius: 12,
                borderSkipped: false,
                maxBarThickness: 54
            }
        ]
    }, {
        plugins: {
            legend: {
                display: false
            }
        },
        scales: {
            x: {
                grid: {
                    display: false
                }
            },
            y: {
                beginAtZero: true,
                ticks: {
                    precision: 0
                }
            }
        }
    });
}

function renderIngresosChart(pagosStats) {
    const ingresosMensuales = Array.isArray(pagosStats.ingresosMensuales) ? pagosStats.ingresosMensuales : [];

    if (ingresosMensuales.length === 0) {
        toggleChartEmptyState("ingresos", true);
        return;
    }

    toggleChartEmptyState("ingresos", false);
    createChart("ingresosChart", "line", {
        labels: ingresosMensuales.map((item) => formatPeriod(item.periodo)),
        datasets: [
            {
                label: "Ingresos",
                data: ingresosMensuales.map((item) => Number(item.total || 0)),
                borderColor: "rgba(25, 122, 86, 1)",
                backgroundColor: "rgba(25, 122, 86, 0.12)",
                fill: true,
                pointRadius: 4,
                pointHoverRadius: 5,
                tension: 0.35
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
                        return formatCurrency(context.parsed.y);
                    }
                }
            }
        },
        scales: {
            y: {
                beginAtZero: true,
                ticks: {
                    callback(value) {
                        return formatCurrency(value);
                    }
                }
            }
        }
    });
}

function renderAsistenciasChart(asistenciasStats) {
    const asistenciasPorDia = Array.isArray(asistenciasStats.asistenciasPorDia) ? asistenciasStats.asistenciasPorDia : [];

    if (asistenciasPorDia.length === 0) {
        toggleChartEmptyState("asistencias", true);
        return;
    }

    toggleChartEmptyState("asistencias", false);
    createChart("asistenciasChart", "line", {
        labels: asistenciasPorDia.map((item) => formatDate(item.fecha)),
        datasets: [
            {
                label: "Asistencias",
                data: asistenciasPorDia.map((item) => Number(item.total || 0)),
                borderColor: "rgba(48, 90, 135, 1)",
                backgroundColor: "rgba(48, 90, 135, 0.12)",
                fill: true,
                pointRadius: 3,
                pointHoverRadius: 4,
                tension: 0.28
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

function showDashboardAlert() {
    const alert = document.querySelector("[data-dashboard-alert]");

    if (alert) {
        alert.classList.remove("d-none");
    }
}

function formatInteger(value) {
    return new Intl.NumberFormat("es-ES", {
        maximumFractionDigits: 0
    }).format(Number(value || 0));
}

function formatCurrency(value) {
    return new Intl.NumberFormat("es-ES", {
        style: "currency",
        currency: "EUR",
        maximumFractionDigits: 2
    }).format(Number(value || 0));
}

function formatPeriod(period) {
    if (!period || !period.includes("-")) {
        return period || "";
    }

    const [year, month] = period.split("-");
    const date = new Date(Number(year), Number(month) - 1, 1);

    return new Intl.DateTimeFormat("es-ES", {
        month: "short",
        year: "numeric"
    }).format(date);
}

function formatDate(dateValue) {
    if (!dateValue) {
        return "";
    }

    return new Intl.DateTimeFormat("es-ES", {
        day: "2-digit",
        month: "short"
    }).format(new Date(`${dateValue}T00:00:00`));
}
