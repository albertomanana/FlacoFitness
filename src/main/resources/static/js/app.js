document.addEventListener("DOMContentLoaded", () => {
    const yearElements = document.querySelectorAll("[data-current-year]");

    yearElements.forEach((element) => {
        element.textContent = String(new Date().getFullYear());
    });
});
