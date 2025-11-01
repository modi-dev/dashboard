package com.dashboard;

import com.dashboard.config.KubernetesConfig;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для ServerDashboardApplication
 */
@SpringBootTest
@ActiveProfiles("test")
class ServerDashboardApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired(required = false)
    private KubernetesConfig kubernetesConfig;

    @Test
    void contextLoads() {
        // This test will pass if the Spring context loads successfully
        assertNotNull(applicationContext);
    }
    
    @Test
    void testApplicationContextContainsMainClass() {
        // Проверяем, что главный класс загружен в контекст
        assertNotNull(applicationContext);
        // Spring Boot не создает bean для главного класса, но контекст должен быть загружен
        String contextId = applicationContext.getId();
        assertNotNull(contextId);
        assertFalse(contextId.isEmpty());
    }
    
    @Test
    void testSchedulingEnabled() {
        // Проверяем, что scheduling включен (@EnableScheduling)
        ScheduledAnnotationBeanPostProcessor scheduler = 
            applicationContext.getBean(ScheduledAnnotationBeanPostProcessor.class);
        assertNotNull(scheduler, "Scheduling should be enabled");
    }
    
    @Test
    void testKubernetesConfigLoaded() {
        // Проверяем, что KubernetesConfig загружен (@EnableConfigurationProperties)
        assertNotNull(kubernetesConfig, "KubernetesConfig should be loaded");
    }
    
    @Test
    void testKubernetesConfigHasDefaultValues() {
        // Проверяем, что KubernetesConfig загружен и имеет значения
        // Значения могут быть переопределены в application-test.yml
        assertNotNull(kubernetesConfig);
        assertNotNull(kubernetesConfig.getNamespace());
        assertNotNull(kubernetesConfig.getKubectlPath());
        // Проверяем, что значения установлены (не null)
        assertFalse(kubernetesConfig.getNamespace().isEmpty());
        assertFalse(kubernetesConfig.getKubectlPath().isEmpty());
    }
    
    @Test
    void testMainClassAnnotations() {
        // Проверяем, что класс имеет правильные аннотации через рефлексию
        Class<?> clazz = ServerDashboardApplication.class;
        
        assertTrue(clazz.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class),
                   "Should have @SpringBootApplication annotation");
        assertTrue(clazz.isAnnotationPresent(org.springframework.scheduling.annotation.EnableScheduling.class),
                   "Should have @EnableScheduling annotation");
        assertTrue(clazz.isAnnotationPresent(org.springframework.boot.context.properties.EnableConfigurationProperties.class),
                   "Should have @EnableConfigurationProperties annotation");
    }
    
    @Test
    void testMainMethodExists() {
        // Проверяем, что метод main существует
        try {
            java.lang.reflect.Method mainMethod = 
                ServerDashboardApplication.class.getMethod("main", String[].class);
            assertNotNull(mainMethod);
            assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()));
            assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()));
            // Проверяем, что метод возвращает void
            assertEquals(void.class, mainMethod.getReturnType());
        } catch (NoSuchMethodException e) {
            fail("Main method should exist");
        }
    }
    
    @Test
    void testMainMethodParameters() {
        // Проверяем параметры метода main
        try {
            java.lang.reflect.Method mainMethod = 
                ServerDashboardApplication.class.getMethod("main", String[].class);
            java.lang.reflect.Parameter[] parameters = mainMethod.getParameters();
            assertEquals(1, parameters.length);
            assertEquals(String[].class, parameters[0].getType());
        } catch (NoSuchMethodException e) {
            fail("Main method should exist");
        }
    }
    
    @Test
    void testEnableConfigurationPropertiesAnnotationValue() {
        // Проверяем, что @EnableConfigurationProperties имеет правильный параметр
        Class<?> clazz = ServerDashboardApplication.class;
        org.springframework.boot.context.properties.EnableConfigurationProperties annotation = 
            clazz.getAnnotation(org.springframework.boot.context.properties.EnableConfigurationProperties.class);
        assertNotNull(annotation);
        
        Class<?>[] value = annotation.value();
        assertNotNull(value);
        assertEquals(1, value.length);
        assertEquals(KubernetesConfig.class, value[0]);
    }
    
    @Test
    void testClassIsPublic() {
        // Проверяем, что класс публичный
        assertTrue(java.lang.reflect.Modifier.isPublic(ServerDashboardApplication.class.getModifiers()));
    }
    
    @Test
    void testClassHasNoAbstractModifier() {
        // Проверяем, что класс не абстрактный
        assertFalse(java.lang.reflect.Modifier.isAbstract(ServerDashboardApplication.class.getModifiers()));
    }
    
    @Test
    void testMainMethodCallsSpringApplicationRun() {
        // Мокируем статический метод SpringApplication.run()
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            // Создаем мок для ConfigurableApplicationContext
            ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
            
            // Настраиваем мок, чтобы SpringApplication.run() возвращал мок контекста
            mockedSpringApplication.when(() -> SpringApplication.run(
                eq(ServerDashboardApplication.class), 
                any(String[].class)
            )).thenReturn(mockContext);
            
            // Вызываем метод main с тестовыми аргументами
            String[] args = {"--test.arg=value"};
            ServerDashboardApplication.main(args);
            
            // Проверяем, что SpringApplication.run() был вызван с правильными параметрами
            mockedSpringApplication.verify(() -> SpringApplication.run(
                eq(ServerDashboardApplication.class),
                eq(args)
            ), times(1));
        }
    }
    
    @Test
    void testMainMethodCallsSpringApplicationRunWithEmptyArgs() {
        // Тестируем вызов main с пустым массивом аргументов
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
            
            mockedSpringApplication.when(() -> SpringApplication.run(
                eq(ServerDashboardApplication.class),
                any(String[].class)
            )).thenReturn(mockContext);
            
            String[] emptyArgs = {};
            ServerDashboardApplication.main(emptyArgs);
            
            // Проверяем, что метод был вызван с пустым массивом
            mockedSpringApplication.verify(() -> SpringApplication.run(
                eq(ServerDashboardApplication.class),
                eq(emptyArgs)
            ), times(1));
        }
    }
    
    @Test
    void testMainMethodCallsSpringApplicationRunWithMultipleArgs() {
        // Тестируем вызов main с несколькими аргументами командной строки
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
            
            mockedSpringApplication.when(() -> SpringApplication.run(
                eq(ServerDashboardApplication.class),
                any(String[].class)
            )).thenReturn(mockContext);
            
            // Проверяем с несколькими аргументами, как это может быть в реальном запуске
            String[] multipleArgs = {
                "--server.port=8080",
                "--spring.profiles.active=dev",
                "--logging.level.com.dashboard=DEBUG"
            };
            
            ServerDashboardApplication.main(multipleArgs);
            
            // Проверяем, что метод был вызван с правильными аргументами
            mockedSpringApplication.verify(() -> SpringApplication.run(
                eq(ServerDashboardApplication.class),
                eq(multipleArgs)
            ), times(1));
        }
    }
    
    @Test
    void testMainMethodHandlesSpringApplicationRunException() {
        // Тестируем обработку исключения при вызове SpringApplication.run()
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            // Настраиваем мок, чтобы выбросить исключение
            RuntimeException testException = new RuntimeException("Test exception");
            mockedSpringApplication.when(() -> SpringApplication.run(
                eq(ServerDashboardApplication.class),
                any(String[].class)
            )).thenThrow(testException);
            
            // Проверяем, что исключение пробрасывается
            assertThrows(RuntimeException.class, () -> {
                ServerDashboardApplication.main(new String[]{});
            });
            
            // Проверяем, что метод был вызван
            mockedSpringApplication.verify(() -> SpringApplication.run(
                eq(ServerDashboardApplication.class),
                any(String[].class)
            ), times(1));
        }
    }
}

