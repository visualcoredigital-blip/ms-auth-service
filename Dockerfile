# ETAPA 1: Compilación (Maven construye el JAR)
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
# Copiamos archivos de configuración de Maven y el código fuente
COPY pom.xml .
COPY src ./src
# Generamos el archivo JAR (esto crea la carpeta target dentro de Docker)
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (Imagen ligera para correr la app)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# Copiamos el JAR generado en la etapa de build y lo renombramos a auth-service.jar
COPY --from=build /app/target/*.jar auth-service.jar

# El puerto que configuramos antes (8001 para Render)
EXPOSE 8001

# Ejecutamos la aplicación
ENTRYPOINT ["java", "-jar", "auth-service.jar"]