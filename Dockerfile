####
# This Dockerfile is used to build a container image for the Quarkus application.
# Build the image with: docker build -f Dockerfile -t fklein/portfolio .
# Run the container with: docker run -i --rm -p 8080:8080 --env-file .env fklein/portfolio
####

## Stage 1 : build with maven builder image
FROM registry.access.redhat.com/ubi9/openjdk-21:1.20 AS build
USER root
WORKDIR /app

# Install Maven
RUN microdnf install -y maven && microdnf clean all

# Copy pom.xml and download dependencies
COPY --chown=default:root pom.xml /app/
RUN mvn dependency:go-offline -B

# Copy source code
COPY --chown=default:root src /app/src

# Build application
RUN mvn package -DskipTests -Dquarkus.package.jar.type=uber-jar

## Stage 2 : create the docker final image
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.20

ENV LANGUAGE='en_US:en'

# Copy the application jar
COPY --from=build --chown=185 /app/target/*-runner.jar /deployments/quarkus-run.jar

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "java", "-jar", "/deployments/quarkus-run.jar" ]
