Дашборд с информацией о сборках.

1. Перед развертыванием необхожимо залогинится в нужный namespace k8s под ТУЗом deploy-sa (у него есть секрет до хранилища с имаджем).
2. Команда развертывания выполняется из папки Helm.
3. В команде развертыения необходимо актуализировать тэг имаджа (main.tag).
4. Примеры команды для разных СУБО (отличаются только каталогом для values)

helm upgrade -i -f ./values.yaml -f ./accr/values-dev.yaml --set main.image=accr/ms-dashbord --set main.tag=sfera-v2 ms-dashbord .
helm upgrade -i -f ./values.yaml -f ./cole/values-dev.yaml --set main.image=cole/ms-dashbord --set main.tag=sfera-v2 ms-dashbord .
helm upgrade -i -f ./values.yaml -f ./dcos/values-dev.yaml --set main.image=dcos/ms-dashbord --set main.tag=sfera-v2 ms-dashbord .
helm upgrade -i -f ./values.yaml -f ./onb/values-dev.yaml --set main.image=smeo/ms-dashbord --set main.tag=sfera-v2 ms-dashbord .
helm upgrade -i -f ./values.yaml -f ./smep/values-dev.yaml --set main.image=smep/ms-dashbord --set main.tag=sfera-v2 ms-dashbord .