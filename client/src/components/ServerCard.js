import React from 'react';
import './ServerCard.css';

const ServerCard = ({ server, onRefresh }) => {
  const getStatusColor = (status) => {
    switch (status) {
      case 'online':
        return '#4CAF50';
      case 'offline':
        return '#F44336';
      default:
        return '#FF9800';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'online':
        return '●';
      case 'offline':
        return '●';
      default:
        return '●';
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'Никогда';
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    
    if (diffMins < 1) return 'Только что';
    if (diffMins < 60) return `${diffMins} мин назад`;
    if (diffMins < 1440) return `${Math.floor(diffMins / 60)} ч назад`;
    return date.toLocaleString();
  };

  const getTypeIcon = (type) => {
    switch (type) {
      case 'Postgres':
        return '🐘';
      case 'Redis':
        return '🔴';
      case 'Kafka':
        return '⚡';
      case 'Astra Linux':
        return '🐧';
      default:
        return '🌐';
    }
  };

  const handleRefresh = () => {
    if (onRefresh) {
      onRefresh(server.id);
    }
  };

  return (
    <div className="server-card">
      <div className="server-card-header">
        <div className="server-info">
          <h3 className="server-name">{server.name}</h3>
          <div className="server-type">
            <span className="type-icon">{getTypeIcon(server.type)}</span>
            <span className="type-name">{server.type}</span>
          </div>
        </div>
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
      
      <div className="server-details">
        <div className="detail-row">
          <span className="detail-label">URL:</span>
          <span className="detail-value url">{server.url}</span>
        </div>
        
        {server.healthcheck && (
          <div className="detail-row">
            <span className="detail-label">Healthcheck:</span>
            <span className="detail-value">{server.healthcheck}</span>
          </div>
        )}
        
        <div className="detail-row">
          <span className="detail-label">Последняя проверка:</span>
          <span className="detail-value">{formatDate(server.lastChecked)}</span>
        </div>
      </div>
      
      <div className="server-actions">
        <button 
          className="action-btn refresh-btn"
          onClick={handleRefresh}
          title="Проверить сейчас"
        >
          🔄 Обновить
        </button>
      </div>
    </div>
  );
};

export default ServerCard;
