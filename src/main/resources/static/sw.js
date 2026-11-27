self.addEventListener("push", (event) => {
    let data = {
        titulo: "La Guarida Lúdica",
        cuerpo: "Tienes una novedad en una conversación.",
        url: "/conversaciones"
    };

    if (event.data) {
        try {
            const parsed = event.data.json();
            data = {
                titulo: parsed.titulo || data.titulo,
                cuerpo: parsed.cuerpo || data.cuerpo,
                url: parsed.url || data.url
            };
        } catch (_) {
            data.cuerpo = event.data.text() || data.cuerpo;
        }
    }

    event.waitUntil(
        self.registration.showNotification(data.titulo, {
            body: data.cuerpo,
            icon: "/favicon.ico",
            data: { url: data.url }
        })
    );
});

self.addEventListener("notificationclick", (event) => {
    event.notification.close();
    const urlDestino = (event.notification.data && event.notification.data.url) || "/conversaciones";

    event.waitUntil(
        clients.matchAll({ type: "window", includeUncontrolled: true }).then((clientList) => {
            for (const client of clientList) {
                if ("focus" in client) {
                    client.navigate(urlDestino);
                    return client.focus();
                }
            }
            if (clients.openWindow) {
                return clients.openWindow(urlDestino);
            }
        })
    );
});