# ETAPA 1: Compilación
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (Cambia la línea de abajo)
FROM eclipse-temurin:17-jdk-alpine 
COPY --from=build /target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]