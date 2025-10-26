// Импортируем необходимые библиотеки и стили
import React, { useState } from 'react'; // React хук для состояния
import axios from 'axios'; // HTTP клиент для API запросов
import './AddServerSidebar.css'; // Стили компонента

/**
 * Компонент бокового меню для добавления новых серверов
 * @param {boolean} isOpen - состояние видимости меню
 * @param {function} onClose - функция для закрытия меню
 * @param {function} onServerAdded - функция вызываемая при успешном добавлении сервера
 */
const AddServerSidebar = ({ isOpen, onClose, onServerAdded }) => {
  // Состояние формы с начальными значениями
  const [formData, setFormData] = useState({
    name: '', // Название сервера
    url: '', // URL сервера
    type: 'Другое', // Тип сервера по умолчанию
    healthcheck: '' // Путь для проверки здоровья (только для типа "Другое")
  });
  
  // Состояние загрузки для блокировки формы во время отправки
  const [loading, setLoading] = useState(false);
  
  // Состояние для отображения ошибок валидации или API
  const [error, setError] = useState(null);

  /**
   * Обработчик изменения полей формы
   * Обновляет состояние формы при вводе пользователя
   * @param {Event} e - событие изменения поля
   */
  const handleInputChange = (e) => {
    // Извлекаем имя поля и его значение из события
    const { name, value } = e.target;
    
    // Обновляем состояние формы, сохраняя предыдущие значения
    setFormData(prev => ({
      ...prev, // Распаковываем предыдущее состояние
      [name]: value // Обновляем конкретное поле
    }));
  };

  /**
   * Обработчик отправки формы
   * Отправляет данные сервера на API и обрабатывает результат
   * @param {Event} e - событие отправки формы
   */
  const handleSubmit = async (e) => {
    e.preventDefault(); // Предотвращаем стандартную отправку формы
    
    // Устанавливаем состояние загрузки и очищаем ошибки
    setLoading(true);
    setError(null);

    try {
      // Отправляем POST запрос с данными формы
      const response = await axios.post('/api/servers', formData);
      
      // Проверяем успешность ответа
      if (response.data.success) {
        // Сбрасываем форму к начальным значениям
        setFormData({
          name: '',
          url: '',
          type: 'Другое',
          healthcheck: ''
        });
        
        // Уведомляем родительский компонент об успешном добавлении
        onServerAdded();
        // Закрываем боковое меню
        onClose();
      } else {
        // Отображаем ошибку от сервера
        setError(response.data.error || 'Ошибка при добавлении сервера');
      }
    } catch (error) {
      // Логируем ошибку в консоль для отладки
      console.error('Error adding server:', error);
      // Отображаем понятное сообщение об ошибке пользователю
      setError(error.response?.data?.error || error.message || 'Неизвестная ошибка');
    } finally {
      // В любом случае снимаем флаг загрузки
      setLoading(false);
    }
  };

  /**
   * Обработчик закрытия бокового меню
   * Сбрасывает форму и очищает ошибки перед закрытием
   */
  const handleClose = () => {
    // Сбрасываем форму к начальным значениям
    setFormData({
      name: '',
      url: '',
      type: 'Другое',
      healthcheck: ''
    });
    // Очищаем ошибки
    setError(null);
    // Вызываем функцию закрытия из родительского компонента
    onClose();
  };

  // Возвращаем JSX разметку компонента
  return (
    <>
      {/* Затемняющий фон - закрывает меню при клике */}
      {isOpen && <div className="sidebar-overlay" onClick={handleClose} />}
      
      {/* Основной контейнер бокового меню */}
      <div className={`add-server-sidebar ${isOpen ? 'open' : ''}`}>
        {/* Заголовок меню с кнопкой закрытия */}
        <div className="sidebar-header">
          <h2>Добавить сервер</h2>
          <button className="close-btn" onClick={handleClose}>
            <span>×</span>
          </button>
        </div>
        
        {/* Форма для добавления сервера */}
        <form onSubmit={handleSubmit} className="server-form">
          {/* Условное отображение ошибки */}
          {error && (
            <div className="error-message">
              {error}
            </div>
          )}
          
          {/* Поле для названия сервера */}
          <div className="form-group">
            <label htmlFor="name">Название сервера *</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleInputChange}
              placeholder="Например: Production API"
              required // Обязательное поле
            />
          </div>
          
          {/* Поле для URL сервера */}
          <div className="form-group">
            <label htmlFor="url">URL сервера *</label>
            <input
              type="url"
              id="url"
              name="url"
              value={formData.url}
              onChange={handleInputChange}
              placeholder="https://api.example.com"
              required // Обязательное поле
            />
          </div>
          
          {/* Выпадающий список типов серверов */}
          <div className="form-group">
            <label htmlFor="type">Тип сервера *</label>
            <select
              id="type"
              name="type"
              value={formData.type}
              onChange={handleInputChange}
              required // Обязательное поле
            >
              <option value="Postgres">Postgres</option>
              <option value="Redis">Redis</option>
              <option value="Kafka">Kafka</option>
              <option value="Astra Linux">Astra Linux</option>
              <option value="Другое">Другое</option>
            </select>
          </div>
          
          {/* Условное поле healthcheck - показывается только для типа "Другое" */}
          {formData.type === 'Другое' && (
            <div className="form-group">
              <label htmlFor="healthcheck">Healthcheck путь</label>
              <input
                type="text"
                id="healthcheck"
                name="healthcheck"
                value={formData.healthcheck}
                onChange={handleInputChange}
                placeholder="/health, /status, /ping"
              />
              <small className="form-hint">
                Укажите путь для проверки здоровья сервера (например: /health)
              </small>
            </div>
          )}
          
          {/* Кнопки действий формы */}
          <div className="form-actions">
            {/* Кнопка отмены */}
            <button
              type="button"
              onClick={handleClose}
              className="btn btn-secondary"
              disabled={loading} // Блокируем во время загрузки
            >
              Отмена
            </button>
            
            {/* Кнопка отправки формы */}
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading} // Блокируем во время загрузки
            >
              {/* Динамический текст в зависимости от состояния загрузки */}
              {loading ? 'Добавление...' : 'Добавить сервер'}
            </button>
          </div>
        </form>
      </div>
    </>
  );
};

// Экспортируем компонент для использования в других файлах
export default AddServerSidebar;
