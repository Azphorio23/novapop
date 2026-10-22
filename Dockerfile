# NovaPop — imagen para desplegar en Render (o cualquier servicio con Docker)

# 1) Compilación con Maven
FROM maven:3.9-eclipse-temurin-21 AS compilacion
WORKDIR /build
COPY backend/pom.xml .
RUN mvn -B -q dependency:go-offline
COPY backend/src ./src
RUN mvn -B -q -DskipTests package

# 2) Imagen final, solo con Java y la aplicación
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=compilacion /build/target/novapop-1.0.0.jar app.jar
# Los scripts SQL se incluyen para el modo local (si no se configuran las credenciales de Supabase)
COPY database ./database
# El plan gratuito de Render tiene 512 MB de memoria
ENV JAVA_OPTS="-Xmx320m -Xss512k -XX:+UseSerialGC -XX:TieredStopAtLevel=1"
EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
