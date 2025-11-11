const PODS_COLUMN_SETTINGS_KEY = 'podsTableColumnSettings';
let sidebarKeyListenerBound = false;

function onDocumentReady(callback) {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', callback, { once: true });
  } else {
    callback();
  }
}

// Notifications -------------------------------------------------------------
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

// Server actions ------------------------------------------------------------
function deleteServer(serverId) {
  if (!serverId) {
    return;
  }

  if (!confirm('Вы уверены, что хотите удалить этот сервер?')) {
    return;
  }

  fetch(`/api/servers/${serverId}`, {
    method: 'DELETE',
    credentials: 'same-origin'
  })
    .then(response => {
      if (response.status === 401 || response.status === 403) {
        window.location.href = '/login';
        return null;
      }
      return response.json();
    })
    .then(data => {
      if (!data) {
        return;
      }
      if (data.success) {
        showNotification('Сервер успешно удален!', 'success');
        setTimeout(() => location.reload(), 200);
      } else {
        showNotification('Ошибка: ' + (data.error || data.message || 'Неизвестная ошибка'), 'error');
      }
    })
    .catch(error => {
      console.error('Error:', error);
      showNotification('Произошла ошибка при удалении сервера', 'error');
    });
}

function refreshServers(ev) {
  const btn = ev && ev.target ? ev.target.closest('button') : null;
  const originalContent = btn ? btn.innerHTML : null;

  if (btn) {
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i>Обновление...';
  }

  fetch('/api/servers/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'same-origin'
  })
    .then(response => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    })
    .then(data => {
      if (!data) {
        return;
      }
      if (data.success) {
        showNotification('Статус серверов успешно обновлен!', 'success');
        setTimeout(() => location.reload(), 500);
      } else {
        showNotification('Ошибка: ' + (data.error || data.message || 'Неизвестная ошибка'), 'error');
        if (btn) {
          btn.disabled = false;
          btn.innerHTML = originalContent;
        }
      }
    })
    .catch(error => {
      console.error('Error:', error);
      showNotification('Произошла ошибка при обновлении статуса серверов', 'error');
      if (btn) {
        btn.disabled = false;
        btn.innerHTML = originalContent;
      }
    });
}

function refreshPods(ev) {
  const btn = ev && ev.target ? ev.target.closest('button') : null;
  const originalContent = btn ? btn.innerHTML : null;

  if (btn) {
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i>Обновление...';
  }

  fetch('/api/pods/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'same-origin'
  })
    .then(response => {
      if (response.status === 401 || response.status === 403) {
        window.location.href = '/login';
        return null;
      }
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    })
    .then(data => {
      if (!data) {
        return;
      }
      if (data.success) {
        showNotification('Информация о подах успешно обновлена!', 'success');
        setTimeout(() => location.reload(), 500);
      } else {
        showNotification('Ошибка: ' + (data.error || data.message || 'Неизвестная ошибка'), 'error');
        if (btn) {
          btn.disabled = false;
          btn.innerHTML = originalContent;
        }
      }
    })
    .catch(error => {
      console.error('Error:', error);
      showNotification('Произошла ошибка при обновлении информации о подах', 'error');
      if (btn) {
        btn.disabled = false;
        btn.innerHTML = originalContent;
      }
    });
}

// Theme ---------------------------------------------------------------------
function toggleTheme() {
  const htmlRoot = document.getElementById('htmlRoot');
  const themeIcon = document.getElementById('themeIcon');
  const themeIconMobile = document.getElementById('themeIconMobile');
  const isDark = htmlRoot.classList.contains('theme-dark');

  if (isDark) {
    htmlRoot.classList.remove('theme-dark');
    if (themeIcon) {
      themeIcon.classList.remove('fa-sun');
      themeIcon.classList.add('fa-moon');
    }
    if (themeIconMobile) {
      themeIconMobile.classList.remove('fa-sun');
      themeIconMobile.classList.add('fa-moon');
    }
    localStorage.setItem('theme', 'light');
  } else {
    htmlRoot.classList.add('theme-dark');
    if (themeIcon) {
      themeIcon.classList.remove('fa-moon');
      themeIcon.classList.add('fa-sun');
    }
    if (themeIconMobile) {
      themeIconMobile.classList.remove('fa-moon');
      themeIconMobile.classList.add('fa-sun');
    }
    localStorage.setItem('theme', 'dark');
  }
}

