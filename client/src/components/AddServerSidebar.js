import React, { useState } from 'react';
import axios from 'axios';
import './AddServerSidebar.css';

const AddServerSidebar = ({ isOpen, onClose, onServerAdded }) => {
  const [formData, setFormData] = useState({
    name: '',
    url: '',
    type: 'Другое',
    healthcheck: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await axios.post('/api/servers', formData);
      
      if (response.data.success) {
        // Сброс формы
        setFormData({
          name: '',
          url: '',
          type: 'Другое',
          healthcheck: ''
        });
        
        // Уведомление об успехе
        onServerAdded();
        onClose();
      } else {
        setError(response.data.error || 'Ошибка при добавлении сервера');
      }
    } catch (error) {
      console.error('Error adding server:', error);
      setError(error.response?.data?.error || error.message || 'Неизвестная ошибка');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setFormData({
      name: '',
      url: '',
      type: 'Другое',
      healthcheck: ''
    });
    setError(null);
    onClose();
  };

  return (
    <>
      {/* Overlay */}
      {isOpen && <div className="sidebar-overlay" onClick={handleClose} />}
      
      {/* Sidebar */}
      <div className={`add-server-sidebar ${isOpen ? 'open' : ''}`}>
        <div className="sidebar-header">
          <h2>Добавить сервер</h2>
          <button className="close-btn" onClick={handleClose}>
            <span>×</span>
          </button>
        </div>
        
        <form onSubmit={handleSubmit} className="server-form">
          {error && (
            <div className="error-message">
              {error}
            </div>
          )}
          
          <div className="form-group">
            <label htmlFor="name">Название сервера *</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleInputChange}
              placeholder="Например: Production API"
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="url">URL сервера *</label>
            <input
              type="url"
              id="url"
              name="url"
              value={formData.url}
              onChange={handleInputChange}
              placeholder="https://api.example.com"
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="type">Тип сервера *</label>
            <select
              id="type"
              name="type"
              value={formData.type}
              onChange={handleInputChange}
              required
            >
              <option value="Postgres">Postgres</option>
              <option value="Redis">Redis</option>
              <option value="Kafka">Kafka</option>
              <option value="Astra Linux">Astra Linux</option>
              <option value="Другое">Другое</option>
            </select>
          </div>
          
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
          
          <div className="form-actions">
            <button
              type="button"
              onClick={handleClose}
              className="btn btn-secondary"
              disabled={loading}
            >
              Отмена
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? 'Добавление...' : 'Добавить сервер'}
            </button>
          </div>
        </form>
      </div>
    </>
  );
};

export default AddServerSidebar;
