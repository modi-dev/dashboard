// Импортируем необходимые библиотеки и стили
import React from 'react'; // React для создания компонента
import './ServerCard.css'; // Стили компонента карточки сервера

/**
 * Компонент карточки сервера для отображения информации о сервере
 * @param {Object} server - объект с данными сервера
 * @param {function} onRefresh - функция для обновления конкретного сервера
 * @param {function} onDelete - функция для удаления сервера
 */
const ServerCard = ({ server, onRefresh, onDelete }) => {
  /**
   * Возвращает цвет в зависимости от статуса сервера
   * @param {string} status - статус сервера (online, offline, unknown)
   * @returns {string} - hex код цвета
   */
  const getStatusColor = (status) => {
    switch (status) {
      case 'online':
        return '#4CAF50'; // Зеленый для онлайн
      case 'offline':
        return '#F44336'; // Красный для оффлайн
      default:
        return '#FF9800'; // Оранжевый для неизвестного статуса
    }
  };

  /**
   * Возвращает иконку в зависимости от статуса сервера
   * @param {string} status - статус сервера
   * @returns {string} - символ иконки
   */
  const getStatusIcon = (status) => {
    switch (status) {
      case 'online':
        return '●'; // Зеленая точка для онлайн
      case 'offline':
        return '●'; // Красная точка для оффлайн
      default:
        return '●'; // Оранжевая точка для неизвестного
    }
  };

  /**
   * Форматирует дату в удобочитаемый формат
   * Показывает дату и время (например, "26.10.2025 15:30:25")
   * @param {string} dateString - строка с датой
   * @returns {string} - отформатированная дата
   */
  const formatDate = (dateString) => {
    if (!dateString) return 'Никогда'; // Если дата отсутствует
    
    const date = new Date(dateString); // Создаем объект Date
    
    // Форматируем дату в формате ДД.ММ.ГГГГ
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0'); // +1 потому что месяцы начинаются с 0
    const year = date.getFullYear();
    
    // Форматируем время в формате ЧЧ:ММ:СС
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    
    return `${day}.${month}.${year} ${hours}:${minutes}:${seconds}`;
  };

  /**
   * Возвращает иконку в зависимости от типа сервера
   * @param {string} type - тип сервера
   * @returns {string} - emoji иконка
   */
  const getTypeIcon = (type) => {
    switch (type) {
      case 'Postgres':
        return '🐘'; // Слон для PostgreSQL
      case 'Redis':
        return '🔴'; // Красный круг для Redis
      case 'Kafka':
        return '⚡'; // Молния для Kafka
      case 'Astra Linux':
        return '🐧'; // Пингвин для Linux
      default:
        return '🌐'; // Глобус для других типов
    }
  };

  /**
   * Обработчик клика по кнопке обновления
   * Вызывает функцию onRefresh с ID сервера
   */
  const handleRefresh = () => {
    if (onRefresh) {
      onRefresh(server.id); // Передаем ID сервера для обновления
    }
  };

  /**
   * Обработчик клика по кнопке удаления
   * Показывает подтверждение и вызывает функцию onDelete с ID сервера
   */
  const handleDelete = () => {
    // Показываем диалог подтверждения
    const confirmed = window.confirm(
      `Вы уверены, что хотите удалить сервер "${server.name}"?\n\nЭто действие нельзя отменить.`
    );
    
    if (confirmed && onDelete) {
      onDelete(server.id); // Передаем ID сервера для удаления
    }
  };

  // Возвращаем JSX разметку карточки сервера
  return (
    <div className="server-card">
      {/* Заголовок карточки с основной информацией */}
      <div className="server-card-header">
        {/* Левая часть: название и тип сервера */}
        <div className="server-info">
          <h3 className="server-name">{server.name}</h3>
          <div className="server-type">
            <span className="type-icon">{getTypeIcon(server.type)}</span>
            <span className="type-name">{server.type}</span>
          </div>
        </div>
        
        {/* Правая часть: статус сервера */}
        <div className="server-status">
          <span 
            className="status-indicator"
            style={{ color: getStatusColor(server.status) }}
          >
            {getStatusIcon(server.status)}
          </span>
          <span 
            className="status-text"
            style={{ color: getStatusColor(server.status) }}
          >
            {server.status || 'unknown'}
          </span>
        </div>
      </div>
      
      {/* Детальная информация о сервере */}
      <div className="server-details">
        {/* URL сервера */}
        <div className="detail-row">
          <span className="detail-label">URL:</span>
          <span className="detail-value url">{server.url}</span>
        </div>
        
        {/* Условное отображение healthcheck (только если указан) */}
        {server.healthcheck && (
          <div className="detail-row">
            <span className="detail-label">Healthcheck:</span>
            <span className="detail-value">{server.healthcheck}</span>
          </div>
        )}
        
        {/* Дата и время последней проверки в формате ДД.ММ.ГГГГ ЧЧ:ММ:СС */}
        <div className="detail-row">
          <span className="detail-label">Последняя проверка:</span>
          <span className="detail-value">{formatDate(server.lastChecked)}</span>
        </div>
      </div>
      
      {/* Кнопки действий */}
      <div className="server-actions">
        <button 
          className="action-btn refresh-btn"
          onClick={handleRefresh}
          title="Проверить сейчас"
        >
          🔄 Обновить
        </button>
        
        <button 
          className="action-btn delete-btn"
          onClick={handleDelete}
          title="Удалить сервер"
        >
          🗑️ Удалить
        </button>
      </div>
    </div>
  );
};

// Экспортируем компонент для использования в других файлах
export default ServerCard;
