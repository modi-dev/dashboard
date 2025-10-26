import React, { useState, useEffect } from 'react';
import './PodInfo.css';

/**
 * Компонент для отображения информации о Kubernetes подах
 * Интегрирует функциональность из version.sh скрипта
 */
const PodInfo = () => {
  // Состояние для хранения данных о подах
  const [pods, setPods] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [namespace, setNamespace] = useState('');
  const [lastUpdate, setLastUpdate] = useState(null);

  /**
   * Загрузка данных о подах с сервера
   */
  const fetchPods = async () => {
    try {
      setLoading(true);
      setError(null);

      console.log('Загружаем данные о подах...');

      // Загружаем информацию о подах
      const podsResponse = await fetch('http://localhost:3001/api/version/pods');
      console.log('Pods response status:', podsResponse.status);
      
      if (!podsResponse.ok) {
        const errorText = await podsResponse.text();
        console.error('Pods response error:', errorText);
        throw new Error(`Ошибка загрузки данных о подах: ${podsResponse.status} - ${errorText}`);
      }
      
      const podsData = await podsResponse.json();
      console.log('Pods data received:', podsData);

      // Загружаем информацию о namespace
      const namespaceResponse = await fetch('http://localhost:3001/api/version/namespace');
      if (namespaceResponse.ok) {
        const namespaceData = await namespaceResponse.text();
        setNamespace(namespaceData);
        console.log('Namespace:', namespaceData);
      }

      setPods(podsData);
      setLastUpdate(new Date());
    } catch (err) {
      console.error('Ошибка при загрузке данных о подах:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Загрузка HTML страницы с версиями (как в оригинальном скрипте)
   */
  const fetchHtmlPage = async () => {
    try {
      const response = await fetch('http://localhost:3001/api/version/html');
      if (!response.ok) {
        throw new Error('Ошибка загрузки HTML страницы');
      }
      const html = await response.text();
      
      // Открываем HTML в новом окне
      const newWindow = window.open('', '_blank');
      newWindow.document.write(html);
      newWindow.document.close();
    } catch (err) {
      console.error('Ошибка при загрузке HTML страницы:', err);
      setError(err.message);
    }
  };

  /**
   * Проверка доступности Kubernetes API
   */
  const checkKubernetesHealth = async () => {
    try {
      const response = await fetch('http://localhost:3001/api/version/health');
      if (response.ok) {
        const healthData = await response.text();
        alert(`Kubernetes API: ${healthData}`);
      } else {
        alert('Kubernetes API недоступен');
      }
    } catch (err) {
      console.error('Ошибка при проверке Kubernetes API:', err);
      alert('Ошибка при проверке Kubernetes API');
    }
  };

  // Загружаем данные при монтировании компонента
  useEffect(() => {
    fetchPods();
  }, []);

  /**
   * Форматирование даты для отображения
   */
  const formatDate = (dateString) => {
    if (!dateString) return 'Неизвестно';
    
    try {
      const date = new Date(dateString);
      return date.toLocaleString('ru-RU', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    } catch (err) {
      return dateString;
    }
  };

  /**
   * Форматирование времени последнего обновления
   */
  const formatLastUpdate = () => {
    if (!lastUpdate) return 'Никогда';
    
    return lastUpdate.toLocaleString('ru-RU', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  };

  // Если загрузка, показываем индикатор
  if (loading) {
    return (
      <div className="pod-info-container">
        <div className="pod-info-header">
          <h2>📦 Информация о подах Kubernetes</h2>
          <div className="loading-spinner">Загрузка...</div>
        </div>
      </div>
    );
  }

  // Если ошибка, показываем сообщение об ошибке
  if (error) {
    return (
      <div className="pod-info-container">
        <div className="pod-info-header">
          <h2>📦 Информация о подах Kubernetes</h2>
          <div className="error-message">
            <p>❌ Ошибка: {error}</p>
            <button onClick={fetchPods} className="retry-button">
              🔄 Попробовать снова
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="pod-info-container">
      {/* Заголовок с кнопками управления */}
      <div className="pod-info-header">
        <h2>📦 Информация о подах Kubernetes</h2>
        <div className="pod-info-controls">
          <button onClick={fetchPods} className="refresh-button" title="Обновить данные">
            🔄 Обновить
          </button>
          <button onClick={fetchHtmlPage} className="html-button" title="Открыть HTML версию">
            🌐 HTML версия
          </button>
          <button onClick={checkKubernetesHealth} className="health-button" title="Проверить Kubernetes API">
            ❤️ Проверить API
          </button>
        </div>
      </div>

      {/* Информация о namespace и времени обновления */}
      <div className="pod-info-meta">
        <div className="meta-item">
          <span className="meta-label">Namespace:</span>
          <span className="meta-value">{namespace || 'Неизвестно'}</span>
        </div>
        <div className="meta-item">
          <span className="meta-label">Последнее обновление:</span>
          <span className="meta-value">{formatLastUpdate()}</span>
        </div>
        <div className="meta-item">
          <span className="meta-label">Количество подов:</span>
          <span className="meta-value">{pods.length}</span>
        </div>
      </div>

      {/* Таблица с информацией о подах */}
      {pods.length > 0 ? (
        <div className="pod-info-table-container">
          <table className="pod-info-table">
            <thead>
              <tr>
                <th>Имя</th>
                <th>Версия</th>
                <th>Ms Branch</th>
                <th>Config Branch</th>
                <th>GC Options</th>
                <th>Дата создания</th>
                <th>Порт</th>
                <th>Ресурсы</th>
              </tr>
            </thead>
            <tbody>
              {pods.map((pod, index) => (
                <tr key={index} className="pod-row">
                  <td className="pod-name">{pod.name || 'Неизвестно'}</td>
                  <td className="pod-version">{pod.version || 'Неизвестно'}</td>
                  <td className="pod-ms-branch">{pod.msBranch || '-'}</td>
                  <td className="pod-config-branch">{pod.configBranch || '-'}</td>
                  <td className="pod-gc-options">{pod.gcOptions || '-'}</td>
                  <td className="pod-creation-date">{formatDate(pod.creationDate)}</td>
                  <td className="pod-port">{pod.port || '-'}</td>
                  <td className="pod-resources">
                    <div className="resource-info">
                      <div className="resource-item">
                        <span className="resource-label">CPU:</span>
                        <span className="resource-value">{pod.cpuRequest || '-'}</span>
                      </div>
                      <div className="resource-item">
                        <span className="resource-label">RAM:</span>
                        <span className="resource-value">{pod.memoryRequest || '-'}</span>
                      </div>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="no-pods-message">
          <p>📭 Поды не найдены</p>
          <p>Убедитесь, что Kubernetes интеграция включена и есть запущенные поды</p>
        </div>
      )}
    </div>
  );
};

export default PodInfo;
