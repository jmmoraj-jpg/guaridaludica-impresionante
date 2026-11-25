document.addEventListener("DOMContentLoaded", () => {
    // Constantes por defecto del taller (alineadas con la lógica del backend)
    const PRECIO_KG_DEFECTO = 20;
    const GRAMOS_FILA_NUEVA_DEFECTO = 15;
    const GRAMOS_INICIAL_DEFECTO = 50;
    const MARGEN_DEFECTO = 100;
    const REDONDEO_PASO_EUROS = 0.5;

    const selectPieza = document.getElementById("selectPieza");
    const inputMinutos = document.getElementById("minutos");
    const listaFilamentos = document.getElementById("listaFilamentos");
    const btnAnadirFilamento = document.getElementById("btnAnadirFilamento");

    const inputCosteHora = document.getElementById("costeHora");
    const inputFactorRiesgo = document.getElementById("factorRiesgo");
    const inputMargen = document.getElementById("margenBeneficio");
    const inputExtras = document.getElementById("extras");
    const checkRedondear = document.getElementById("redondear");

    const textoHoras = document.getElementById("textoHoras");
    const resGramosTotales = document.getElementById("resGramosTotales");
    const resMaterial = document.getElementById("resMaterial");
    const resEnergia = document.getElementById("resEnergia");
    const resRiesgo = document.getElementById("resRiesgo");
    const resExtras = document.getElementById("resExtras");
    const resCosteTotal = document.getElementById("resCosteTotal");
    const resPrecioExacto = document.getElementById("resPrecioExacto");
    const resPrecioFinal = document.getElementById("resPrecioFinal");
    const resBeneficio = document.getElementById("resBeneficio");

    // Referencias para el formulario de guardado en base de datos
    const inputProductoId = document.getElementById("inputProductoId");
    const inputCosteProduccion = document.getElementById("inputCosteProduccion");
    const inputMargenBeneficio = document.getElementById("inputMargenBeneficio");
    const inputPrecioSugerido = document.getElementById("inputPrecioSugerido");
    const btnGuardarCalculo = document.getElementById("btnGuardarCalculo");

    // Guardamos las opciones HTML del inventario de filamentos del primer select antes de alterar nada
    const selectInicial = document.querySelector(".select-filamento");
    const opcionesFilamentosHtml = selectInicial
        ? selectInicial.innerHTML
        : `<option value="" data-precio="${PRECIO_KG_DEFECTO}">Estándar / Otro (20,00 €/kg)</option>`;

    function formatearEuros(valor) {
        return valor.toFixed(2).replace(".", ",") + " €";
    }

    function redondearAlAlza(valor, paso) {
        if (!paso || paso <= 0) return valor;
        const factor = 1 / paso;
        return Math.ceil(valor * factor) / factor;
    }

    function actualizarTextoHoras(minutos) {
        const h = Math.floor(minutos / 60);
        const m = minutos % 60;
        textoHoras.textContent = `Equivale a ${h} h ${m} min`;
    }

    function actualizarBotonesQuitar() {
        const filas = listaFilamentos.querySelectorAll(".fila-filamento");
        filas.forEach((fila) => {
            const btnQuitar = fila.querySelector(".btn-quitar-filamento");
            if (filas.length > 1) {
                btnQuitar.classList.remove("d-none");
            } else {
                btnQuitar.classList.add("d-none");
            }
        });
    }

    function renderizarFilaFilamento(filamentoId = "", gramos = 0, precioKg = PRECIO_KG_DEFECTO, mostrarQuitar = false) {
        const filaDiv = document.createElement("div");
        filaDiv.className = "fila-filamento border rounded-3 p-2 bg-light";
        filaDiv.innerHTML = `
            <div class="row g-2 align-items-end">
                <div class="col-md-5">
                    <label class="form-label small text-muted mb-1">Filamento</label>
                    <select class="form-select form-select-sm select-filamento">
                        <option value="" data-precio="${PRECIO_KG_DEFECTO}" disabled ${!filamentoId ? "selected" : ""}>-- Selecciona un filamento --</option>
                        ${opcionesFilamentosHtml}
                    </select>
                </div>
                <div class="col-5 col-md-3">
                    <label class="form-label small text-muted mb-1">Gramos (g)</label>
                    <input type="number" class="form-control form-control-sm input-gramos" min="0" step="0.1" value="${gramos}">
                </div>
                <div class="col-5 col-md-3">
                    <label class="form-label small text-muted mb-1">Precio (€/kg)</label>
                    <input type="number" class="form-control form-control-sm input-precio-kg" min="0" step="0.5" value="${precioKg}">
                </div>
                <div class="col-2 col-md-1 text-end">
                    <button type="button" class="btn btn-sm btn-outline-danger btn-quitar-filamento ${mostrarQuitar ? "" : "d-none"}" title="Quitar color">✕</button>
                </div>
            </div>
        `;

        listaFilamentos.appendChild(filaDiv);

        const selectAgregado = filaDiv.querySelector(".select-filamento");
        if (filamentoId) {
            selectAgregado.value = filamentoId;
        }
    }

    function resetearFilamentosPorDefecto(gramosDefecto = GRAMOS_INICIAL_DEFECTO) {
        listaFilamentos.innerHTML = "";
        renderizarFilaFilamento("", gramosDefecto, PRECIO_KG_DEFECTO, false);
        actualizarBotonesQuitar();
    }

    function calcular() {
        const minutos = parseFloat(inputMinutos.value) || 0;
        const costeHora = parseFloat(inputCosteHora.value) || 0;
        const factorRiesgo = parseFloat(inputFactorRiesgo.value) || 0;
        const margen = parseFloat(inputMargen.value) || 0;
        const extras = parseFloat(inputExtras.value) || 0;

        actualizarTextoHoras(minutos);

        // 1. Sumar gramos y coste de todos los filamentos añadidos
        let gramosTotales = 0;
        let costeMaterial = 0;

        listaFilamentos.querySelectorAll(".fila-filamento").forEach((fila) => {
            const inputGramos = fila.querySelector(".input-gramos");
            const inputPrecio = fila.querySelector(".input-precio-kg");

            const gramos = inputGramos && inputGramos.value !== "" ? parseFloat(inputGramos.value) : 0;
            const precioKg = inputPrecio && inputPrecio.value !== "" ? parseFloat(inputPrecio.value) : 0;

            gramosTotales += isNaN(gramos) ? 0 : gramos;
            costeMaterial += ((isNaN(gramos) ? 0 : gramos) / 1000) * (isNaN(precioKg) ? 0 : precioKg);
        });

        // 2. Coste energético y desgaste de máquina
        const horas = minutos / 60;
        const costeEnergia = horas * costeHora;

        // 3. Colchón por posibles fallos / purgas / soportes
        const subtotalBase = costeMaterial + costeEnergia;
        const costeRiesgo = subtotalBase * (factorRiesgo / 100);

        // 4. Coste real total
        const costeTotal = subtotalBase + costeRiesgo + extras;

        // 5. Precio con margen
        const precioExacto = costeTotal * (1 + margen / 100);
        const precioFinal = checkRedondear.checked
            ? redondearAlAlza(precioExacto, REDONDEO_PASO_EUROS)
            : precioExacto;

        const beneficio = precioFinal - costeTotal;

        // Pintar resultados de gramos y coste de material
        resGramosTotales.textContent = gramosTotales % 1 === 0
            ? gramosTotales.toFixed(0)
            : gramosTotales.toFixed(1).replace(".", ",");

        resMaterial.textContent = formatearEuros(costeMaterial);
        resEnergia.textContent = formatearEuros(costeEnergia);
        resRiesgo.textContent = formatearEuros(costeRiesgo);
        resExtras.textContent = formatearEuros(extras);
        resCosteTotal.textContent = formatearEuros(costeTotal);
        resPrecioExacto.textContent = formatearEuros(precioExacto);
        resPrecioFinal.textContent = formatearEuros(precioFinal);
        resBeneficio.textContent = "+" + formatearEuros(beneficio);

        // --- ACTUALIZAR CAMPOS OCULTOS PARA GUARDAR EN BBDD ---
        if (inputProductoId) {
            inputProductoId.value = selectPieza.value;
            inputCosteProduccion.value = costeTotal.toFixed(2);
            inputMargenBeneficio.value = margen;
            inputPrecioSugerido.value = precioFinal.toFixed(2);

            if (selectPieza.value && selectPieza.value !== "") {
                btnGuardarCalculo.removeAttribute("disabled");
            } else {
                btnGuardarCalculo.setAttribute("disabled", "true");
            }
        }
    }

    // Añadir una nueva fila de color clonando la primera
    btnAnadirFilamento.addEventListener("click", () => {
        const primeraFila = listaFilamentos.querySelector(".fila-filamento");
        const nuevaFila = primeraFila.cloneNode(true);

        const select = nuevaFila.querySelector(".select-filamento");
        select.selectedIndex = 0;
        nuevaFila.querySelector(".input-gramos").value = GRAMOS_FILA_NUEVA_DEFECTO;
        nuevaFila.querySelector(".input-precio-kg").value = PRECIO_KG_DEFECTO;

        listaFilamentos.appendChild(nuevaFila);
        actualizarBotonesQuitar();
        calcular();
    });

    // Delegación de eventos dentro de la lista de filamentos
    listaFilamentos.addEventListener("change", (e) => {
        if (e.target.classList.contains("select-filamento")) {
            const fila = e.target.closest(".fila-filamento");
            const opcion = e.target.options[e.target.selectedIndex];
            const precioStr = opcion.getAttribute("data-precio");
            const precio = parseFloat(precioStr);
            if (!isNaN(precio)) {
                fila.querySelector(".input-precio-kg").value = precio;
            }
            calcular();
        }
    });

    listaFilamentos.addEventListener("input", (e) => {
        if (e.target.classList.contains("input-gramos") || e.target.classList.contains("input-precio-kg")) {
            calcular();
        }
    });

    listaFilamentos.addEventListener("click", (e) => {
        const btnQuitar = e.target.closest(".btn-quitar-filamento");
        if (btnQuitar) {
            const filas = listaFilamentos.querySelectorAll(".fila-filamento");
            if (filas.length > 1) {
                btnQuitar.closest(".fila-filamento").remove();
                actualizarBotonesQuitar();
                calcular();
            }
        }
    });

    // Al elegir una pieza del catálogo, rellena su tiempo, margen y filamentos guardados (o reinicia si elige manual)
    selectPieza.addEventListener("change", () => {
        const opcion = selectPieza.options[selectPieza.selectedIndex];

        if (!selectPieza.value) {
            inputMargen.value = MARGEN_DEFECTO;
            resetearFilamentosPorDefecto(GRAMOS_INICIAL_DEFECTO);
            calcular();
            return;
        }

        const tiempo = parseInt(opcion.dataset.tiempo, 10);
        inputMinutos.value = !isNaN(tiempo) ? tiempo : 0;

        const margenGuardado = parseFloat(opcion.dataset.margen);
        inputMargen.value = !isNaN(margenGuardado) ? margenGuardado : MARGEN_DEFECTO;

        const filamentosData = opcion.dataset.filamentos;
        if (filamentosData && filamentosData.trim() !== "") {
            listaFilamentos.innerHTML = "";

            const items = filamentosData.split(",");
            const mostrarQuitar = items.length > 1;
            items.forEach((item) => {
                const [filamentoId, gramos, precioKg] = item.split(":");
                renderizarFilaFilamento(
                    filamentoId || "",
                    gramos !== undefined ? gramos : 0,
                    precioKg !== undefined ? precioKg : PRECIO_KG_DEFECTO,
                    mostrarQuitar
                );
            });
            actualizarBotonesQuitar();
        } else {
            resetearFilamentosPorDefecto(0);
        }

        calcular();
    });

    // Recalcular ante cambios en el resto de parámetros
    [
        inputMinutos,
        inputCosteHora,
        inputFactorRiesgo,
        inputMargen,
        inputExtras,
        checkRedondear
    ].forEach(el => el.addEventListener("input", calcular));

    const preseleccionado = selectPieza.dataset.preseleccionado;
    if (preseleccionado) {
        const existeOpcion = Array.from(selectPieza.options).some(opt => opt.value === preseleccionado);
        if (existeOpcion) {
            selectPieza.value = preseleccionado;
            selectPieza.dispatchEvent(new Event("change"));
        }
    }
    
    actualizarBotonesQuitar();
    calcular();
});