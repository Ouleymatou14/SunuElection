# Exemples d'Utilisation de l'API SunuElection

Ce document fournit des exemples d'utilisation de l'API SunuElection pour les cas d'utilisation courants.

## Authentification

### Inscription d'un nouvel utilisateur

```http
POST /api/auth/register
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "Password123!",
    "fullName": "John Doe",
    "role": "VOTER"
}
```

Réponse :
```json
{
    "message": "Inscription réussie"
}
```

### Connexion

```http
POST /api/auth/login
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "Password123!"
}
```

Réponse :
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
        "id": 1,
        "email": "user@example.com",
        "fullName": "John Doe",
        "role": "VOTER"
    }
}
```

### Réinitialisation du mot de passe

```http
POST /api/auth/reset-password
Content-Type: application/json

{
    "email": "user@example.com"
}
```

Réponse :
```json
{
    "message": "Email de réinitialisation envoyé"
}
```

## Gestion des Votes

### Soumettre un vote

```http
POST /api/vote
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
    "candidateId": 1
}
```

Réponse :
```json
{
    "message": "Vote soumis avec succès"
}
```

### Vérifier l'état du vote

```http
GET /api/vote/status
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Réponse :
```json
{
    "hasVoted": true
}
```

### Obtenir les résultats

```http
GET /api/vote/results
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Réponse :
```json
{
    "totalVotes": 150,
    "candidates": [
        {
            "id": 1,
            "name": "John Smith",
            "votes": 75
        },
        {
            "id": 2,
            "name": "Jane Doe",
            "votes": 75
        }
    ]
}
```

## Administration

### Créer une sauvegarde

```http
POST /api/admin/backup/create
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Réponse :
```json
{
    "fileName": "backup_20240315_123456.zip"
}
```

### Lister les sauvegardes

```http
GET /api/admin/backup/list
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Réponse :
```json
[
    "backup_20240315_123456.zip",
    "backup_20240314_234567.zip"
]
```

### Restaurer une sauvegarde

```http
POST /api/admin/backup/restore/backup_20240315_123456.zip
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Réponse :
```json
{
    "message": "Système restauré avec succès"
}
```

## Gestion des Utilisateurs

### Lister les utilisateurs

```http
GET /api/admin/users
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Réponse :
```json
[
    {
        "id": 1,
        "email": "user@example.com",
        "fullName": "John Doe",
        "role": "VOTER",
        "enabled": true
    },
    {
        "id": 2,
        "email": "admin@example.com",
        "fullName": "Admin User",
        "role": "ADMIN",
        "enabled": true
    }
]
```

### Désactiver un utilisateur

```http
PUT /api/admin/users/1/disable
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Réponse :
```json
{
    "message": "Utilisateur désactivé avec succès"
}
```

## Journal d'Audit

### Consulter les logs

```http
GET /api/audit/logs?username=admin@example.com&start=2024-03-01&end=2024-03-15
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Réponse :
```json
[
    {
        "id": 1,
        "timestamp": "2024-03-15T10:30:00",
        "username": "admin@example.com",
        "action": "CREATE_BACKUP",
        "details": "Création de la sauvegarde backup_20240315_103000.zip"
    },
    {
        "id": 2,
        "timestamp": "2024-03-15T11:00:00",
        "username": "admin@example.com",
        "action": "DISABLE_USER",
        "details": "Désactivation de l'utilisateur user@example.com"
    }
]
```

## Gestion des Erreurs

### Erreur d'authentification

```http
POST /api/auth/login
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "WrongPassword"
}
```

Réponse :
```json
{
    "error": "Identifiants invalides",
    "status": 401
}
```

### Erreur de validation

```http
POST /api/auth/register
Content-Type: application/json

{
    "email": "invalid-email",
    "password": "123"
}
```

Réponse :
```json
{
    "error": "Validation failed",
    "details": [
        {
            "field": "email",
            "message": "doit être une adresse email valide"
        },
        {
            "field": "password",
            "message": "doit contenir au moins 8 caractères"
        }
    ],
    "status": 400
}
```

### Erreur de rate limiting

```http
POST /api/auth/login
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "Password123!"
}
```

Réponse :
```http
HTTP/1.1 429 Too Many Requests
Retry-After: 60
```

```json
{
    "error": "Trop de requêtes",
    "message": "Veuillez réessayer dans 60 secondes",
    "status": 429
}
``` 