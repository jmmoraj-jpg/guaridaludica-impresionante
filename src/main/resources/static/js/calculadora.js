document.addEventListener("DOMContentLoaded", () => {
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

    function formatearEuros(valor) {
        return valor.toFixed(2).replace(".", ",") + " €";
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
            const gramos = parseFloat(fila.querySelector(".input-gramos").value) || 0;
            const precioKg = parseFloat(fila.querySelector(".input-precio-kg").value) || 0;

            gramosTotales += gramos;
            costeMaterial += (gramos / 1000) * precioKg;
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
            ? Math.ceil(precioExacto * 2) / 2
            : precioExacto;

        const beneficio = precioFinal - costeTotal;

        // Pintar resultados
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
    }

    // Añadir una nueva fila de color clonando la primera
    btnAnadirFilamento.addEventListener("click", () => {
        const primeraFila = listaFilamentos.querySelector(".fila-filamento");
        const nuevaFila = primeraFila.cloneNode(true);

        // Reiniciar valores de la nueva fila
        const select = nuevaFila.querySelector(".select-filamento");
        select.selectedIndex = 0;
        nuevaFila.querySelector(".input-gramos").value = 15;
        nuevaFila.querySelector(".input-precio-kg").value = 20;

        listaFilamentos.appendChild(nuevaFila);
        actualizarBotonesQuitar();
        calcular();
    });

    // Delegación de eventos dentro de la lista de filamentos (funciona para filas nuevas y existentes)
    listaFilamentos.addEventListener("change", (e) => {
        if (e.target.classList.contains("select-filamento")) {
            const fila = e.target.closest(".fila-filamento");
            const opcion = e.target.options[e.target.selectedIndex];
            const precio = parseFloat(opcion.dataset.precio);
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

    // Al elegir una pieza del catálogo, rellena su tiempo si lo tiene guardado
    selectPieza.addEventListener("change", () => {
        const opcion = selectPieza.options[selectPieza.selectedIndex];
        const tiempo = parseInt(opcion.dataset.tiempo, 10);
        if (!isNaN(tiempo) && tiempo > 0) {
            inputMinutos.value = tiempo;
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

    actualizarBotonesQuitar();
    calcular();
});