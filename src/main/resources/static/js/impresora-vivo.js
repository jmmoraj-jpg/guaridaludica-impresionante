// Mantiene al día la tarjeta "Imprimiendo ahora" de la portada, sin recargar la página.
// Consulta al servidor cada pocos segundos, y solo mientras la pestaña está a la vista.
document.addEventListener('DOMContentLoaded', function () {
    const tarjetaInicial = document.getElementById('tarjetaImpresora');
    if (!tarjetaInicial || !tarjetaInicial.dataset.url) return;

    const url = tarjetaInicial.dataset.url;
    const INTERVALO = 20000; // milisegundos entre consultas
    let temporizador = null;

    function programar() {
        clearTimeout(temporizador);
        if (document.hidden) return;
        temporizador = setTimeout(consultar, INTERVALO);
    }

    function consultar() {
        fetch(url, { cache: 'no-store', headers: { 'X-Requested-With': 'fetch' } })
            .then(function (respuesta) {
                if (!respuesta.ok) throw new Error('Respuesta ' + respuesta.status);
                return respuesta.text();
            })
            .then(aplicar)
            .catch(function () {
                // Si falla, no pasa nada: se reintenta en el siguiente ciclo
            })
            .then(programar);
    }

    // Al volver a la pestaña se actualiza al momento; al irse, se deja de consultar
    document.addEventListener('visibilitychange', function () {
        if (document.hidden) {
            clearTimeout(temporizador);
        } else {
            consultar();
        }
    });

    function aplicar(html) {
        const contenedor = document.createElement('div');
        contenedor.innerHTML = html.trim();
        const nueva = contenedor.firstElementChild;
        const actual = document.getElementById('tarjetaImpresora');
        if (!nueva || !actual || nueva.id !== 'tarjetaImpresora') return;

        if (nombres(actual) === nombres(nueva)) {
            // Mismas piezas en la impresora: solo cambian los números
            actualizarNumeros(actual, nueva);
        } else {
            // Entra o sale una pieza (o pasa de ocupada a libre): se cambia la tarjeta entera
            actual.replaceWith(nueva);
        }
    }

    // Nombres de las piezas en curso, para saber si la lista ha cambiado
    function nombres(tarjeta) {
        const lista = [];
        const filas = tarjeta.querySelectorAll('.impresion');
        for (let i = 0; i < filas.length; i++) {
            lista.push(filas[i].querySelector('span').textContent);
        }
        return lista.join('|');
    }

    function actualizarNumeros(actual, nueva) {
        const filasActuales = actual.querySelectorAll('.impresion');
        const filasNuevas = nueva.querySelectorAll('.impresion');

        for (let i = 0; i < filasActuales.length; i++) {
            const barra = filasActuales[i].querySelector('.progreso-barra');
            const barraNueva = filasNuevas[i].querySelector('.progreso-barra');
            const textos = filasActuales[i].querySelectorAll('.d-flex span');
            const texto = textos[textos.length - 1];

            const desde = parseInt(texto.textContent, 10);
            const hasta = parseInt(barraNueva.style.width, 10);
            if (isNaN(hasta) || hasta === desde) continue;

            barra.style.width = hasta + '%'; // la barra crece con la transición de animaciones.css
            animarNumero(texto, isNaN(desde) ? 0 : desde, hasta);
        }

        const colaActual = actual.querySelector('.cola-texto');
        const colaNueva = nueva.querySelector('.cola-texto');
        if (colaActual && colaNueva && colaActual.textContent !== colaNueva.textContent) {
            colaActual.textContent = colaNueva.textContent;
        }
    }

    function animarNumero(elemento, desde, hasta) {
        if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
            elemento.textContent = hasta + '%';
            return;
        }
        const duracion = 800; // igual que la transición de la barra
        const inicio = performance.now();

        function fotograma(ahora) {
            const avance = Math.min((ahora - inicio) / duracion, 1);
            const suave = 1 - Math.pow(1 - avance, 3);
            elemento.textContent = Math.round(desde + (hasta - desde) * suave) + '%';
            if (avance < 1) {
                requestAnimationFrame(fotograma);
            }
        }
        requestAnimationFrame(fotograma);
    }

    programar();
});
