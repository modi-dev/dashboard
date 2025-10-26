// Импортируем необходимые библиотеки и компоненты
import React, { useState, useEffect } from 'react'; // React хуки для состояния и эффектов
import axios from 'axios'; // HTTP клиент для запросов к API
import AddServerSidebar from './components/AddServerSidebar'; // Компонент бокового меню
import ServerCard from './components/ServerCard'; // Компонент карточки сервера
import PodInfo from './components/PodInfo'; // Компонент информации о Kubernetes подах
import './App.css'; // Стили основного компонента

/**
 * Главный компонент приложения Server Dashboard
 * Отвечает за отображение списка серверов и управление их состоянием
 */
function App() {
  // Состояние для хранения списка серверов
  const [servers, setServers] = useState([]);
  
  // Состояние для индикации процесса загрузки
  const [loading, setLoading] = useState(false);
  
  // Состояние для хранения ошибок
  const [error, setError] = useState(null);
  
  // Состояние для управления видимостью бокового меню
  const [sidebarOpen, setSidebarOpen] = useState(false);
  
  // Состояние для управления отображением информации о подах
  const [showPodInfo, setShowPodInfo] = useState(false);

  /**
   * useEffect - хук для выполнения побочных эффектов
   * Выполняется при монтировании компонента (пустой массив зависимостей [])
   */
  useEffect(() => {
    // Загружаем серверы при первом запуске
    fetchServers();
    
    // Настраиваем автоматическое обновление каждые 30 секунд
    const interval = setInterval(fetchServers, 30000);
    
    // Функция очистки - выполняется при размонтировании компонента
    // Останавливаем интервал, чтобы избежать утечек памяти
    return () => clearInterval(interval);
  }, []); // Пустой массив означает, что эффект выполнится только один раз

  /**
   * Асинхронная функция для загрузки списка серверов с API
   * Обрабатывает состояния загрузки, успеха и ошибок
   */
  const fetchServers = async () => {
    // Устанавливаем состояние загрузки
    setLoading(true);
    // Очищаем предыдущие ошибки
    setError(null);
    
    try {
      // Выполняем GET запрос к API
      const response = await axios.get('/api/servers');
      
      // Проверяем успешность ответа
      if (response.data.success && response.data.data) {
        // Обновляем состояние с полученными данными
        setServers(response.data.data);
      } else {
        // Логируем ошибку и устанавливаем пустой массив
        console.error('API returned unsuccessful response:', response.data);
        setServers([]);
        setError('Ошибка при получении данных с сервера');
      }
    } catch (error) {
      // Обрабатываем ошибки сети или сервера
      console.error('Error fetching servers:', error);
      setServers([]);
      setError('Не удалось подключиться к серверу');
    } finally {
      // В любом случае снимаем флаг загрузки
      setLoading(false);
    }
  };

  /**
   * Обработчик успешного добавления сервера
   * Вызывается из компонента AddServerSidebar после успешного создания
   */
  const handleServerAdded = () => {
    // Перезагружаем список серверов для отображения нового сервера
    fetchServers();
  };

  /**
   * Обработчик принудительной проверки конкретного сервера
   * @param {number} serverId - ID сервера для проверки
   */
  const handleRefreshServer = async (serverId) => {
    try {
      // Отправляем POST запрос для принудительной проверки сервера
      await axios.post(`/api/servers/${serverId}/check`);
      // Перезагружаем список для получения обновленных данных
      fetchServers();
    } catch (error) {
      // Логируем ошибку, но не показываем пользователю
      console.error('Error refreshing server:', error);
    }
  };

  /**
   * Обработчик удаления сервера
   * @param {number} serverId - ID сервера для удаления
   */
  const handleDeleteServer = async (serverId) => {
    try {
      // Отправляем DELETE запрос для удаления сервера
      await axios.delete(`/api/servers/${serverId}`);
      // Перезагружаем список для обновления отображения
      fetchServers();
    } catch (error) {
      // Показываем ошибку пользователю
      console.error('Error deleting server:', error);
      alert('Ошибка при удалении сервера. Попробуйте еще раз.');
    }
  };

  /**
   * Открывает боковое меню для добавления сервера
   */
  const openSidebar = () => {
    setSidebarOpen(true);
  };

  /**
   * Закрывает боковое меню
   */
  const closeSidebar = () => {
    setSidebarOpen(false);
  };

  /**
   * Переключает отображение информации о подах Kubernetes
   */
  const togglePodInfo = () => {
    setShowPodInfo(!showPodInfo);
  };

  // Возвращаем JSX разметку компонента
  return (
    <div className="App">
      {/* Заголовок приложения с кнопками управления */}
      <header className="app-header">
        <div className="header-content">
          {/* Название приложения с логотипом */}
          <h1 className="app-title">
            <img src="/logo-icon.svg" alt="Server Dashboard" className="title-logo" />
            Server Dashboard
          </h1>
          
          {/* Кнопки действий в заголовке */}
          <div className="header-actions">
            {/* Кнопка для открытия бокового меню добавления сервера */}
            <button 
              className="btn btn-primary add-server-btn"
              onClick={openSidebar}
            >
              <span className="btn-icon">+</span>
              Добавить сервер
            </button>
            
            {/* Кнопка для переключения отображения информации о подах */}
            <button 
              className="btn btn-info pod-info-btn"
              onClick={togglePodInfo}
              title={showPodInfo ? 'Скрыть информацию о подах' : 'Показать информацию о подах'}
            >
              <span className="btn-icon">📦</span>
              {showPodInfo ? 'Скрыть поды' : 'Поды K8s'}
            </button>
            
            {/* Кнопка для обновления всех серверов */}
            <button 
              className="btn btn-secondary refresh-btn"
              onClick={fetchServers}
              disabled={loading} // Блокируем кнопку во время загрузки
            >
              <span className="btn-icon">🔄</span>
              {/* Динамический текст в зависимости от состояния загрузки */}
              {loading ? 'Обновление...' : 'Обновить'}
            </button>
          </div>
        </div>
      </header>

      {/* Основной контент приложения */}
      <main className="app-main">
        {/* Условное отображение баннера ошибки */}
        {error && (
          <div className="error-banner">
            <span className="error-icon">⚠️</span>
            {error}
          </div>
        )}

        {/* Отображение информации о подах Kubernetes */}
        {showPodInfo && <PodInfo />}

        {/* Состояние загрузки при первом запуске */}
        {loading && servers.length === 0 && (
          <div className="loading-container">
            <div className="loading-spinner"></div>
            <p>Загрузка серверов...</p>
          </div>
        )}

        {/* Отображение сетки карточек серверов */}
        {!loading && servers.length > 0 && (
          <div className="servers-grid">
            {/* Маппим массив серверов в компоненты ServerCard */}
            {servers.map(server => (
              <ServerCard
                key={server.id} // Уникальный ключ для React
                server={server} // Передаем данные сервера
                onRefresh={handleRefreshServer} // Функция для обновления
                onDelete={handleDeleteServer} // Функция для удаления
              />
            ))}
          </div>
        )}

        {/* Пустое состояние - когда серверов нет */}
        {!loading && servers.length === 0 && !error && (
          <div className="empty-state">
            <div className="empty-icon">🖥️</div>
            <h3>Серверы не добавлены</h3>
            <p>Добавьте первый сервер для мониторинга</p>
            <button 
              className="btn btn-primary"
              onClick={openSidebar}
            >
              <span className="btn-icon">+</span>
              Добавить сервер
            </button>
          </div>
        )}
      </main>

      {/* Компонент бокового меню для добавления серверов */}
      <AddServerSidebar
        isOpen={sidebarOpen} // Передаем состояние видимости
        onClose={closeSidebar} // Функция для закрытия меню
        onServerAdded={handleServerAdded} // Обработчик успешного добавления
      />
    </div>
  );
}

// Экспортируем компонент для использования в других файлах
export default App;