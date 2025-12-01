# Étape de build
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Installation de Maven
RUN apt-get update && apt-get install -y maven

COPY pom.xml .
COPY src ./src

# Compilation du projet
RUN mvn clean package -DskipTests

# Étape de production
FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=build /app/target/tailor_pulp_website.jar tailor_pulp_website.jar

EXPOSE 4000

ENTRYPOINT ["java", "-jar", "tailor_pulp_website.jar"]
