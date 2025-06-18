// =========================
// JWT helpers & auth utils
// =========================

// Stockage et récupération du token JWT
function setToken(token) {
    localStorage.setItem('token', token);
}
function getToken() {
    return localStorage.getItem('token');
}
function removeToken() {
    localStorage.removeItem('token');
}

// Décoder le payload d'un JWT (Base64URL)
function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch (e) {
        return {};
    }
}

// Retourne la liste des rôles de l'utilisateur connecté
function getRoles() {
    // Récupère les rôles du JWT quels que soient les formats renvoyés par l'API

    const token = getToken();
    if (!token) return [];
    const payload = parseJwt(token);
        let roles = payload.roles || payload.authorities || payload.scopes || payload.scope || [];
    // Spring peut renvoyer authorities sous forme d'objets [{authority:"ROLE_ADMIN"}]
    if (Array.isArray(roles) && roles.length && typeof roles[0] === 'object') {
        roles = roles.map(o => o.role || o.authority || o.name || '').filter(Boolean);
    }

        if (typeof roles === 'string') roles = roles.split(/[, ]/);
    if (!Array.isArray(roles)) roles = [roles];
    roles = roles.map(r => r.replace(/^ROLE_/i, '').toUpperCase());
    if (!roles.length) {
        try {
            const cached = JSON.parse(localStorage.getItem('rolesJSON') || '[]');
            if (Array.isArray(cached)) roles = cached.map(r=>r.replace(/^ROLE_/i,'').toUpperCase());
        } catch(e) {}
    }
    return roles;
}

function hasRole(role) {
    return getRoles().includes(role.toUpperCase());
}

// Requête API avec gestion du token
async function apiRequest(url, options = {}) {
    const token = getToken();
    options.headers = options.headers || {};
    if (token) {
        options.headers['Authorization'] = 'Bearer ' + token;
    }
    if (!options.headers['Content-Type'] && options.body) {
        options.headers['Content-Type'] = 'application/json';
    }
    const response = await fetch(url, options);
    if (response.status === 401) {
        removeToken();
        window.location.href = '/api/index.html';
        return;
    }
    if (response.status === 204) {
        return {};
    }
    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('application/json')) {
        try {
            return await response.json();
        } catch (e) {
            return {};
        }
    }
    return {};
}

// Redirection selon le rôle
function redirectToDashboard(role) {
    role = (role || '').toUpperCase();
    if (role === 'ADMIN') {
        window.location.href = '/api/admin.html';
    } else if (role === 'SCRUTATEUR') {
        window.location.href = '/api/scrutateur.html';
    } else {
        window.location.href = '/api/dashboard.html';
    }
} 