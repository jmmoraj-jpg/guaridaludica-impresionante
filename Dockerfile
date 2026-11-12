# Etapa de compilación (Cambiado a 21-jdk)
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .

# Dar permisos de ejecución al Maven Wrapper
RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

# Etapa de ejecución (Cambiado a 21-jre)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]