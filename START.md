# Portfolio Website - Guide de démarrage rapide

## Prérequis
- Java 21
- Maven 3.9+

## Configuration

1. **Configurer les clés API** dans le fichier `.env` :
```bash
# Anthropic Claude API Key
ANTHROPIC_API_KEY=your-key-here

# Voyage AI API Key (pour les embeddings)
VOYAGE_API_KEY=your-key-here
```

## Lancer l'application

### Mode développement (avec hot-reload)
```bash
export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
export JAVA_HOME="/opt/homebrew/opt/openjdk@21"
source .env
./mvnw quarkus:dev
```

L'application sera accessible sur : **http://localhost:8080**

### Build et Run en production
```bash
# Build
./mvnw clean package

# Run
java -jar target/quarkus-app/quarkus-run.jar
```

### Avec Docker
```bash
# Build l'image
docker build -t fklein-portfolio .

# Run le container
docker run -p 8080:8080 --env-file .env fklein-portfolio
```

## Structure du projet

```
website_fklein/
├── src/main/
│   ├── java/com/fklein/
│   │   ├── models/          # Modèles de données (ProfileData, ChatMessage)
│   │   ├── resources/       # REST endpoints (ChatbotResource, PortfolioResource)
│   │   └── services/        # Services (RAG, Claude, Embeddings, VectorStore)
│   └── resources/
│       ├── data/
│       │   └── cv.json      # Données de votre CV
│       ├── templates/
│       │   └── index.html   # Template Qute principal
│       └── META-INF/resources/
│           ├── css/style.css    # Styles du site
│           └── js/chatbot.js    # JavaScript du chatbot
├── pom.xml                  # Configuration Maven
└── .env                     # Variables d'environnement (API keys)
```

## Fonctionnalités

- ✅ Site portfolio moderne avec theme sombre
- ✅ Chatbot RAG intelligent (Claude Sonnet 4.5)
- ✅ 13 expériences professionnelles affichées
- ✅ Certification Kubestronaut mise en avant
- ✅ Logos officiels des entreprises
- ✅ Icônes réseaux sociaux (LinkedIn, GitHub, X, Blog)
- ✅ Design responsive et glassmorphism
- ✅ Hot-reload en mode dev

## Modifier le CV

Éditez le fichier `src/main/resources/data/cv.json` pour mettre à jour :
- Informations personnelles
- Expériences professionnelles
- Certifications
- Compétences
- Langues

Le site se mettra à jour automatiquement en mode dev !
