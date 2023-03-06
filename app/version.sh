#!/bin/bash

# каталог с html страницей
html_dir="/app"
html_file="index.html"
oc_dir="/app"

# собираем HTML
## выбираем стиль 
html="<style>"
html="${html}/* Стили таблицы (IKSWEB) */"
html="${html}table.iksweb{text-decoration: none;border-collapse:collapse;width:100%;text-align:center;}"
html="${html}table.iksweb th{font-weight:normal;font-size:14px; color:#ffffff;background-color:#354251;}"
html="${html}table.iksweb td{font-size:13px;color:#354251;}"
html="${html}table.iksweb td,table.iksweb th{white-space:pre-wrap;padding:10px 5px;line-height:13px;vertical-align: middle;border: 1px solid #354251;}	table.iksweb tr:hover{background-color:#f9fafb}"
html="${html}table.iksweb tr:hover td{color:#354251;cursor:default;}"
html="${html}</style>"

## получаем текущий namespace в OC и текущую даты
namespace=$(${oc_dir}/oc get pods -o jsonpath="{.items[0].metadata.namespace}")
date="$(date +'%Y-%m-%d %H:%M:%S') UTC"

## запрос данных из опеншифт
table=$(oc get pods -o jsonpath="{range .items[*]}{'<tr>'}{'<td align="left">'}{.metadata.labels.app}{'</td>'}{'<td align="left">'}{.spec.containers[?(@.name=='main')].image}{'</td>'}{'<td>'}{.metadata.creationTimestamp}{'</td>'}{'<td align="left">'}{range .spec.containers[*]}{.name}{'</br>'}{'req.cpu: '}{.resources.requests.cpu}{'|____|'}{'lim.cpu: '}{.resources.limits.cpu}{'</br>'}{'req.mem: '}{.resources.requests.memory}{'|____|'}{'lim.mem: '}{.resources.limits.memory}{'</br>'}{end}{'</td>'}{'<td>'}{.spec.containers[0].ports[0].containerPort}{'</td>'}{'<td>'}{.status.phase}{'</td>'}{'</tr>'}{end}")
## добавляем время и HTML разметку и заголовок таблицы
html="${html}<html><body><p>update time: ${date}<br>namespace: ${namespace}</p><br><table class='iksweb'><thead><tr><th>NAME</th><th>VERSION</th><th>CREATION DATE</th><th>LIMITS</th><th>PORTS</th><th>STATUS</th></tr></thead>${table}</table></html>"

# выгружаем html
> $html_dir/$html_file
echo $html >> $html_dir/$html_file