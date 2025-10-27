# Руководство для Junior разработчиков

## 📚 Структура проекта

```
dashboard/
├── src/main/java/com/dashboard/
│   ├── ServerDashboardApplication.java  # Главный класс - точка входа
│   ├── config/                          # Настройки приложения
│   │   ├── WebClientConfig.java         # Настройка HTTP клиента
│   │   └── KubernetesConfig.java        # Настройка Kubernetes
│   ├── model/                           # Модели данных (таблицы БД)
│   │   ├── Server.java                  # Сервер (главная сущность)
│   │   ├── ServerType.java              # Типы серверов (enum)
│   │   ├── ServerStatus.java            # Статусы серверов (enum)
│   │   └── PodInfo.java                 # Информация о Kubernetes поде
│   ├── dto/                             # Data Transfer Objects (для API)
│   │   └── ServerDto.java               # DTO для передачи данных о сервере
│   ├── repository/                      # Работа с базой данных
│   │   └── ServerRepository.java        # Репозиторий для серверов
│   ├── service/                         # Бизнес-логика
│   │   ├── ServerMonitorService.java    # Проверка доступности серверов
│   │   ├── KubernetesService.java       # Работа с Kubernetes
│   │   └── CsvExportService.java        # Экспорт в CSV
│   └── controller/                      # API endpoints (REST контроллеры)
│       ├── HomeController.java          # Главная страница API
│       ├── ServerController.java        # API для работы с серверами
│       ├── VersionController.java       # API для Kubernetes подов
│       └── DashboardController.java     # SSR страницы (Thymeleaf)
└── src/main/resources/
    ├── application.properties           # Основные настройки
    ├── static/                          # HTML, CSS, JS файлы
    └── templates/                       # Thymeleaf шаблоны
```

## 🔑 Ключевые концепции

### Spring Boot Аннотации

- `@SpringBootApplication` - главный класс приложения
- `@RestController` - класс с REST API endpoints
- `@Service` - класс с бизнес-логикой
- `@Repository` - класс для работы с БД
- `@Configuration` - класс с настройками
- `@Entity` - класс представляет таблицу в БД
- `@Autowired` - автоматическое внедрение зависимостей

### HTTP Методы (REST API)

- `@GetMapping` - получить данные (SELECT)
- `@PostMapping` - создать данные (INSERT)
- `@PutMapping` - обновить данные (UPDATE)
- `@DeleteMapping` - удалить данные (DELETE)

### JPA Аннотации (работа с БД)

- `@Id` - первичный ключ
- `@GeneratedValue` - автогенерация значения
- `@Column` - настройка колонки БД
- `@Table` - имя таблицы
- `@PrePersist` - вызывается перед сохранением
- `@PreUpdate` - вызывается перед обновлением

## 📝 Основные классы с комментариями

### ServerDashboardApplication.java
```java
// Главный класс - запускает все приложение
@SpringBootApplication  // Автоконфигурация Spring
@EnableScheduling       // Включает задачи по расписанию
public class ServerDashboardApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerDashboardApplication.class, args);
    }
}
```

### Server.java (модель)
```java
@Entity  // Это таблица в БД
@Table(name = "servers")
public class Server {
    @Id  // Первичный ключ
    @GeneratedValue  // Автоинкремент
    private Long id;
    
    @NotBlank  // Обязательное поле
    private String name;  // Название сервера
    
    private String url;  // Адрес для проверки
    
    @Enumerated(EnumType.STRING)
    private ServerType type;  // Тип сервера
    
    private ServerStatus status;  // ONLINE/OFFLINE
    
    private LocalDateTime lastChecked;  // Время проверки
}
```

### ServerRepository.java
```java
// Репозиторий - для работы с БД
// Spring автоматически создает все методы!
public interface ServerRepository extends JpaRepository<Server, Long> {
    // findAll() - получить все серверы
    // findById() - найти по ID
    // save() - сохранить/обновить
    // delete() - удалить
    
    // Можно добавить свои методы:
    List<Server> findByStatus(ServerStatus status);
}
```

