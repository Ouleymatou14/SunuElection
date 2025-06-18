# SunuElection – Rapport Final des Exigences & Assurance Qualité

_Date : 18-06-2025_

---

## 1. Tableau MoSCoW / INVEST

| Type d’utilisateur | Fonction | Importance | User Story | Implémenté |
|--------------------|----------|------------|------------|------------|
| Électeur | Authentification | Must | En tant qu’électeur, je veux pouvoir m’authentifier pour accéder à l’espace de vote. | ✅ Oui |
| Électeur | Bulletin de vote | Must | … je veux pouvoir voter de façon confidentielle via l’interface web. | ✅ Oui |
| Électeur | Base des votes | Must | … je veux être sûr que mon vote est enregistré et pris en compte. | ✅ Oui |
| Admin | Import liste électorale CSV | Must | … je veux pouvoir charger la liste des électeurs avant le scrutin. | ⏳ En attente (Sprint Beta) |
| Admin | Résultats agrégés | Must | … je veux pouvoir voir les résultats finaux sans lien avec les identités. | ✅ Oui |
| Scrutateur | Consultation des journaux | Should | … je veux pouvoir consulter les journaux pour vérifier l’intégrité du vote. | ✅ Oui |
| Électeur | Notification de vote | Could | … je veux recevoir une confirmation que mon vote a bien été pris en compte. | ⏳ En attente |

---

## 2. Satisfaction des Exigences de Sécurité

### 2.1 Autorisation
* **Modèle :** Discretionary Access Control (DAC) implémenté sous forme de _Role-Based Access Control_.
* **Rôles :** `ADMIN`, `USER`, `SCRUTATEUR` persistés en base.
* **Enforcement :**
  * Annotations `@PreAuthorize`/`@Secured` sur les contrôleurs, expressions SpEL.
  * Filtre JWT extrait le rôle et renseigne `SecurityContext`.
* **Mise à jour des politiques :** modifications des rôles/permissions via l’UI admin ➜ persistées en BD et prises en compte instantanément (pas besoin de redémarrage).

### 2.2 Authentification
* **Utilisateurs :** login + mot de passe, stockage haché Bcrypt (10 rounds).
* **Programmes :** échange JWT (HS512). Refresh-token 7 jours.
* **Flux :** `/api/auth/login` ➜ retour `access_token` (15 min) + `refresh_token`.

### 2.3 Audit / Responsabilité
* Table `audit_log` : `id`, `user_id`, `action`, `details`, `timestamp`, `cumulative_hash`, `signature` (RSA-PSS).
* Chaque enregistrement enchaîne son hash SHA-256 avec le hash cumulatif précédent ➜ détection anti-réécriture.
* API `/api/audit/verify` vérifie la chaîne et la signature.

### 2.4 Confidentialité
* Votes chiffrés côté client avec la clé publique RSA (2048 bits) exposée via `/crypto/public-key`.
* Transmission HTTPS recommandée (terminée par serveur proxy/Nginx).
* Les journaux n’enregistrent aucun PII relatif aux bulletins.

### 2.5 Intégrité
* Contraintes JPA + clés étrangères.
* Hash cumulatif + signature des audits.
* Vérification de signature du vote (service `CryptoService.sign/verify`).

### 2.6 Gestion des Secrets
| Secret | Création / Rotation | Stockage | Effacement |
|--------|--------------------|----------|------------|
| Mot de passe utilisateur | Choisi par l’utilisateur / réinitialisation admin | Bcrypt en BD | Réécriture + suppression dans logs |
| Clé secrète JWT | Variable d’environnement `JWT_SECRET` | Fichier `application.properties` _externalisé_ | Redémarrage + suppression fichier |
| Paire RSA serveur | Générée au déploiement (keytool) | Keystore `.p12` chiffré par mot de passe env. | Rotation annuelle, ancien keystore supprimé |

Algorithmes utilisés : Bcrypt, HS512 (JWT), RSA-PSS 2048, SHA-256.

---

## 3. Assurance & Tests

| Niveau | Outils | Métriques |
|--------|--------|-----------|
| Unitaire | JUnit 5, Mockito | 29 tests – couverture lignes : 78 % |
| Intégration | SpringBootTest + H2 | 12 IT (REST ↔ Service ↔ Repo) |
| Sécurité | Spring Security Test, OWASP ZAP (passive) | 0 alerte haute |
| Statique | SpotBugs 4.7.3 + FindSecBugs 1.12 | 0 High, 8 Medium (corrigés), seuil CI = High |


---

## 4. Statut des Tâches (18-06-2025)

| ID | Tâche | Volet | Statut |
|----|-------|-------|--------|
| A-01 | Journal d’audit + UI | Audit | ✅ |
| A-02 | Intégrité log (hash + sig) | Audit | ✅ |
| A-03 | RSA clé publique API | Audit | ✅ |
| A-04 | Vote chiffré client | Audit | ✅ |
| A-05 | Auth JWT & refresh | AuthN | ✅ |
| A-06 | Routes protégées rôles | AuthZ | ✅ |
| A-07 | Page scrutateur / admin résultats | Audit/AuthZ | ✅ |
| A-08 | Import CSV électeurs | AuthZ | ⏳ Spécifié |
| A-09 | Notification de vote email | UX | ⏳ Spécifié |

---

## 5. Annexes
* Rapports SpotBugs : `target/spotbugs.html`
* Couverture JaCoCo : `target/site/jacoco/index.html`
* Documentation Swagger : `http://localhost:8080/swagger-ui.html`
