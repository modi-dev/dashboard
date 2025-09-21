Дашборд с информацией о сборках.

1. Перед развертыванием необхожимо залогинится в нужный namespace k8s под ТУЗом deploy-sa или prometheus
2. Команда развертывания выполняется из папки Helm.
3. В команде развертыения необходимо актуализировать значения для <subo> <stand>, например, ./accr/values-ift.yaml
4. Deploy: helm upgrade -i -f ./values.yaml -f ./<subo>/values-<stand>.yaml ms-dashbord .