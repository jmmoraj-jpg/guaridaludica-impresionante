(function () {
    const desc = document.getElementById('textoDescripcion');
    const btn = document.getElementById('btnToggleDescripcion');
    if (!desc || !btn) return;

    function evaluarAltura() {
        desc.style.maxHeight = '';
        desc.classList.remove('expandida');
        desc.classList.add('colapsada');

        const esLargo = desc.scrollHeight > desc.clientHeight + 4;
        if (!esLargo) {
            desc.classList.remove('colapsada');
            btn.classList.add('d-none');
            return;
        }

        btn.classList.remove('d-none');
    }

    btn.addEventListener('click', function () {
        const expandido = desc.classList.toggle('expandida');
        desc.classList.toggle('colapsada', !expandido);
        btn.setAttribute('aria-expanded', String(expandido));
        btn.textContent = expandido ? 'Ver menos' : 'Leer más';

        if (expandido) {
            // Fijamos la altura exacta en px para que la transición CSS anime de forma fluida
            desc.style.maxHeight = desc.scrollHeight + 'px';
        } else {
            // Forzamos reflow para que la transición de cierre también se anime
            desc.style.maxHeight = desc.scrollHeight + 'px';
            void desc.offsetHeight;
            desc.style.maxHeight = '';
        }
    });

    window.addEventListener('load', evaluarAltura);
    window.addEventListener('resize', function () {
        if (!desc.classList.contains('expandida')) {
            evaluarAltura();
        } else {
            desc.style.maxHeight = desc.scrollHeight + 'px';
        }
    });
})();