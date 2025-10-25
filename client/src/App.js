import React, { useState, useEffect } from 'react';
import axios from 'axios';
import AddServerSidebar from './components/AddServerSidebar';
import ServerCard from './components/ServerCard';
import './App.css';

function App() {
  const [servers, setServers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [sidebarOpen, setSidebarOpen] = useState(false);

  useEffect(() => {
    fetchServers();
    
    // Автоматическое обновление каждые 30 секунд
    const interval = setInterval(fetchServers, 30000);
    
    return () => clearInterval(interval);
  }, []);

  const fetchServers = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get('/api/servers');
      if (response.data.success && response.data.data) {
        setServers(response.data.data);
      } else {
        console.error('API returned unsuccessful response:', response.data);
        setServers([]);
        setError('Ошибка при получении данных с сервера');
      }
    } catch (error) {
      console.error('Error fetching servers:', error);
      setServers([]);
      setError('Не удалось подключиться к серверу');
    } finally {
      setLoading(false);
    }
  };

  const handleServerAdded = () => {
    fetchServers();
  };

  const handleRefreshServer = async (serverId) => {
    try {
      await axios.post(`/api/servers/${serverId}/check`);
      fetchServers();
    } catch (error) {
      console.error('Error refreshing server:', error);
    }
  };

  const openSidebar = () => {
    setSidebarOpen(true);
  };

  const closeSidebar = () => {
    setSidebarOpen(false);
  };

  return (
    <div className="App">
      <header className="app-header">
        <div className="header-content">
          <h1 className="app-title">
            <span className="title-icon">📊</span>
            Server Dashboard
          </h1>
          <div className="header-actions">
            <button 
              className="btn btn-primary add-server-btn"
              onClick={openSidebar}
            >
              <span className="btn-icon">+</span>
              Добавить сервер
            </button>
            <button 
              className="btn btn-secondary refresh-btn"
              onClick={fetchServers}
              disabled={loading}
            >
              <span className="btn-icon">🔄</span>
              {loading ? 'Обновление...' : 'Обновить'}
            </button>
          </div>
        </div>
      </header>

      <main className="app-main">
        {error && (
          <div className="error-banner">
            <span className="error-icon">⚠️</span>
            {error}
          </div>
        )}

        {loading && servers.length === 0 && (
          <div className="loading-container">
            <div className="loading-spinner"></div>
            <p>Загрузка серверов...</p>
          </div>
        )}

        {!loading && servers.length > 0 && (
          <div className="servers-grid">
            {servers.map(server => (
              <ServerCard
                key={server.id}
                server={server}
                onRefresh={handleRefreshServer}
              />
            ))}
          </div>
        )}

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

      <AddServerSidebar
        isOpen={sidebarOpen}
        onClose={closeSidebar}
        onServerAdded={handleServerAdded}
      />
    </div>
  );
}

export default App;