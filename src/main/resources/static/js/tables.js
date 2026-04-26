/* ─────────────────────────────────────────────────────────────
   FlacoFitness — DataTables Init (v3)
   ───────────────────────────────────────────────────────────── */

document.addEventListener("DOMContentLoaded", () => {
    initializeDataTables();
    initializeTableFilters();
});

function initializeDataTables() {
    if (typeof DataTable === "undefined") {
        return;
    }

    ensureDataTableFilterHook();

    const tables = document.querySelectorAll("[data-ff-datatable]");
    window.ffTables = [];

    tables.forEach((table) => {
        if (table.__ffDataTable || (typeof DataTable.isDataTable === "function" && DataTable.isDataTable(table))) {
            return;
        }

        /* ── Guard: do NOT init DataTable on empty-state tables ──
           When Thymeleaf renders the empty tbody with a single
           td[colspan], DataTables interprets it as a data row
           with fewer columns than the header, causing:
           "Requested unknown parameter '1' for row 0, column 1"
        */
        const dataRows = table.querySelectorAll("tbody tr");
        const isEmptyState =
            dataRows.length === 1 &&
            dataRows[0].querySelector("td[colspan]") !== null;

        if (isEmptyState) {
            /* Skip DataTable init — the empty message is already shown by Thymeleaf */
            return;
        }

        const headers = Array.from(table.querySelectorAll("thead th"));
        const nonOrderableTargets = [];
        const nonSearchableTargets = [];

        headers.forEach((header, index) => {
            if (header.dataset.orderable === "false") {
                nonOrderableTargets.push(index);
            }

            if (header.dataset.searchable === "false") {
                nonSearchableTargets.push(index);
            }
        });

        const tableInstance = new DataTable(table, {
            responsive: true,
            autoWidth: false,
            stateSave: table.dataset.stateSave !== "false",
            pageLength: Number(table.dataset.pageLength || 10),
            lengthChange: table.dataset.lengthChange !== "false",
            searching: table.dataset.searching !== "false",
            paging: table.dataset.paging !== "false",
            order: resolveDefaultOrder(table),
            columnDefs: [
                {
                    targets: nonOrderableTargets,
                    orderable: false,
                },
                {
                    targets: nonSearchableTargets,
                    searchable: false,
                },
            ],
            language: {
                emptyTable: "No hay datos para mostrar",
                zeroRecords: "No se encontraron resultados",
                info: "Mostrando _START_ a _END_ de _TOTAL_ registros",
                infoEmpty: "Sin registros disponibles",
                infoFiltered: "(filtrado de _MAX_ registros)",
                lengthMenu: "Mostrar _MENU_ filas",
                search: "Buscar",
                searchPlaceholder:
                    table.dataset.searchPlaceholder || "Buscar...",
                paginate: {
                    first: "Primera",
                    previous: "Anterior",
                    next: "Siguiente",
                    last: "Ultima",
                },
            },
        });

        const wrapper = table.closest(".dt-container");
        if (wrapper) {
            wrapper.classList.add("ff-datatable-wrap");
        }

        table.__ffDataTable = tableInstance;
        window.ffTables.push(tableInstance);
    });
}

function initializeTableFilters() {
    const filterScopes = document.querySelectorAll("[data-ff-table-filters]");

    filterScopes.forEach((scope) => {
        const targetTableId = scope.dataset.targetTable;
        if (!targetTableId) {
            return;
        }

        const table = document.getElementById(targetTableId);
        if (!table) {
            return;
        }

        const controls = Array.from(scope.querySelectorAll("[data-ff-filter-key]"));
        if (controls.length === 0) {
            return;
        }

        const applyFilters = () => {
            const activeFilters = {};

            controls.forEach((control) => {
                const key = control.dataset.ffFilterKey;
                const value = String(control.value || "").trim().toLowerCase();
                if (!key || !value || value === "all") {
                    return;
                }
                activeFilters[key] = value;
            });

            table.dataset.ffActiveFilters = JSON.stringify(activeFilters);

            if (table.__ffDataTable) {
                table.__ffDataTable.draw();
                return;
            }

            const rows = table.querySelectorAll("tbody tr[data-ff-row]");
            rows.forEach((row) => {
                row.hidden = !rowMatchesFilters(row, activeFilters);
            });
        };

        controls.forEach((control) => {
            control.addEventListener("change", applyFilters);
            control.addEventListener("input", applyFilters);
        });

        applyFilters();
    });
}

function ensureDataTableFilterHook() {
    if (!DataTable.ext || !Array.isArray(DataTable.ext.search) || DataTable.ext.search.__ffRegistered) {
        return;
    }

    DataTable.ext.search.push((settings, _data, dataIndex) => {
        const table = settings?.nTable;
        if (!table) {
            return true;
        }

        const activeFilters = parseActiveFilters(table.dataset.ffActiveFilters);
        if (Object.keys(activeFilters).length === 0) {
            return true;
        }

        const row = settings.aoData?.[dataIndex]?.nTr;
        if (!row) {
            return true;
        }

        return rowMatchesFilters(row, activeFilters);
    });

    DataTable.ext.search.__ffRegistered = true;
}

function parseActiveFilters(raw) {
    if (!raw) {
        return {};
    }

    try {
        const parsed = JSON.parse(raw);
        return parsed && typeof parsed === "object" ? parsed : {};
    } catch (_error) {
        return {};
    }
}

function rowMatchesFilters(row, activeFilters) {
    return Object.entries(activeFilters).every(([key, expected]) => {
        const datasetKey = key.replace(/-([a-z])/g, (_match, letter) => letter.toUpperCase());
        const actualValue = String(row.dataset[datasetKey] || "").trim().toLowerCase();

        if (!actualValue) {
            return false;
        }

        return expected.split(",").map((value) => value.trim()).includes(actualValue);
    });
}

function resolveDefaultOrder(table) {
    const orderColumn = Number(table.dataset.orderColumn ?? 0);
    const orderDirection = table.dataset.orderDirection || "asc";

    return Number.isNaN(orderColumn)
        ? [[0, "asc"]]
        : [[orderColumn, orderDirection]];
}
