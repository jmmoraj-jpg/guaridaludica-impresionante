document.addEventListener('DOMContentLoaded', function () {
    // Si el visitante pide menos movimiento (o el navegador es muy antiguo), todo queda tal cual
    const sinMovimiento = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (sinMovimiento || !('IntersectionObserver' in window)) return;

    document.documentElement.classList.add('js-anim');

    // 1. Las piezas se "imprimen" cuando entran en pantalla
    const columnas = [];
    const piezas = document.querySelectorAll('.pieza');
    for (let i = 0; i < piezas.length; i++) {
        const columna = piezas[i].closest('.col');
        if (columna) {
            columnas.push(columna);
        }
    }

    const observador = new IntersectionObserver(function (entradas) {
        for (let i = 0; i < entradas.length; i++) {
            if (entradas[i].isIntersecting) {
                entradas[i].target.classList.add('impreso');
                observador.unobserve(entradas[i].target);
            }
        }
    }, { threshold: 0.1 });

    for (let i = 0; i < columnas.length; i++) {
        columnas[i].classList.add('imprimir');
        // Escalonado por columna: una fila se imprime de izquierda a derecha
        columnas[i].style.setProperty('--retraso', (i % 4) * 50 + 'ms');
        observador.observe(columnas[i]);
    }

    // 2. El porcentaje de la impresora sube contando, a la vez que se llena la barra
    const impresiones = document.querySelectorAll('.impresion');
    for (let i = 0; i < impresiones.length; i++) {
        const barra = impresiones[i].querySelector('.progreso-barra');
        const textos = impresiones[i].querySelectorAll('.d-flex span');
        if (!barra || textos.length === 0) continue;

        const meta = parseInt(barra.style.width, 10);
        if (isNaN(meta)) continue;

        contar(textos[textos.length - 1], meta);
    }

    function contar(elemento, meta) {
        const duracion = 1200; // igual que la barra en animaciones.css
        const espera = 300;
        const inicio = performance.now() + espera;
        elemento.textContent = '0%';

        function fotograma(ahora) {
            const avance = Math.min(Math.max((ahora - inicio) / duracion, 0), 1);
            const suave = 1 - Math.pow(1 - avance, 3);
            elemento.textContent = Math.round(meta * suave) + '%';
            if (avance < 1) {
                requestAnimationFrame(fotograma);
            }
        }
        requestAnimationFrame(fotograma);
    }
});
