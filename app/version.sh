#!/bin/bash

# каталог с html страницей и клиентом openshift
html_dir="/app"
html_file="index.html"
oc_dir="/app"

# собираем HTML
## выбираем стиль 
html="<style>"
html="${html}/* Стили таблицы (IKSWEB) */"
html="${html}table.iksweb{text-decoration: none;border-collapse:collapse;width:100%;text-align:center;}"
html="${html}table.iksweb th{font-weight:normal;font-size:14px; color:#ffffff;background-color:#354251;}"
html="${html}table.iksweb td{font-size:14px;color:#354251;}"
html="${html}table.iksweb td,table.iksweb th{white-space:pre-wrap;padding:10px 5px;line-height:13px;vertical-align: middle;border: 1px solid #354251;}  table.iksweb tr:hover{background-color:#f9fafb}"
html="${html}table.iksweb tr:hover td{color:#354251;cursor:default;}"
html="${html}</style>"

## получаем текущий namespace в OC и текущую даты
namespace=$(${oc_dir}/oc get pods -o jsonpath="{.items[0].metadata.namespace}")
date="$(date +'%Y-%m-%d %H:%M:%S') UTC"

## запрос данных из опеншифт
table=$(${oc_dir}/oc get pods -o jsonpath="
{range .items[?(@.metadata.labels.app)]}
{'<tr>'}
    {'<td align="left">'}{.metadata.labels.app}{'</td>'}{'<td align="left">'}{..containers[?(@.name == 'main')].image}{\" \"}{'</td>'}{'<td>'}{..metadata.annotations.ms\-branch}{\" \"}{'</td>'}{'<td>'}{..metadata.annotations.config\-branch}{\" \"}{'</td>'}{'<td>'}{.metadata.creationTimestamp}{\" \"}{'</br>'}{'</td>'}{'<td align="left">'}{..containers[?(@.name == 'main')].ports[0].containerPort} {'</td>'} {'<td align="left">'}{'CPU: '}{..containers[?(@.name == 'main')].resources.requests.cpu}{'</br>'}{'</br>'}{'RAM: '} {..containers[?(@.name == 'main')].resources.requests.memory}{'</td>'}
{'</tr>'}
{end}
" | grep -vE istio\|dashbord\|stub\|devtool\|hello\|devtool\|kafka\|tyk\|mrp)

sed_table=$(echo $table | sed 's/nexus[^:]*://g')

## добавляем время, HTML разметку и заголовок таблицы
html="${html}<html><body><p style="font-size:12px" > update time: ${date}<br>namespace: ${namespace}</p><br><table class='iksweb'><thead><tr><th>NAME</th><th>VERSION</th><th>MsBranch</th><th>ConfigBranch</th><th>CREATION DATE</th><th>PORT</th><th>REQUEST</th></tr></thead>${sed_table}</table></html>"

# выгружаем html
> $html_dir/$html_file
echo $html >> $html_dir/$html_file