function initTheme() {
  const savedTheme = localStorage.getItem('theme') || 'light';
  const htmlRoot = document.getElementById('htmlRoot');
  const themeIcon = document.getElementById('themeIcon');
  const themeIconMobile = document.getElementById('themeIconMobile');

  if (savedTheme === 'dark') {
    htmlRoot.classList.add('theme-dark');
    if (themeIcon) {
      themeIcon.classList.remove('fa-moon');
      themeIcon.classList.add('fa-sun');
    }
    if (themeIconMobile) {
      themeIconMobile.classList.remove('fa-moon');
      themeIconMobile.classList.add('fa-sun');
    }
  } else {
    htmlRoot.classList.remove('theme-dark');
    if (themeIcon) {
      themeIcon.classList.remove('fa-sun');
      themeIcon.classList.add('fa-moon');
    }
    if (themeIconMobile) {
      themeIconMobile.classList.remove('fa-sun');
      themeIconMobile.classList.add('fa-moon');
    }
  }
}

// Sidebar & add server ------------------------------------------------------
function openSidebar() {
  const sidebar = document.getElementById('addServerSidebar');
  const overlay = document.getElementById('sidebarOverlay');
  const mainContent = document.querySelector('.main-content');

  if (sidebar) {
    sidebar.classList.add('open');
  }
  if (overlay) {
    overlay.classList.add('show');
  }
  if (mainContent) {
    mainContent.classList.add('sidebar-open');
  }

  document.body.style.overflow = 'hidden';

  if (sidebar) {
    const firstInput = sidebar.querySelector('input, select, textarea');
    if (firstInput) {
      setTimeout(() => firstInput.focus(), 0);
    }
  }
}

function closeSidebar() {
  const sidebar = document.getElementById('addServerSidebar');
  const overlay = document.getElementById('sidebarOverlay');
  const mainContent = document.querySelector('.main-content');
  const form = document.getElementById('addServerForm');

  if (sidebar) {
    sidebar.classList.remove('open');
  }
  if (overlay) {
    overlay.classList.remove('show');
  }
  if (mainContent) {
    mainContent.classList.remove('sidebar-open');
  }

  document.body.style.overflow = 'auto';

  if (form) {
    form.reset();
  }

  toggleHealthcheck();
}

function toggleHealthcheck() {
  const serverType = document.getElementById('serverType');
  if (!serverType) {
    return;
  }

  const type = serverType.value;
  const showExtras = type === 'OTHER';

  const healthcheckField = document.getElementById('healthcheckField');
  const healthcheckInput = document.getElementById('serverHealthcheck');
  const metricsEndpointField = document.getElementById('metricsEndpointField');
  const versionRegexField = document.getElementById('versionRegexField');

  if (healthcheckField) {
    healthcheckField.style.display = showExtras ? 'block' : 'none';
  }
  if (healthcheckInput) {
    healthcheckInput.required = showExtras;
    if (!showExtras) {
      healthcheckInput.value = '';
    }
  }
  if (metricsEndpointField) {
    metricsEndpointField.style.display = showExtras ? 'block' : 'none';
  }
  if (versionRegexField) {
    versionRegexField.style.display = showExtras ? 'block' : 'none';
  }
}

function addServer() {
  const nameInput = document.getElementById('serverName');
  const urlInput = document.getElementById('serverUrl');
  const typeSelect = document.getElementById('serverType');

  if (!nameInput || !urlInput || !typeSelect) {
    return;
  }

  const serverData = {
    name: nameInput.value,
    url: urlInput.value,
    type: typeSelect.value
  };

  if (serverData.type === 'OTHER') {
    const healthcheckInput = document.getElementById('serverHealthcheck');
    const metricsEndpointInput = document.getElementById('serverMetricsEndpoint');
    const versionRegexInput = document.getElementById('serverVersionRegex');

    if (healthcheckInput) {
      serverData.healthcheck = healthcheckInput.value;
    }
    if (metricsEndpointInput) {
      serverData.metricsEndpoint = metricsEndpointInput.value;
    }
    if (versionRegexInput) {
      serverData.versionRegex = versionRegexInput.value;
    }
  }

  fetch('/api/servers', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(serverData),
    credentials: 'same-origin'
  })
    .then(response => {
      if (response.status === 401 || response.status === 403) {
        window.location.href = '/login';
        return null;
      }
      return response.json();
    })
    .then(data => {
      if (!data) {
        return;
      }
      if (data.success) {
        closeSidebar();
        showNotification('Сервер успешно добавлен!', 'success');
        setTimeout(() => location.reload(), 300);
      } else {
        showNotification('Ошибка: ' + (data.error || data.message || 'Неизвестная ошибка'), 'error');
      }
    })
    .catch(error => {
      console.error('Error:', error);
      showNotification('Произошла ошибка при добавлении сервера', 'error');
    });
}

