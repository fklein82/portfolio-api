# 🚀 Déploiement Railway - Guide Pas à Pas

## Étape 1: Créer un compte Railway

1. Allez sur **https://railway.app**
2. Cliquez sur **"Start a New Project"** ou **"Login"**
3. **Sign up with GitHub** (recommandé pour auto-deploy)
4. Autorisez Railway à accéder à vos repos GitHub

## Étape 2: Créer un nouveau projet

1. Sur le dashboard Railway, cliquez **"New Project"**
2. Sélectionnez **"Deploy from GitHub repo"**
3. Cherchez et sélectionnez **`fklein82/portfolio-api`**
4. Railway va automatiquement détecter que c'est un projet Java/Maven

## Étape 3: Configurer les variables d'environnement

1. Dans votre projet Railway, cliquez sur l'onglet **"Variables"**
2. Ajoutez la variable suivante:

   ```
   Variable Name: OPENAI_API_KEY
   Value: sk-proj-... (votre clé OpenAI)
   ```

3. Cliquez **"Add"** puis **"Deploy"**

## Étape 4: Attendre le déploiement

Le build prendra environ **3-5 minutes**. Vous verrez:

```
✓ Building...
  - Installing Java 21
  - Running ./mvnw clean package
  - Building Quarkus application
✓ Build successful
✓ Deploying...
✓ Deployment successful!
```

## Étape 5: Obtenir votre URL

1. Une fois déployé, allez dans **"Settings"** → **"Domains"**
2. Railway génère automatiquement une URL comme:
   ```
   https://portfolio-api-production.up.railway.app
   ```
3. **Copiez cette URL** - vous en aurez besoin pour le frontend!

## Étape 6: Tester votre API

Testez le health check:

```bash
curl https://VOTRE-URL.up.railway.app/api/chat/health
```

Devrait retourner: `Chatbot is ready!`

Testez le chatbot:

```bash
curl -X POST https://VOTRE-URL.up.railway.app/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"message": "Who is Frédéric Klein?", "role": "user"}'
```

## Étape 7: Mettre à jour le frontend

1. Allez dans le repo frontend:
   ```bash
   cd /Users/fklein/fklein82.github.io
   ```

2. Éditez `js/chatbot.js` ligne 4:
   ```javascript
   const API_URL = 'https://VOTRE-URL.up.railway.app';
   ```

3. Commitez et pushez:
   ```bash
   git add js/chatbot.js
   git commit -m "Update API URL to Railway endpoint"
   git push
   ```

4. Attendez 1-2 minutes que GitHub Pages redéploie

## Étape 8: Tester le site complet

1. Ouvrez https://fklein82.github.io
2. Testez le chatbot avec des questions:
   - "Who is Frédéric Klein?" (anglais)
   - "Quelles sont tes certifications Kubernetes?" (français)

## ✅ Checklist de déploiement

- [ ] Compte Railway créé et connecté à GitHub
- [ ] Projet créé depuis `portfolio-api` repo
- [ ] Variable `OPENAI_API_KEY` configurée
- [ ] Build réussi (logs verts)
- [ ] URL Railway obtenue
- [ ] Health check fonctionne
- [ ] Frontend mis à jour avec l'URL Railway
- [ ] Chatbot fonctionne sur GitHub Pages

## 🔧 Dépannage

### Build échoue

**Erreur:** "Maven build failed"
- Vérifiez les logs: possible problème de dépendances
- Railway utilise Java 21 par défaut (correct)

### API démarre mais timeout

**Erreur:** "Application failed health check"
- Vérifiez que `OPENAI_API_KEY` est bien configurée
- Regardez les logs: `railway logs`

### CORS errors sur le frontend

**Erreur:** "blocked by CORS policy"
- Vérifiez que `application.properties` autorise `https://fklein82.github.io`
- Ligne 16: `%prod.quarkus.http.cors.origins=https://fklein82.github.io`

### OpenAI rate limit

**Erreur:** "You exceeded your current quota"
- Ajoutez des crédits sur https://platform.openai.com/account/billing
- Ou attendez que le quota se renouvelle

## 💰 Coûts

**Railway Free Tier:**
- $5 de crédit gratuit par mois
- Suffisant pour ~500-1000 requêtes chatbot/mois
- Si dépassé: ~$5-10/mois selon usage

**OpenAI API:**
- Embeddings: ~$0.01/mois
- GPT-4: ~$0.03/requête
- Total: ~$5-15/mois selon trafic

## 🔄 Auto-Deploy

Railway est configuré pour **auto-deploy**:
- Chaque `git push` sur `main` → nouveau build automatique
- Pratique pour les mises à jour du CV ou corrections

## 📊 Monitoring

Voir les logs en temps réel:

```bash
# Installer Railway CLI
npm i -g @railway/cli

# Login
railway login

# Voir les logs
railway logs
```

Ou sur le dashboard Railway: **Deployments** → **View Logs**

## 🎉 C'est fait!

Votre architecture complète est maintenant déployée:

```
GitHub Pages (Frontend)
        ↓
   Railway (API)
        ↓
  OpenAI (GPT-4)
```

Profitez de votre portfolio avec chatbot IA! 🚀
