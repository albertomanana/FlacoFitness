let usuariosActivosCache = null;

async function cargarUsuariosActivos() {
    if (usuariosActivosCache) {
        return usuariosActivosCache;
    }

    try {
        const response = await fetch("/api/staff/usuarios-activos");
        const result = await response.json();
        usuariosActivosCache = result.success ? result.usuarios : [];
        if (!result.success) {
            showStaffNotification(result.mensaje || "No se pudieron cargar los clientes.", "danger");
        }
        return usuariosActivosCache;
    } catch (_error) {
        showStaffNotification("No se pudieron cargar los clientes.", "danger");
        return [];
    }
}

async function registrarAsistenciaStaff(usuarioId) {
    try {
        const response = await fetch(`/api/staff/registrar-asistencia?usuarioId=${encodeURIComponent(usuarioId)}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" }
        });
        const result = await response.json();
        if (result.success) {
            showStaffNotification(`${result.usuarioNombre} registrado a las ${result.hora}.`, "success");
            window.setTimeout(() => location.reload(), 1200);
            return;
        }
        showStaffNotification(result.mensaje || "No se pudo registrar la asistencia.", "danger");
    } catch (_error) {
        showStaffNotification("No se pudo conectar con el servidor.", "danger");
    }
}

async function llenarDropdownUsuarios() {
    const dropdown = document.querySelector('[name="usuarioCheckIn"]');
    if (!dropdown) {
        return;
    }

    const usuarios = await cargarUsuariosActivos();
    const selectedValue = dropdown.value;
    dropdown.innerHTML = '<option disabled selected value="">Selecciona un cliente...</option>';
    usuarios.forEach((usuario) => {
        const option = document.createElement("option");
        option.value = usuario.id;
        option.textContent = `${usuario.nombre} (${usuario.email})`;
        option.dataset.search = `${usuario.nombre} ${usuario.email}`.toLowerCase();
        dropdown.appendChild(option);
    });
    if (selectedValue) {
        dropdown.value = selectedValue;
    }

    const search = document.querySelector("[data-staff-user-search]");
    if (search && !search.dataset.ready) {
        search.addEventListener("input", () => {
            const query = normalizeText(search.value);
            Array.from(dropdown.options).forEach((option) => {
                if (!option.value) {
                    return;
                }
                option.hidden = query.length > 0 && !normalizeText(option.textContent).includes(query);
            });
        });
        search.dataset.ready = "true";
    }
}

async function handleCheckInFormStaff(form) {
    const usuarioId = form.querySelector('[name="usuarioCheckIn"]')?.value;
    if (!usuarioId) {
        showStaffNotification("Selecciona un cliente.", "warning");
        return;
    }
    await registrarAsistenciaStaff(usuarioId);
}

function showStaffNotification(message, type = "info") {
    const alertDiv = document.createElement("div");
    alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
    alertDiv.style.cssText = "top:20px;right:20px;z-index:10000;min-width:280px;box-shadow:0 12px 30px rgba(15,23,42,.16);";
    alertDiv.setAttribute("role", "alert");
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
    `;
    document.body.appendChild(alertDiv);
    window.setTimeout(() => alertDiv.remove(), 5000);
}

function normalizeText(value) {
    return String(value || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase()
        .trim();
}

document.addEventListener("DOMContentLoaded", llenarDropdownUsuarios);

window.cargarUsuariosActivos = cargarUsuariosActivos;
window.registrarAsistenciaStaff = registrarAsistenciaStaff;
window.handleCheckInFormStaff = handleCheckInFormStaff;
window.llenarDropdownUsuarios = llenarDropdownUsuarios;