function initSidebar() {
  const addServerForm = document.getElementById('addServerForm');
  if (addServerForm && addServerForm.dataset.bound !== 'true') {
    addServerForm.addEventListener('submit', (event) => {
      event.preventDefault();
      addServer();
    });
    addServerForm.dataset.bound = 'true';
  }

  const serverType = document.getElementById('serverType');
  if (serverType && serverType.dataset.bound !== 'true') {
    serverType.addEventListener('change', toggleHealthcheck);
    serverType.dataset.bound = 'true';
  }

  toggleHealthcheck();

  if (!sidebarKeyListenerBound) {
    document.addEventListener('keydown', (event) => {
      if (event.key === 'Escape') {
        closeSidebar();
      }
    });
    sidebarKeyListenerBound = true;
  }
}

// Table filters -------------------------------------------------------------
function initServersFilter() {
  const filterInput = document.getElementById('serversFilter');
  const table = document.getElementById('serversTable');

  if (!filterInput || !table || filterInput.dataset.bound === 'true') {
    return;
  }

  filterInput.addEventListener('input', () => {
    const query = filterInput.value.trim().toLowerCase();
    const rows = table.querySelectorAll('tbody tr');

    rows.forEach(row => {
      const text = row.innerText.toLowerCase();
      row.style.display = text.includes(query) ? '' : 'none';
    });
  });

  filterInput.dataset.bound = 'true';
}

function initPodsFilter(table) {
  const filterInput = document.getElementById('podsFilter');

  if (!filterInput || filterInput.dataset.bound === 'true') {
    return;
  }

  filterInput.addEventListener('input', () => {
    const query = filterInput.value.trim().toLowerCase();
    const rows = table.querySelectorAll('tbody tr');

    rows.forEach(row => {
      const isGroup = row.getAttribute('data-is-group') === 'true';
      const isGroupItem = row.getAttribute('data-is-group-item') === 'true';
      const text = row.innerText.toLowerCase();
      const matches = text.includes(query);

      if (isGroup) {
        const groupName = row.getAttribute('data-group-name');
        const groupItems = table.querySelectorAll(`tr[data-group-name="${groupName}"][data-is-group-item="true"]`);
        let anyChildMatches = matches;

        groupItems.forEach(item => {
          if (item.innerText.toLowerCase().includes(query)) {
            anyChildMatches = true;
          }
        });

        if (anyChildMatches) {
          row.style.display = '';
          if (row.getAttribute('data-expanded') === 'true') {
            groupItems.forEach(item => {
              item.style.display = item.innerText.toLowerCase().includes(query) ? '' : 'none';
            });
          }
        } else {
          row.style.display = 'none';
          groupItems.forEach(item => {
            item.style.display = 'none';
          });
        }
      } else if (isGroupItem) {
        const groupName = row.getAttribute('data-group-name');
        const groupRow = table.querySelector(`tr[data-group-name="${groupName}"][data-is-group="true"]`);
        if (groupRow && groupRow.getAttribute('data-expanded') === 'true' && matches) {
          row.style.display = '';
        } else if (!matches) {
          row.style.display = 'none';
        }
      } else {
        row.style.display = matches ? '' : 'none';
      }
    });
  });

  filterInput.dataset.bound = 'true';
}

