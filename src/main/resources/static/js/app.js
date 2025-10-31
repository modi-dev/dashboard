// Common UI functions used across the application

// Notification function
function showNotification(message, type = 'info') {
  const notification = document.createElement('div');
  notification.className = `alert alert-${type === 'success' ? 'success' : type === 'error' ? 'danger' : 'info'} alert-dismissible fade show position-fixed`;
  notification.style.cssText = 'top: 20px; right: 20px; z-index: 1060; min-width: 300px;';
  notification.innerHTML = `
    ${message}
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
  `;

  document.body.appendChild(notification);

  setTimeout(() => {
    if (notification.parentNode) {
      notification.remove();
    }
  }, 5000);
}

// Delete server function
function deleteServer(serverId) {
  if (!serverId) return;
  if (confirm('Вы уверены, что хотите удалить этот сервер?')) {
    fetch('/api/servers/' + serverId, { method: 'DELETE' })
      .then(response => response.json())
      .then(data => {
        if (data.success) {
          location.reload();
          showNotification('Сервер успешно удален!', 'success');
        } else {
          showNotification('Ошибка: ' + (data.error || data.message || 'Неизвестная ошибка'), 'error');
        }
      })
      .catch(error => {
        console.error('Error:', error);
        showNotification('Произошла ошибка при удалении сервера', 'error');
      });
  }
}

// Refresh servers function
function refreshServers() {
  const btn = event && event.target ? event.target.closest('button') : null;
  const originalContent = btn ? btn.innerHTML : null;
  if (btn) {
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i>Обновление...';
  }

  fetch('/api/servers/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  })
    .then(response => response.json())
    .then(data => {
      if (data.success) {
        showNotification('Статус серверов успешно обновлен!', 'success');
        setTimeout(() => { location.reload(); }, 500);
      } else {
        showNotification('Ошибка: ' + (data.error || data.message || 'Неизвестная ошибка'), 'error');
        if (btn) { btn.disabled = false; btn.innerHTML = originalContent; }
      }
    })
    .catch(error => {
      console.error('Error:', error);
      showNotification('Произошла ошибка при обновлении статуса серверов', 'error');
      if (btn) { btn.disabled = false; btn.innerHTML = originalContent; }
    });
}

// Refresh pods function
function refreshPods() {
  const btn = event && event.target ? event.target.closest('button') : null;
  const originalContent = btn ? btn.innerHTML : null;
  if (btn) {
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i>Обновление...';
  }

  fetch('/api/pods/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  })
    .then(response => response.json())
    .then(data => {
      if (data.success) {
        showNotification('Информация о подах успешно обновлена!', 'success');
        setTimeout(() => { location.reload(); }, 500);
      } else {
        showNotification('Ошибка: ' + (data.error || data.message || 'Неизвестная ошибка'), 'error');
        if (btn) { btn.disabled = false; btn.innerHTML = originalContent; }
      }
    })
    .catch(error => {
      console.error('Error:', error);
      showNotification('Произошла ошибка при обновлении информации о подах', 'error');
      if (btn) { btn.disabled = false; btn.innerHTML = originalContent; }
    });
}
