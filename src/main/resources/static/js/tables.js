/* ─────────────────────────────────────────────────────────────
   FlacoFitness — DataTables Init (v3)
   ───────────────────────────────────────────────────────────── */

document.addEventListener("DOMContentLoaded", () => {
    initializeDataTables();
});

function initializeDataTables() {
    if (typeof DataTable === "undefined") {
        return;
    }

    const tables = document.querySelectorAll("[data-ff-datatable]");
    window.ffTables = [];

    tables.forEach((table) => {
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

        window.ffTables.push(tableInstance);
    });
}

function resolveDefaultOrder(table) {
    const orderColumn = Number(table.dataset.orderColumn ?? 0);
    const orderDirection = table.dataset.orderDirection || "asc";

    return Number.isNaN(orderColumn)
        ? [[0, "asc"]]
        : [[orderColumn, orderDirection]];
}
