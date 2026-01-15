#!/bin/bash

# Script de démarrage pour le portfolio Frédéric Klein

echo "🚀 Démarrage du portfolio website..."
echo ""

# Configuration Java
export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
export JAVA_HOME="/opt/homebrew/opt/openjdk@21"

# Vérifier que Java est disponible
if ! command -v java &> /dev/null; then
    echo "❌ Erreur: Java 21 n'est pas installé ou pas dans le PATH"
    echo "   Installez avec: brew install openjdk@21"
    exit 1
fi

# Vérifier la version de Java
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "❌ Erreur: Java 21+ requis (version actuelle: $JAVA_VERSION)"
    exit 1
fi

echo "✅ Java version: $(java -version 2>&1 | head -n 1)"

# Charger les variables d'environnement
if [ -f .env ]; then
    echo "✅ Chargement des variables d'environnement depuis .env"
    source .env
else
    echo "⚠️  Attention: Fichier .env non trouvé"
    echo "   Le chatbot ne fonctionnera pas sans les clés API"
    echo "   Copiez .env.example vers .env et ajoutez vos clés"
fi

echo ""
echo "🌐 Lancement de Quarkus en mode développement..."
echo "   URL: http://localhost:8080"
echo ""
echo "📝 Appuyez sur Ctrl+C pour arrêter"
echo ""

# Lancer Quarkus en mode dev
./mvnw quarkus:dev