// Pods grouping & controls --------------------------------------------------
function groupPods(table) {
  if (table.dataset.grouped === 'true') {
    return;
  }

  const tbody = table.querySelector('tbody');
  if (!tbody) {
    return;
  }

  const rows = Array.from(tbody.querySelectorAll('tr'));
  const groups = {};

  rows.forEach(row => {
    const podName = row.getAttribute('data-pod-name');
    if (!groups[podName]) {
      groups[podName] = [];
    }
    groups[podName].push(row);
  });

  tbody.innerHTML = '';

  Object.keys(groups).sort().forEach(name => {
    const groupRows = groups[name];

    if (groupRows.length > 1) {
      const firstRow = groupRows[0];
      const groupedRow = firstRow.cloneNode(true);
      groupedRow.setAttribute('data-group-name', name);
      groupedRow.setAttribute('data-is-group', 'true');
      groupedRow.setAttribute('data-expanded', 'false');

      const firstCell = groupedRow.querySelector('td:first-child');
      if (firstCell) {
        firstCell.innerHTML = '<button class="btn btn-sm btn-link p-0" onclick="togglePodGroup(this)" style="min-width: 20px;"><i class="fas fa-chevron-right"></i></button>';
      }

      const detailCells = Array.from(groupedRow.querySelectorAll('.pod-detail-col'));
      detailCells.forEach(cell => {
        cell.setAttribute('data-original-html', cell.innerHTML);
        cell.innerHTML = '';
        cell.style.visibility = 'hidden';
        cell.style.width = '0';
        cell.style.padding = '0';
        cell.style.border = 'none';
      });

      groupRows.forEach(row => {
        row.setAttribute('data-group-name', name);
        row.setAttribute('data-is-group-item', 'true');
        row.style.display = 'none';
        const cell = row.querySelector('td:first-child');
        if (cell) {
          cell.innerHTML = '';
        }
      });

      const nameCell = groupedRow.querySelector('td:nth-child(2)');
      if (nameCell) {
        const badge = document.createElement('span');
        badge.className = 'badge bg-primary ms-2';
        badge.textContent = groupRows.length;
        const strong = nameCell.querySelector('strong') || nameCell;
        strong.appendChild(badge);
      }

      tbody.appendChild(groupedRow);
      groupRows.forEach(row => tbody.appendChild(row));
    } else {
      const row = groupRows[0];
      const firstCell = row.querySelector('td:first-child');
      if (firstCell) {
        firstCell.innerHTML = '';
      }
      tbody.appendChild(row);
    }
  });

  table.dataset.grouped = 'true';
}

function togglePodGroup(button) {
  const row = button.closest('tr');
  if (!row) {
    return;
  }

  const table = row.closest('table');
  const groupName = row.getAttribute('data-group-name');
  const isExpanded = row.getAttribute('data-expanded') === 'true';
  const icon = button.querySelector('i');

  if (isExpanded) {
    if (icon) {
      icon.className = 'fas fa-chevron-right';
    }
    row.setAttribute('data-expanded', 'false');

    const detailCells = Array.from(row.querySelectorAll('.pod-detail-col'));
    detailCells.forEach(cell => {
      cell.setAttribute('data-original-html', cell.innerHTML);
      cell.innerHTML = '';
      cell.style.visibility = 'hidden';
      cell.style.width = '0';
      cell.style.padding = '0';
      cell.style.border = 'none';
    });

    if (table) {
      const allRows = table.querySelectorAll(`tr[data-group-name="${groupName}"][data-is-group-item="true"]`);
      allRows.forEach(r => {
        r.style.display = 'none';
      });
    }
  } else {
    if (icon) {
      icon.className = 'fas fa-chevron-down';
    }
    row.setAttribute('data-expanded', 'true');

    const detailCells = Array.from(row.querySelectorAll('.pod-detail-col'));
    detailCells.forEach(cell => {
      cell.innerHTML = '-';
      cell.style.visibility = '';
      cell.style.width = '';
      cell.style.padding = '';
      cell.style.border = '';
    });

    if (table) {
      const allRows = table.querySelectorAll(`tr[data-group-name="${groupName}"][data-is-group-item="true"]`);
      allRows.forEach(r => {
        r.style.display = '';
      });
    }
  }
}

function getPodsColumnSettings() {
  const defaultSettings = { visibility: {}, widths: {} };
  try {
    const raw = localStorage.getItem(PODS_COLUMN_SETTINGS_KEY);
    if (!raw) {
      return defaultSettings;
    }
    const parsed = JSON.parse(raw);
    return {
      visibility: parsed.visibility || {},
      widths: parsed.widths || {}
    };
  } catch (error) {
    console.warn('Не удалось загрузить настройки колонок подов:', error);
    return defaultSettings;
  }
}

