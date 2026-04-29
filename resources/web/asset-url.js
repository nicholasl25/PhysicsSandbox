/**
 * Resolve root-absolute asset paths for non-root deployments (e.g. GitHub Pages subpaths).
 * Set <meta name="app-root" content="/YourRepo"> before this script, or window.__APP_ROOT__ = '/YourRepo'.
 * Leave empty for serving at site root (e.g. http://localhost:8080/).
 */
(function (global) {
    'use strict';

    function metaContent(name) {
        if (typeof document === 'undefined') return null;
        const el = document.querySelector('meta[name="' + name + '"]');
        return el && el.getAttribute('content') != null ? el.getAttribute('content') : null;
    }

    function getAppRoot() {
        if (typeof global.__APP_ROOT__ === 'string') {
            return global.__APP_ROOT__.replace(/\/+$/, '');
        }
        const fromMeta = metaContent('app-root');
        if (fromMeta !== null && fromMeta !== '') {
            return fromMeta.replace(/\/+$/, '');
        }
        return '';
    }

    /**
     * @param {string} path - e.g. "/textures/Earth.jpg"
     * @returns {string}
     */
    function resolveAssetUrl(path) {
        if (path == null || path === '') return path;
        if (/^https?:\/\//i.test(path)) return path;
        const p = String(path);
        if (!p.startsWith('/')) return p;
        const root = getAppRoot();
        if (!root) return p;
        return root + p;
    }

    /**
     * Returns ordered candidates for resilient static hosting.
     * Useful when assets may live in /textures, /resources/textures, ../textures, etc.
     * @param {string} path
     * @returns {string[]}
     */
    function resolveAssetCandidates(path) {
        if (path == null || path === '') return [path];
        if (/^https?:\/\//i.test(path)) return [path];
        const p = String(path);
        if (!p.startsWith('/')) return [p];

        const out = [];
        const add = (u) => {
            if (!u) return;
            if (!out.includes(u)) out.push(u);
        };

        const root = getAppRoot();
        add(root ? root + p : p);

        // Common Pages/static hosting fallback where assets remain under /resources/textures.
        if (p.startsWith('/textures/')) {
            const rel = p.slice('/textures/'.length);
            add(root ? root + '/resources/textures/' + rel : '/resources/textures/' + rel);
            add('../textures/' + rel);
            add('../../textures/' + rel);
            add('./textures/' + rel);
        }

        return out;
    }

    global.getAppRoot = getAppRoot;
    global.resolveAssetUrl = resolveAssetUrl;
    global.resolveAssetCandidates = resolveAssetCandidates;
}(typeof window !== 'undefined' ? window : this));
