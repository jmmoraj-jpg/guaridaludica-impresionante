document.addEventListener('DOMContentLoaded', function () {
    // Confirmación en los formularios que la piden (cancelar, terminada...)
    const confirmables = document.querySelectorAll('form[data-confirmar]');
    for (const f of confirmables) {
        // Si ya se registró (script cargado dos veces), no se repite
        if (f.dataset.confirmacionLista) {
            continue;
        }
        f.dataset.confirmacionLista = 'si';
        f.addEventListener('submit', function (e) {
            if (!confirm(f.dataset.confirmar)) {
                e.preventDefault();
            }
        });
    }

    const form = document.getElementById('formMensaje');
    if (!form) return;
    const texto = form.querySelector('textarea');

    // Al entrar, baja hasta el final de la conversación
    form.scrollIntoView({ block: 'end' });

    // Ctrl+Enter (o Cmd+Enter) envía el mensaje
    texto.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
            e.preventDefault();
            form.requestSubmit();
        }
    });
});