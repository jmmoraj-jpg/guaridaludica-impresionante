document.addEventListener('DOMContentLoaded', function () {
    const buscador = document.getElementById('buscarCategoria');
    const lista = document.getElementById('listaCategorias');
    const boton = document.getElementById('botonMasCategorias');
    if (!buscador || !lista) return;

    // Sin tildes y en minúsculas, para que "decoracion" encuentre "Decoración"
    function normalizar(t) {
        return t.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
    }

    buscador.addEventListener('input', function () {
        const texto = normalizar(buscador.value);
        lista.querySelectorAll('li').forEach(function (li) {
            li.hidden = !normalizar(li.textContent).includes(texto);
        });
    });

    // Al abrir el desplegable, el cursor va directo al buscador
    if (boton) {
        boton.parentElement.addEventListener('shown.bs.dropdown', function () {
            buscador.focus();
        });
    }
});