FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Gradle wrapper 복사 및 실행 권한 부여
COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

# 소스 복사
COPY build.gradle .
COPY settings.gradle .
COPY src src

# 🔥 반드시 clean 포함해서 빌드
RUN ./gradlew clean build -x test --no-daemon

# -----------------------------------
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