### ServerController.java
```java
@RestController  // REST API контроллер
@RequestMapping("/api/servers")  // Базовый путь
public class ServerController {
    
    @Autowired  // Внедрение зависимости
    private ServerRepository repository;
    
    @GetMapping  // GET /api/servers
    public List<Server> getAllServers() {
        return repository.findAll();
    }
    
    @PostMapping  // POST /api/servers
    public Server createServer(@RequestBody Server server) {
        return repository.save(server);
    }
    
    @DeleteMapping("/{id}")  // DELETE /api/servers/1
    public void deleteServer(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
```

### ServerMonitorService.java
```java
@Service  // Бизнес-логика
public class ServerMonitorService {
    
    @Autowired
    private ServerRepository repository;
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
    // Задача выполняется каждые 30 секунд
    @Scheduled(fixedRate = 30000)
    public void checkAllServers() {
        List<Server> servers = repository.findAll();
        servers.forEach(this::checkServer);
    }
    
    // Проверка одного сервера
    private void checkServer(Server server) {
        try {
            // Делаем HTTP запрос
            webClient.get()
                .uri(server.getUrl())
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(5));
            
            // Если успешно - сервер онлайн
            server.setStatus(ServerStatus.ONLINE);
        } catch (Exception e) {
            // Если ошибка - сервер оффлайн
            server.setStatus(ServerStatus.OFFLINE);
        }
        
        server.setLastChecked(LocalDateTime.now());
        repository.save(server);
    }
}
```

## 🔄 Как работает приложение

1. **Запуск**: `ServerDashboardApplication.main()` запускает Spring Boot
2. **Инициализация**: Spring находит все компоненты (@Service, @Controller и т.д.)
3. **База данных**: JPA создает таблицы по моделям (@Entity)
4. **Расписание**: `@Scheduled` методы начинают работать
5. **API**: Контроллеры (@RestController) начинают принимать HTTP запросы
6. **Мониторинг**: Каждые 30 сек проверяются все серверы
7. **UI**: Static файлы (HTML/JS) отображают данные из API

## 📊 Поток данных

```
Browser (JS) 
  ↓ HTTP GET /api/servers
Controller (@RestController)
  ↓ вызывает
Service (@Service) - бизнес-логика
  ↓ использует
Repository (@Repository) - работа с БД
  ↓ запрос к
Database (PostgreSQL)
  ↑ возврат данных
Browser (отображение)
```

## 🛠️ Полезные команды

```bash
# Запуск приложения
mvn spring-boot:run

# Компиляция
mvn clean compile

# Тесты
mvn test

# Сборка JAR файла
mvn clean package
```

## 🌐 API Endpoints

- `GET /api` - информация об API
- `GET /api/servers` - список серверов
- `POST /api/servers` - добавить сервер
- `DELETE /api/servers/{id}` - удалить сервер
- `GET /api/servers/export/csv` - экспорт в CSV
- `GET /api/version/pods` - список Kubernetes подов
- `GET /api/version/export/csv` - экспорт подов в CSV

## 💡 Советы для Junior

1. **Читай аннотации** - они говорят Spring что делать
2. **Смотри на структуру** - каждый слой имеет свою роль
3. **Используй автодополнение** в IDE - оно покажет доступные методы
4. **Логи** - смотри в консоль, там много полезной информации
5. **Postman** - используй для тестирования API
6. **H2 Console** - можно посмотреть БД через браузер

## 🐛 Отладка

- Добавь `System.out.println()` или `logger.info()` для вывода в консоль
- Используй дебаггер в IDE (точки останова)
- Проверяй логи Spring Boot при запуске
- Смотри HTTP статус коды (200 = OK, 404 = Not Found, 500 = Server Error)

## 📚 Что почитать

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [JPA Tutorial](https://spring.io/guides/gs/accessing-data-jpa/)
- [REST API Best Practices](https://restfulapi.net/)


