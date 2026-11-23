document.addEventListener('DOMContentLoaded', function () {
    // Confirmación en los formularios que la piden (cancelar, terminada...)
    const confirmables = document.querySelectorAll('form[data-confirmar]');
    for (const f of confirmables) {
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

    // Modal ampliable para imágenes de referencia en el hilo
    const modalEl = document.getElementById('modalImagenReferencia');
    if (modalEl) {
        const img = document.getElementById('imgReferenciaAmpliada');
        const label = document.getElementById('tituloModalReferencia');
        const btnCerrarRef = modalEl.querySelector('.btn-close');

        function abrirModalReferencia(btn) {
            const imgMini = btn.querySelector('img');
            const src = btn.getAttribute('data-img-src')
                || (imgMini ? (imgMini.currentSrc || imgMini.getAttribute('src')) : '')
                || '';
            const titulo = btn.getAttribute('data-img-titulo')
                || (imgMini ? imgMini.getAttribute('alt') : '')
                || 'Imagen de referencia';

            if (label) {
                label.textContent = titulo;
            }
            if (img) {
                img.alt = titulo;
                img.src = src;
            }

            modalEl.style.display = 'block';
            modalEl.style.opacity = '1';
            modalEl.classList.add('show');
            modalEl.removeAttribute('aria-hidden');
            modalEl.setAttribute('aria-modal', 'true');
            document.body.classList.add('modal-open');
        }

        function cerrarModalReferencia() {
            modalEl.classList.remove('show');
            modalEl.style.opacity = '';
            modalEl.style.display = 'none';
            modalEl.setAttribute('aria-hidden', 'true');
            modalEl.removeAttribute('aria-modal');
            document.body.classList.remove('modal-open');
            if (img) {
                img.removeAttribute('src');
            }
        }

        document.querySelectorAll('.ref-miniatura').forEach(function (btn) {
            if (btn.dataset.modalLigado) return;
            btn.dataset.modalLigado = 'si';
            btn.addEventListener('click', function (e) {
                e.preventDefault();
                abrirModalReferencia(btn);
            });
        });

        if (btnCerrarRef) {
            btnCerrarRef.addEventListener('click', function (e) {
                e.preventDefault();
                e.stopPropagation();
                cerrarModalReferencia();
            });
        }

        modalEl.addEventListener('click', function (e) {
            if (e.target === modalEl) {
                cerrarModalReferencia();
            }
        });

        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && modalEl.classList.contains('show')) {
                cerrarModalReferencia();
            }
        });
    }

    const form = document.getElementById('formMensaje');
    if (!form) return;
    const texto = form.querySelector('textarea');

    // Al entrar, baja hasta el final de la conversación
    form.scrollIntoView({ block: 'end' });

    // Ctrl+Enter (o Cmd+Enter) envía el mensaje
    if (texto) {
        texto.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
                e.preventDefault();
                form.requestSubmit();
            }
        });
    }
});