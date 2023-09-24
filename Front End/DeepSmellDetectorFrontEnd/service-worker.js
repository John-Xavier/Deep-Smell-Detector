self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open('my-cache').then((cache) => {
            return cache.addAll([
                '/',
                '/index.html',
                '/detect.html',
                '/about.html',
                '/styles.css',
                'images/logo1.png',
                'images/192.png',
                'images/512.png',
                'images/gradientBlueLines.jpg',
            ]);
        })
    );
});

self.addEventListener('fetch', (event) => {
    event.respondWith(
        caches.match(event.request).then((response) => {
            return response || fetch(event.request);
        })
    );
});
