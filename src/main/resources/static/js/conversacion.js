document.addEventListener('DOMContentLoaded', function () {
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