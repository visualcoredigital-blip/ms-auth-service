# Usa una imagen de Maven para construir el proyecto
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
# ESTE ES EL COMANDO CLAVE: usa 'package', NO 'spring-boot:run'
RUN mvn clean package -DskipTests

# Usa una imagen ligera de Java para ejecutarlo
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# El archivo generado suele estar en target/
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
# Ejecuta el JAR directamente
ENTRYPOINT ["java", "-jar", "app.jar"]