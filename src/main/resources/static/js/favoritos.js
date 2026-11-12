// Corazón de favoritos sin recargar la página.
// Si algo falla (red, sesión caducada...), se envía el formulario de la forma normal.
document.addEventListener('DOMContentLoaded', function () {
    const formularios = document.querySelectorAll('form[action*="/favoritos/"]');
    for (let i = 0; i < formularios.length; i++) {
        formularios[i].addEventListener('submit', alternar);
    }

    function alternar(evento) {
        evento.preventDefault();
        const formulario = evento.currentTarget;
        const boton = formulario.querySelector('button');
        if (!boton || boton.disabled) return;
        boton.disabled = true;

        fetch(formulario.action, {
            method: 'POST',
            headers: { 'X-Requested-With': 'fetch' },
            body: new URLSearchParams(new FormData(formulario))
        }).then(function (respuesta) {
            // Una redirección significa sesión caducada: que lo resuelva el envío normal
            if (!respuesta.ok || respuesta.redirected) {
                throw new Error('No se pudo cambiar el favorito');
            }
            if (boton.classList.contains('btn-corazon')) {
                marcarCorazon(boton);
            } else {
                quitarTarjeta(formulario);
            }
            boton.disabled = false;
        }).catch(function () {
            formulario.submit();
        });
    }

    // Catálogo: el corazón se rellena (o se vacía) en el sitio
    function marcarCorazon(boton) {
        const activo = boton.classList.toggle('activo');
        boton.textContent = activo ? '♥' : '♡';
        if (activo) {
            boton.classList.add('latiendo');
            boton.addEventListener('animationend', function quitar() {
                boton.classList.remove('latiendo');
                boton.removeEventListener('animationend', quitar);
            });
        }
    }

    // Página de favoritos: la tarjeta se desvanece y desaparece de la lista
    function quitarTarjeta(formulario) {
        const columna = formulario.closest('.col');
        if (!columna) {
            window.location.reload();
            return;
        }
        columna.classList.add('saliendo');
        setTimeout(function () {
            columna.remove();
            // Sin piezas, se recarga para que la plantilla muestre el aviso de "aún no tienes favoritos"
            if (document.querySelectorAll('.pieza').length === 0) {
                window.location.reload();
            }
        }, 280);
    }
});
