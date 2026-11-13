/**
 * app.js — CineComfenalco
 * Control de tema claro/oscuro, persistencia en localStorage,
 * interactividad del topbar y widgets.
 */

document.addEventListener("DOMContentLoaded", function () {
  // 1. GESTIÓN DEL TEMA CLARO / OSCURO
  const temaAlmacenado = localStorage.getItem("cine_tema") || localStorage.getItem("cine_theme") || "light";
  const themeToggleInput = document.getElementById("theme-toggle");
  const btnLight = document.getElementById("btnTemaClaro");
  const btnDark = document.getElementById("btnTemaOscuro");

  function aplicarTema(tema) {
    if (tema === "dark") {
      document.documentElement.setAttribute("data-theme", "dark");
      document.documentElement.classList.add("dark");
      if (themeToggleInput) themeToggleInput.checked = true;
      if (btnDark) btnDark.classList.add("activo");
      if (btnLight) btnLight.classList.remove("activo");
    } else {
      document.documentElement.setAttribute("data-theme", "light");
      document.documentElement.classList.remove("dark");
      if (themeToggleInput) themeToggleInput.checked = false;
      if (btnLight) btnLight.classList.add("activo");
      if (btnDark) btnDark.classList.remove("activo");
    }
    localStorage.setItem("cine_tema", tema);
    localStorage.setItem("cine_theme", tema);
  }

  // Aplicar tema inicial
  aplicarTema(temaAlmacenado);

  // Escuchar cambio en el checkbox del Topbar
  if (themeToggleInput) {
    themeToggleInput.addEventListener("change", function () {
      aplicarTema(this.checked ? "dark" : "light");
    });
  }

  // Botones secundarios si existen en sidebar
  if (btnLight) {
    btnLight.addEventListener("click", function () {
      aplicarTema("light");
    });
  }
  if (btnDark) {
    btnDark.addEventListener("click", function () {
      aplicarTema("dark");
    });
  }

  // Auto-descarte de alertas flash después de 6 segundos
  const alertas = document.querySelectorAll(".alert");
  if (alertas.length > 0) {
    setTimeout(function () {
      alertas.forEach(function (alerta) {
        alerta.style.transition = "opacity 0.4s ease, transform 0.4s ease";
        alerta.style.opacity = "0";
        alerta.style.transform = "translateY(-6px)";
        setTimeout(function () {
          if (alerta.parentNode) alerta.parentNode.removeChild(alerta);
        }, 400);
      });
    }, 6000);
  }
});
