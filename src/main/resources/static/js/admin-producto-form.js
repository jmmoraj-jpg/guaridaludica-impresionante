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