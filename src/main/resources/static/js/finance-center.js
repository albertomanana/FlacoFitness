(() => {
document.addEventListener("DOMContentLoaded", () => {
    const root = document.querySelector("[data-finance-center]");
    if (!root || typeof Chart === "undefined") {
        return;
    }

    const stats = readFinancialStats();
    if (!stats) {
        return;
    }

    renderCashflowChart(root, stats);
    renderCategoryChart(root, "expenses", stats.gastosPorCategoria || []);
    renderCategoryChart(root, "payments", stats.pagosPorEstado || []);
    renderCategoryChart(root, "payrolls", stats.nominasPorEstado || []);
});

function readFinancialStats() {
    const script = document.getElementById("financialCenterStats");
    if (!script) {
        return null;
    }

    try {
        return JSON.parse(script.textContent || "{}");
    } catch (_error) {
        return null;
    }
}

function renderCashflowChart(root, stats) {
    const canvas = root.querySelector('[data-finance-chart="cashflow"]');
    if (!canvas) {
        return;
    }

    const ingresos = stats.ingresosMensuales || [];
    const gastos = stats.gastosMensuales || [];
    const labels = Array.from(new Set([
        ...ingresos.map((item) => item.periodo),
        ...gastos.map((item) => item.periodo),
    ])).sort().slice(-8);

    const valuesFor = (items) => labels.map((label) => {
        const match = items.find((item) => item.periodo === label);
        return Number(match?.total || 0);
    });

    createChart(canvas, {
        type: "line",
        data: {
            labels,
            datasets: [
                {
                    label: "Ingresos",
                    data: valuesFor(ingresos),
                    borderColor: "#22c55e",
                    backgroundColor: "rgba(34, 197, 94, .16)",
                    tension: .38,
                    fill: true,
                },
                {
                    label: "Gastos",
                    data: valuesFor(gastos),
                    borderColor: "#f59e0b",
                    backgroundColor: "rgba(245, 158, 11, .14)",
                    tension: .38,
                    fill: true,
                },
            ],
        },
    });
}

function renderCategoryChart(root, key, items) {
    const canvas = root.querySelector(`[data-finance-chart="${key}"]`);
    if (!canvas) {
        return;
    }

    const filtered = items.filter((item) => Number(item.total || 0) > 0);
    const labels = filtered.length ? filtered.map((item) => normalizeLabel(item.categoria)) : ["Sin datos"];
    const values = filtered.length ? filtered.map((item) => Number(item.total || 0)) : [1];

    createChart(canvas, {
        type: "doughnut",
        data: {
            labels,
            datasets: [{
                data: values,
                backgroundColor: [
                    "rgba(56, 189, 248, .82)",
                    "rgba(34, 197, 94, .82)",
                    "rgba(245, 158, 11, .82)",
                    "rgba(239, 68, 68, .82)",
                    "rgba(148, 163, 184, .72)",
                ],
                borderColor: "rgba(255, 255, 255, .14)",
                borderWidth: 1,
            }],
        },
        options: {
            cutout: "68%",
        },
    });
}

function createChart(canvas, config) {
    const gridColor = "rgba(15, 23, 42, .12)";
    const textColor = "rgba(15, 23, 42, .72)";

    return new Chart(canvas, {
        ...config,
        options: {
            responsive: true,
            maintainAspectRatio: true,
            animation: window.matchMedia("(prefers-reduced-motion: reduce)").matches ? false : {
                duration: 700,
                easing: "easeOutQuart",
            },
            plugins: {
                legend: {
                    position: "bottom",
                    labels: {
                        color: textColor,
                        boxWidth: 10,
                        usePointStyle: true,
                    },
                },
            },
            scales: config.type === "line" ? {
                x: { ticks: { color: textColor }, grid: { color: gridColor } },
                y: { ticks: { color: textColor }, grid: { color: gridColor }, beginAtZero: true },
            } : undefined,
            ...(config.options || {}),
        },
    });
}

function normalizeLabel(value) {
    return String(value || "")
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/(^|\s)\S/g, (letter) => letter.toUpperCase());
}
})();
