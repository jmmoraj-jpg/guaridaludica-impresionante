document.addEventListener('DOMContentLoaded', function () {
    const input = document.getElementById('nuevas');
    const contenedor = document.getElementById('previas');
    if (!input || !contenedor) return;

    input.addEventListener('change', function () {
        contenedor.innerHTML = '';
        const hayPortada = document.querySelector('input[name="portada"]:checked') !== null;

        Array.from(input.files).forEach(function (archivo, i) {
            const caja = document.createElement('div');
            caja.className = 'img-admin';

            const img = document.createElement('img');
            img.src = URL.createObjectURL(archivo);

            const etiqueta = document.createElement('label');
            etiqueta.className = 'form-check';

            const radio = document.createElement('input');
            radio.type = 'radio';
            radio.name = 'portada';
            radio.value = 'nueva-' + i;
            if (!hayPortada && i === 0) radio.checked = true;

            etiqueta.appendChild(radio);
            etiqueta.appendChild(document.createTextNode(' Portada'));
            caja.appendChild(img);
            caja.appendChild(etiqueta);
            contenedor.appendChild(caja);
        });
    });
});