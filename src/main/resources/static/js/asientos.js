/**
 * asientos.js — CineComfenalco
 * Selección de asientos del mapa de butacas: máximo 15, ocupados no seleccionables,
 * cálculo de total en tiempo real.
 */

document.addEventListener("DOMContentLoaded", function () {
  const form = document.querySelector("form[action*='/venta'], form[action$='/venta']");
  if (!form) return;

  const precioEl = document.getElementById("precioUnitario");
  const totalEl = document.getElementById("totalSeleccion");
  const contadorEl = document.getElementById("contadorAsientos");
  const precioUnitario = precioEl ? parseFloat(precioEl.dataset.precio || "0") : 0;

  const MAX_ASIENTOS = 15;
  let seleccionados = new Set();

  function actualizarTotal() {
    const total = seleccionados.size * precioUnitario;
    if (totalEl) totalEl.textContent = new Intl.NumberFormat("es-CO").format(total);
    if (contadorEl) contadorEl.textContent = seleccionados.size;
  }

  document.querySelectorAll(".seat:not(.seat--occupied)").forEach(function (asiento) {
    asiento.addEventListener("click", function () {
      const codigo = asiento.dataset.codigo;
      if (seleccionados.has(codigo)) {
        seleccionados.delete(codigo);
        asiento.classList.remove("seat--selected");
        asiento.setAttribute("aria-checked", "false");
      } else {
        if (seleccionados.size >= MAX_ASIENTOS) {
          return;
        }
        seleccionados.add(codigo);
        asiento.classList.add("seat--selected");
        asiento.setAttribute("aria-checked", "true");
      }
      actualizarTotal();
      sincronizarCheckboxes();
    });
  });

  function sincronizarCheckboxes() {
    document.querySelectorAll('input[name="asientos"]').forEach(function (cb) {
      cb.checked = seleccionados.has(cb.value);
    });
  }

  form.addEventListener("submit", function (event) {
    sincronizarCheckboxes();
    if (seleccionados.size === 0) {
      event.preventDefault();
      alert("Selecciona al menos un asiento.");
    }
  });

  actualizarTotal();
});
