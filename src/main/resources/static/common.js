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