async function reservarSesion(sesionId) {
    try {
        const response = await fetch(`/api/cliente/reservar-sesion?sesionId=${encodeURIComponent(sesionId)}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" }
        });
        const result = await response.json();
        if (result.success) {
            showNotification(result.mensaje || "Reserva confirmada.", "success");
            window.setTimeout(() => location.reload(), 1200);
            return;
        }
        showNotification(result.mensaje || "No se pudo reservar la clase.", "danger");
    } catch (_error) {
        showNotification("No se pudo conectar con el servidor.", "danger");
    }
}

async function cancelarReserva(reservaId) {
    if (!window.confirm("¿Cancelar esta reserva?")) {
        return;
    }

    try {
        const response = await fetch(`/api/cliente/cancelar-reserva/${encodeURIComponent(reservaId)}`, {
            method: "DELETE",
            headers: { "Content-Type": "application/json" }
        });
        const result = await response.json();
        if (result.success) {
            showNotification(result.mensaje || "Reserva cancelada.", "success");
            window.setTimeout(() => location.reload(), 1200);
            return;
        }
        showNotification(result.mensaje || "No se pudo cancelar la reserva.", "danger");
    } catch (_error) {
        showNotification("No se pudo conectar con el servidor.", "danger");
    }
}

async function registrarCheckIn(sesionId = null) {
    try {
        const url = sesionId
            ? `/api/cliente/check-in?sesionId=${encodeURIComponent(sesionId)}`
            : "/api/cliente/check-in";
        const response = await fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" }
        });
        const result = await response.json();
        if (result.success) {
            showNotification(result.mensaje || "Check-in registrado.", "success");
            window.setTimeout(() => location.reload(), 1200);
            return;
        }
        showNotification(result.mensaje || "No se pudo registrar el check-in.", "danger");
    } catch (_error) {
        showNotification("No se pudo conectar con el servidor.", "danger");
    }
}

function showNotification(message, type = "info") {
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

window.reservarSesion = reservarSesion;
window.cancelarReserva = cancelarReserva;
window.registrarCheckIn = registrarCheckIn;
