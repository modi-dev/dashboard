import React, { useState, useEffect } from 'react';
import axios from 'axios';

function App() {
  const [servers, setServers] = useState([]);
  const [newServer, setNewServer] = useState({ name: '', url: '' });

  useEffect(() => {
    fetchServers();
    
    // Автоматическое обновление каждые 30 секунд
    const interval = setInterval(fetchServers, 30000);
    
    return () => clearInterval(interval);
  }, []);

  const fetchServers = async () => {
    try {
      const response = await axios.get('/api/servers');
      setServers(response.data);
    } catch (error) {
      console.error('Error fetching servers:', error);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post('/api/servers', newServer);
      setNewServer({ name: '', url: '' });
      fetchServers();
    } catch (error) {
      console.error('Error adding server:', error);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'online':
        return '#4CAF50'; // зеленый
      case 'offline':
        return '#F44336'; // красный
      default:
        return '#FF9800'; // оранжевый
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'Never';
    return new Date(dateString).toLocaleString();
  };

  return (
    <div className="App">
      <h1>Server Dashboard</h1>
      
      <form onSubmit={handleSubmit} style={{ marginBottom: '20px', padding: '20px', border: '1px solid #ddd', borderRadius: '5px' }}>
        <h3>Add New Server</h3>
        <div style={{ marginBottom: '10px' }}>
          <input
            type="text"
            placeholder="Server Name"
            value={newServer.name}
            onChange={(e) => setNewServer({ ...newServer, name: e.target.value })}
            required
            style={{ marginRight: '10px', padding: '5px' }}
          />
          <input
            type="url"
            placeholder="Server URL"
            value={newServer.url}
            onChange={(e) => setNewServer({ ...newServer, url: e.target.value })}
            required
            style={{ marginRight: '10px', padding: '5px', width: '300px' }}
          />
          <button type="submit" style={{ padding: '5px 15px', backgroundColor: '#2196F3', color: 'white', border: 'none', borderRadius: '3px' }}>
            Add Server
          </button>
        </div>
      </form>

      <h3>Server Status</h3>
      <table style={{ width: '100%', borderCollapse: 'collapse', border: '1px solid #ddd' }}>
        <thead>
          <tr style={{ backgroundColor: '#f5f5f5' }}>
            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #ddd' }}>Name</th>
            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #ddd' }}>URL</th>
            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #ddd' }}>Status</th>
            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #ddd' }}>Last Checked</th>
          </tr>
        </thead>
        <tbody>
          {servers.map(server => (
            <tr key={server.id}>
              <td style={{ padding: '12px', border: '1px solid #ddd' }}>{server.name}</td>
              <td style={{ padding: '12px', border: '1px solid #ddd' }}>{server.url}</td>
              <td style={{ padding: '12px', border: '1px solid #ddd' }}>
                <span 
                  style={{ 
                    padding: '4px 8px', 
                    borderRadius: '4px', 
                    color: 'white', 
                    backgroundColor: getStatusColor(server.status),
                    fontWeight: 'bold'
                  }}
                >
                  {server.status || 'unknown'}
                </span>
              </td>
              <td style={{ padding: '12px', border: '1px solid #ddd' }}>
                {formatDate(server.lastChecked)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      
      {servers.length === 0 && (
        <p style={{ textAlign: 'center', color: '#666', fontStyle: 'italic' }}>
          No servers added yet. Add your first server above.
        </p>
      )}
    </div>
  );
}

export default App;