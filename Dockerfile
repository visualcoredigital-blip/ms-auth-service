FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# El COPY se mantiene, pero el volumen lo ignorará al arrancar
COPY target/auth-service-0.0.1-SNAPSHOT.jar app.jar 
EXPOSE 8081
# CAMBIO AQUÍ: Apuntamos a la ruta real dentro del volumen
ENTRYPOINT ["java", "-jar", "target/auth-service-0.0.1-SNAPSHOT.jar"]