#!/bin/bash

# каталог с html страницей
html_dir="../dist"
html_file="index.html"

if [ -e $html_dir ]
then
	# проверка существования файла
	if [ -e $html_dir/$html_file ]
	then
		# удалим файл, если был.
	rm $html_dir/$html_file
	fi
else
	mkdir $html_dir
fi

# получаем текущий namespace в OC и текущую даты
namespace=$(oc project | grep -oP '"(.*?)" ')
date="$(date +'%Y-%m-%d %H:%M:%S') UTC"

# собираем HTML
## выбираем стиль 
echo "<style>
	/* Стили таблицы (IKSWEB) */
	table.iksweb{text-decoration: none;border-collapse:collapse;width:100%;text-align:center;}
	table.iksweb th{font-weight:normal;font-size:14px; color:#ffffff;background-color:#354251;}
	table.iksweb td{font-size:13px;color:#354251;}
	table.iksweb td,table.iksweb th{white-space:pre-wrap;padding:10px 5px;line-height:13px;vertical-align: middle;border: 1px solid #354251;}	table.iksweb tr:hover{background-color:#f9fafb}
	table.iksweb tr:hover td{color:#354251;cursor:default;}
</style>"  >> $html_dir/$html_file

## запрос данных из опеншифт
html=$(oc get pods -o jsonpath="{range .items[*]}{'<tr>'}{'<td align="left">'}{.metadata.labels.app}{'</td>'}{'<td align="left">'}{.spec.containers[0].image}{'</td>'}{'<td>'}{.spec.containers[0].ports[0].containerPort}{'</td>'}{'<td>'}{.status.phase}{'</td>'}{'</tr>'}{end}")
## добавляем время и HTML разметку и заголовок таблицы
html="<html><body><p>update time: ${date}<br>namespace: ${namespace}</p><br><table class='iksweb'><thead><tr><th>NAME</th><th>VERSION</th><th>PORTS</th><th>STATUS</th></tr></thead>${html}</table></html>"

# выгружаем html
echo $html >> $html_dir/$html_file

