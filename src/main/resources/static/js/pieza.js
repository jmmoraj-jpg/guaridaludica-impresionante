(function () {
    const desc = document.getElementById('textoDescripcion');
    const btn = document.getElementById('btnToggleDescripcion');
    if (!desc || !btn) return;

    function evaluarAltura() {
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
    });

    window.addEventListener('load', evaluarAltura);
    window.addEventListener('resize', function () {
        if (!desc.classList.contains('expandida')) {
            evaluarAltura();
        }
    });
})();