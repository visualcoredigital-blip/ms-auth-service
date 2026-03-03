# ETAPA 1: Compilación (Build)
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
# Generamos el archivo .jar
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (Runtime)
FROM openjdk:17-jdk-slim
# Copiamos solo el .jar generado en la etapa anterior
COPY --from=build /target/*.jar app.jar
# Exponemos el puerto del microservicio
EXPOSE 8081
# Ejecutamos con JAVA, no con MAVEN
ENTRYPOINT ["java", "-jar", "/app.jar"]