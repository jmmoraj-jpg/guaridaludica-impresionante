document.addEventListener('DOMContentLoaded', function () {
    const buscador = document.getElementById('buscarPieza');
    const lista = document.getElementById('listaPiezas');
    const aviso = document.getElementById('sinResultados');
    if (!buscador || !lista) return;

    // Guarda todas las opciones al cargar la página
    const todas = [];
    for (const o of lista.options) {
        todas.push({ valor: o.value, texto: o.text });
    }

    // Ignora mayúsculas y tildes: "dragon" encuentra "Dragón"
    function normalizar(texto) {
        return texto.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
    }

    function filtrar() {
        const busqueda = normalizar(buscador.value.trim());
        const elegida = lista.value;
        lista.innerHTML = '';
        let visibles = 0;

        for (const p of todas) {
            if (busqueda === '' || normalizar(p.texto).includes(busqueda)) {
                const opcion = document.createElement('option');
                opcion.value = p.valor;
                opcion.textContent = p.texto;
                if (p.valor === elegida) {
                    opcion.selected = true;
                }
                lista.appendChild(opcion);
                visibles++;
            }
        }

        // Si solo queda una, se deja elegida: escribir y pulsar Enter basta
        if (visibles === 1) {
            lista.selectedIndex = 0;
        }
        aviso.classList.toggle('d-none', visibles > 0);
    }

    buscador.addEventListener('input', filtrar);
});