# SunuElection - Système de Vote Électronique Sécurisé

## Description
SunuElection est un système de vote électronique sécurisé conçu pour les environnements institutionnels et académiques. Il garantit un processus de vote anonyme, traçable et non révocable.

## Fonctionnalités Principales
- 🔐 Authentification & rafraîchissement JWT
- 🛡️ Autorisation basée sur rôles : **ADMIN**, **USER**, **SCRUTATEUR**
- 🗳️ Vote électronique chiffré côté client (RSA) et signé serveur
- 📊 Résultats agrégés en temps réel (anonymes)
- 📝 Journal d'audit horodaté, signé (+ export CSV)
- 🧾 Preuve d’intégrité des logs (SHA-256)
- 🔄 Sauvegarde & restauration de la base (backups)
- 👥 Gestion des utilisateurs + import CSV de la liste électorale
- 🚦 Rate-limiting anti-brute-force (Bucket4j)
- 📱 Interface responsive (Bootstrap 5)
- 🔐 Authentification sécurisée avec JWT
- 🗳️ Vote électronique chiffré
- 📊 Comptage automatisé des votes
- 📝 Journal d'audit complet
- 🔄 Système de sauvegarde et restauration
- 👥 Gestion des utilisateurs et des rôles
- 📱 Interface responsive

## Technologies Utilisées
- **Backend**: Java 11, Spring Boot 2.7.x
- **Frontend**: HTML5, CSS3, JavaScript
- **Base de données**: MySQL 8.0
- **Sécurité**: Spring Security, JWT, Bcrypt
- **Documentation**: OpenAPI/Swagger

## Prérequis
- Java 11 ou supérieur
- MySQL 8.0
- Maven 3.6+
- Node.js 14+ (pour le frontend)

## Installation

1. Cloner le repository :
```bash
git clone https://github.com/Ouleymatou14/SunuElection.git
cd SunuElection
```

2. Configurer la base de données :
```sql
CREATE DATABASE sunelection;
```

3. Configurer les variables d'environnement :
```properties
# Copier application.properties.example vers application.properties
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

4. Compiler et démarrer l'application :
```bash
mvn clean install
mvn spring-boot:run
```

## Configuration

### Base de données
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sunelection
spring.datasource.username=root
spring.datasource.password=root
```

### Sécurité
```properties
jwt.secret=votreCleSecreteJWT
jwt.expiration=86400000
```

### Rate Limiting
```properties
rate-limit.auth.requests=5
rate-limit.auth.duration=60
rate-limit.api.requests=100
rate-limit.api.duration=60
```

## Utilisation

### API REST
L'API est documentée avec Swagger UI, accessible à l'adresse :
```
http://localhost:8080/swagger-ui.html
```

### Endpoints Principaux

#### Authentification
```http
POST /api/auth/login
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "password"
}
```

#### Vote
```http
POST /api/vote
Authorization: Bearer <token>
Content-Type: application/json

{
    "candidateId": 1
}
```

#### Administration
```http
GET /api/admin/users
Authorization: Bearer <token>
```

### Interface d'Administration
Accédez à l'interface d'administration à l'adresse :
```
http://localhost:8080/admin.html
```

## Sécurité

### Authentification
- JWT pour l'authentification
- Sessions sans état
- Protection CSRF
- Rate limiting

### Chiffrement
- Votes chiffrés avec RSA/AES
- Mots de passe hachés avec Bcrypt
- HTTPS obligatoire

### Audit
- Journalisation de toutes les actions
- Horodatage des événements
- Signatures numériques

## Développement

### Structure du Projet
```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── sunelection/
│   │           ├── config/
│   │           ├── controller/
│   │           ├── model/
│   │           ├── repository/
│   │           ├── security/
│   │           └── service/
│   └── resources/
│       ├── static/
│       └── application.properties
└── test/
    └── java/
        └── com/
            └── sunelection/
```

### Tests
```bash
# Exécuter tous les tests
mvn test

# Exécuter les tests avec couverture
mvn verify
```

## Contribution
1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## Licence
Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

## Contact
Ouleymatou Sadiya CISSE - ouleymatousadiyacisse@esp.sn

## Remerciements
- Spring Boot Team
- Bootstrap
- Font Awesome 