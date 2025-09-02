# Step 1: Use Maven with JDK 17 to build the app
FROM maven:3.9.9-eclipse-temurin-17 AS builder

#docker container main yolu bu
WORKDIR /app

COPY pom.xml .

#soldaki benim makine sagdaki containerdeki app/src oalcak diyo yane
COPY src ./src

RUN mvn clean package -DskipTests

# Step 2: Use lightweight JRE 17 for running the app
FROM eclipse-temurin:17.0.15_6-jre

WORKDIR /app

#ilk asamadaki builder ile olusan jar dosyasını buraya kopyaladik
COPY --from=builder /app/target/back-end-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

#docker container docker profili ile calissin
ENV SPRING_PROFILES_ACTIVE=docker

#container calismaya basladigi gibi calisacak komutlar
ENTRYPOINT ["java", "-jar", "app.jar"]