function savePodsColumnSettings(settings) {
  localStorage.setItem(PODS_COLUMN_SETTINGS_KEY, JSON.stringify(settings));
}

function getHeaderMinWidth(headerCell) {
  if (!headerCell) {
    return 0;
  }

  const stored = parseInt(headerCell.dataset.minWidth || '', 10);
  if (!Number.isNaN(stored) && stored > 0) {
    return stored;
  }

  const label = headerCell.querySelector('.column-header-text');
  const labelRect = label ? label.getBoundingClientRect() : null;
  const labelWidth = labelRect ? labelRect.width : (headerCell.scrollWidth || 0);
  const styles = window.getComputedStyle(headerCell);
  const paddingLeft = parseFloat(styles.paddingLeft || '0');
  const paddingRight = parseFloat(styles.paddingRight || '0');
  const gap = 2;
  const measured = Math.ceil(labelWidth + paddingLeft + paddingRight + gap);
  const normalized = Math.max(60, measured);

  headerCell.dataset.minWidth = String(normalized);
  return normalized;
}

function applyColumnVisibility(table, columnKey, visible) {
  const cells = table.querySelectorAll(`[data-column-key="${columnKey}"]`);

  cells.forEach(cell => {
    if (visible) {
      cell.classList.remove('column-hidden');
    } else {
      cell.classList.add('column-hidden');
    }
  });
}

function applyColumnWidth(table, columnKey, width) {
  if (width === undefined || width === null) {
    return;
  }

  const numericWidth = typeof width === 'number' ? width : parseInt(width, 10);
  if (Number.isNaN(numericWidth) || numericWidth <= 0) {
    return;
  }

  const headerCell = table.querySelector(`thead th[data-column-key="${columnKey}"]`);
  const minWidth = Math.max(60, getHeaderMinWidth(headerCell));
  const finalWidth = Math.max(numericWidth, minWidth);
  const widthPx = `${finalWidth}px`;

  const cells = table.querySelectorAll(`[data-column-key="${columnKey}"]`);
  if (cells.length === 0) {
    return;
  }

  table.classList.add('pods-table-fixed');
  cells.forEach(cell => {
    cell.style.width = widthPx;
    cell.style.minWidth = widthPx;
    cell.style.maxWidth = widthPx;
  });
}

function clearColumnWidth(table, columnKey) {
  const cells = table.querySelectorAll(`[data-column-key="${columnKey}"]`);
  cells.forEach(cell => {
    cell.style.width = '';
    cell.style.minWidth = '';
    cell.style.maxWidth = '';
    delete cell.dataset.minWidth;
  });

  const headerCell = table.querySelector(`thead th[data-column-key="${columnKey}"]`);
  if (headerCell) {
    delete headerCell.dataset.minWidth;
  }
}

function ensureTableWidthLock(table, settings) {
  if (table.dataset.columnWidthsLocked === 'true') {
    return;
  }

  const headerCells = Array.from(table.querySelectorAll('thead th[data-column-key]'));

  headerCells.forEach(headerCell => {
    const columnKey = headerCell.dataset.columnKey;
    if (!columnKey) {
      return;
    }
    if (headerCell.classList.contains('column-hidden')) {
      return;
    }

    if (settings.widths && Object.prototype.hasOwnProperty.call(settings.widths, columnKey)) {
      applyColumnWidth(table, columnKey, settings.widths[columnKey]);
      return;
    }

    const rectWidth = headerCell.getBoundingClientRect().width || headerCell.offsetWidth;
    if (!rectWidth) {
      return;
    }

    const computedWidth = Math.round(rectWidth);
    const normalized = Math.max(60, getHeaderMinWidth(headerCell));
    const widthPx = `${Math.max(computedWidth, normalized)}px`;

    const cells = table.querySelectorAll(`[data-column-key="${columnKey}"]`);
    cells.forEach(cell => {
      cell.style.width = widthPx;
      cell.style.minWidth = widthPx;
      cell.style.maxWidth = widthPx;
    });
  });

  table.classList.add('pods-table-fixed');
  table.dataset.columnWidthsLocked = 'true';
}

