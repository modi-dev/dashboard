# Используем официальный OpenJDK образ
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем Maven wrapper и pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Копируем исходный код
COPY src src

# Собираем приложение
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# Создаем пользователя для безопасности
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Открываем порт
EXPOSE 3001

# Устанавливаем переменные окружения
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Запускаем приложение
CMD ["java", "-jar", "target/server-dashboard-1.0.0.jar"]

