package com.dashboard.service;

/**
 * Интерфейс для выполнения команд kubectl
 * Позволяет абстрагироваться от прямого использования ProcessBuilder
 * и упрощает тестирование через моки
 */
public interface KubectlCommandExecutor {
    
    /**
     * Выполняет команду kubectl и возвращает результат
     * 
     * @param args аргументы команды (без kubectl)
     * @return результат выполнения команды (stdout)
     * @throws KubectlException если команда завершилась с ошибкой или таймаутом
     */
    String executeCommand(String... args) throws KubectlException;
    
    /**
     * Проверяет доступность kubectl
     * 
     * @return true если kubectl доступен и работает
     */
    boolean isAvailable();
}

