function urlBase64ToUint8Array(base64String) {
    const padding = "=".repeat((4 - (base64String.length % 4)) % 4);
    const base64 = (base64String + padding).replace(/-/g, "+").replace(/_/g, "/");
    const rawData = window.atob(base64);
    const outputArray = new Uint8Array(rawData.length);
    for (let i = 0; i < rawData.length; ++i) {
        outputArray[i] = rawData.charCodeAt(i);
    }
    return outputArray;
}

function obtenerCsrfHeaders() {
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    const headerMeta = document.querySelector('meta[name="_csrf_header"]');
    if (tokenMeta && headerMeta && tokenMeta.content && headerMeta.content) {
        return { [headerMeta.content]: tokenMeta.content };
    }
    return {};
}

async function activarNotificacionesPush() {
    if (!("serviceWorker" in navigator) || !("PushManager" in window) || !("Notification" in window)) {
        alert("Este navegador no soporta notificaciones push web.");
        return;
    }

    const permiso = await Notification.requestPermission();
    if (permiso !== "granted") {
        alert("No se concedió el permiso de notificaciones.");
        return;
    }

    const keyResp = await fetch("/api/push/vapid-public-key", { credentials: "same-origin" });
    if (!keyResp.ok) {
        throw new Error("No se pudo obtener la clave pública VAPID.");
    }
    const { publicKey } = await keyResp.json();
    if (!publicKey) {
        throw new Error("La clave pública VAPID está vacía en el servidor.");
    }

    const registration = await navigator.serviceWorker.register("/sw.js");
    await navigator.serviceWorker.ready;

    const subscription = await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: urlBase64ToUint8Array(publicKey)
    });

    const jsonSub = subscription.toJSON();
    const body = {
        endpoint: jsonSub.endpoint,
        p256dh: jsonSub.keys && jsonSub.keys.p256dh,
        auth: jsonSub.keys && jsonSub.keys.auth
    };

    const subResp = await fetch("/api/push/suscribir", {
        method: "POST",
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/json",
            ...obtenerCsrfHeaders()
        },
        body: JSON.stringify(body)
    });

    if (!subResp.ok) {
        throw new Error("Error al guardar la suscripción en el servidor (" + subResp.status + ").");
    }
}