function attachColumnResizer(table, headerCell, columnKey, settings) {
  if (!headerCell || !columnKey || headerCell.querySelector('.column-resizer')) {
    return;
  }

  const resizer = document.createElement('span');
  resizer.className = 'column-resizer';
  headerCell.appendChild(resizer);

  let startX = 0;
  let startWidth = 0;
  let lastWidth = 0;

  const onMouseMove = (event) => {
    const delta = event.clientX - startX;
    let newWidth = startWidth + delta;
    const headerMinWidth = Math.max(60, getHeaderMinWidth(headerCell));
    if (newWidth < headerMinWidth) {
      newWidth = headerMinWidth;
    }
    lastWidth = newWidth;
    applyColumnWidth(table, columnKey, newWidth);
  };

  const onMouseUp = () => {
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup', onMouseUp);
    document.body.classList.remove('column-resize-active');
    if (lastWidth > 0) {
      settings.widths[columnKey] = Math.round(lastWidth);
      savePodsColumnSettings(settings);
    }
  };

  resizer.addEventListener('mousedown', (event) => {
    event.preventDefault();
    event.stopPropagation();
    ensureTableWidthLock(table, settings);
    startX = event.clientX;
    startWidth = headerCell.offsetWidth;
    lastWidth = startWidth;
    document.body.classList.add('column-resize-active');
    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', onMouseUp);
  });
}

function initPodsColumnControls(table) {
  const headerCells = Array.from(table.querySelectorAll('thead th[data-column-key]'));
  if (headerCells.length === 0) {
    return;
  }

  const settings = getPodsColumnSettings();

  headerCells.forEach(headerCell => {
    const columnKey = headerCell.dataset.columnKey;
    if (!columnKey) {
      return;
    }

    const isVisible = Object.prototype.hasOwnProperty.call(settings.visibility, columnKey)
      ? settings.visibility[columnKey]
      : true;
    applyColumnVisibility(table, columnKey, isVisible);

    const storedWidth = settings.widths[columnKey];
    if (storedWidth) {
      applyColumnWidth(table, columnKey, storedWidth);
    }

    const checkbox = document.querySelector(`.column-toggle[data-column-key="${columnKey}"]`);
    if (checkbox) {
      checkbox.checked = isVisible;
    }

    attachColumnResizer(table, headerCell, columnKey, settings);
  });

  if (Object.keys(settings.widths || {}).length > 0) {
    ensureTableWidthLock(table, settings);
  }

  const toggles = document.querySelectorAll('.column-toggle[data-column-key]');
  toggles.forEach(checkbox => {
    if (checkbox.dataset.bound === 'true') {
      return;
    }
    checkbox.addEventListener('change', function () {
      const columnKey = this.dataset.columnKey;
      const visible = this.checked;
      applyColumnVisibility(table, columnKey, visible);
      settings.visibility[columnKey] = visible;
      savePodsColumnSettings(settings);
    });
    checkbox.dataset.bound = 'true';
  });

  const resetButton = document.getElementById('resetPodsColumns');
  if (resetButton && resetButton.dataset.bound !== 'true') {
    resetButton.addEventListener('click', () => {
      const checkboxes = document.querySelectorAll('.column-toggle[data-column-key]');
      checkboxes.forEach(checkbox => {
        checkbox.checked = true;
        const key = checkbox.dataset.columnKey;
        applyColumnVisibility(table, key, true);
        clearColumnWidth(table, key);
      });

      table.classList.remove('pods-table-fixed');
      delete table.dataset.columnWidthsLocked;

      settings.visibility = {};
      settings.widths = {};
      savePodsColumnSettings(settings);
    });
    resetButton.dataset.bound = 'true';
  }
}

function initPodsFeatures() {
  const table = document.getElementById('podsTable');
  if (!table) {
    return;
  }

  groupPods(table);
  initPodsFilter(table);
  initPodsColumnControls(table);
}

// Bootstrap everything ------------------------------------------------------
onDocumentReady(() => {
  initTheme();
  initSidebar();
  initServersFilter();
  initPodsFeatures();
});

// Expose globals for inline handlers ---------------------------------------
window.showNotification = showNotification;
window.deleteServer = deleteServer;
window.refreshServers = refreshServers;
window.refreshPods = refreshPods;
window.toggleTheme = toggleTheme;
window.openSidebar = openSidebar;
window.closeSidebar = closeSidebar;
window.toggleHealthcheck = toggleHealthcheck;
window.addServer = addServer;
window.togglePodGroup = togglePodGroup;
