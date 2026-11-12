document.addEventListener("DOMContentLoaded", function () {
    const colorPicker = document.getElementById("inputColorPicker");
    const colorText = document.getElementById("inputColorText");

    if (colorPicker && colorText) {
        // 1. Si al cargar (ej. modo edición) la caja de texto ya tiene un color válido, sintonizamos el picker
        if (colorText.value.match(/^#([A-Fa-f0-9]{6})$/)) {
            colorPicker.value = colorText.value;
        }

        // 2. Cuando el usuario cambia el selector visual, actualiza la caja de texto
        colorPicker.addEventListener("input", function () {
            colorText.value = colorPicker.value;
        });

        // 3. Cuando el usuario escribe en el texto, si es un HEX válido, actualiza el selector visual
        colorText.addEventListener("input", function () {
            const val = colorText.value;
            if (val.match(/^#([A-Fa-f0-9]{6})$/)) {
                colorPicker.value = val;
            }
        });
    }
});