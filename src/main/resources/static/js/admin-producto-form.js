document.addEventListener('DOMContentLoaded', function () {
    iniciarVistaPreviaImagenes();
    iniciarBuscadorCategorias();
});

function iniciarVistaPreviaImagenes() {
    const input = document.getElementById('nuevas');
    const contenedor = document.getElementById('previas');
    if (!input || !contenedor) return;

    input.addEventListener('change', function () {
        contenedor.innerHTML = '';
        const hayPortada = document.querySelector('input[name="portada"]:checked') !== null;

        Array.from(input.files).forEach(function (archivo, i) {
            const caja = document.createElement('div');
            caja.className = 'img-admin';

            const img = document.createElement('img');
            img.src = URL.createObjectURL(archivo);

            const etiqueta = document.createElement('label');
            etiqueta.className = 'form-check';

            const radio = document.createElement('input');
            radio.type = 'radio';
            radio.name = 'portada';
            radio.value = 'nueva-' + i;
            if (!hayPortada && i === 0) radio.checked = true;

            etiqueta.appendChild(radio);
            etiqueta.appendChild(document.createTextNode(' Portada'));
            caja.appendChild(img);
            caja.appendChild(etiqueta);
            contenedor.appendChild(caja);
        });
    });
}

function iniciarBuscadorCategorias() {
    const buscador = document.getElementById('buscarCategorias');
    const lista = document.getElementById('listaCategoriasForm');
    if (!buscador || !lista) return;

    function normalizar(t) {
        return t.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
    }

    buscador.addEventListener('input', function () {
        const texto = normalizar(buscador.value);
        lista.querySelectorAll('.form-check').forEach(function (fila) {
            fila.hidden = !normalizar(fila.textContent).includes(texto);
        });
    });

    // Enter en el buscador no debe enviar el formulario
    buscador.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') e.preventDefault();
    });
}

document.addEventListener("DOMContentLoaded", () => {
    const btnAnadir = document.getElementById("btnAnadirFilamentoForm");
    const contenedor = document.getElementById("listaFilamentosForm");

    if (btnAnadir && contenedor) {
        function actualizarIndicesYBotones() {
            const filas = contenedor.querySelectorAll(".fila-filamento-form");
            filas.forEach((fila, index) => {
                const select = fila.querySelector("select");
                const inputGramos = fila.querySelector("input[type='number']");
                
                if (select) select.name = `filamentosForm[${index}].filamentoId`;
                if (inputGramos) inputGramos.name = `filamentosForm[${index}].gramos`;

                const btnQuitar = fila.querySelector(".btn-quitar-fila");
                if (btnQuitar) {
                    if (filas.length > 1) {
                        btnQuitar.classList.remove("d-none");
                    } else {
                        btnQuitar.classList.add("d-none");
                    }
                }
            });
        }

        btnAnadir.addEventListener("click", () => {
            const primeraFila = contenedor.querySelector(".fila-filamento-form");
            if (primeraFila) {
                const nuevaFila = primeraFila.cloneNode(true);
                nuevaFila.querySelector("select").selectedIndex = 0;
                nuevaFila.querySelector("input[type='number']").value = "0";
                contenedor.appendChild(nuevaFila);
                actualizarIndicesYBotones();
            }
        });

        contenedor.addEventListener("click", (e) => {
            if (e.target.closest(".btn-quitar-fila")) {
                const fila = e.target.closest(".fila-filamento-form");
                if (contenedor.querySelectorAll(".fila-filamento-form").length > 1) {
                    fila.remove();
                    actualizarIndicesYBotones();
                }
            }
        });

        actualizarIndicesYBotones();
    }
});