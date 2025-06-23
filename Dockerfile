# ---------- Étape 1 : build Maven ----------
FROM maven:3.9.4-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY pom.xml .
COPY src src
RUN mvn -T1C -DskipTests clean package
    
# ---------- Étape 2 : image d’exécution ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]