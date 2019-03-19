NAME = istio-in-action
VERSION = 1.0.6
GCP = gcloud
K8S = kubectl

.PHONY: info

info:
	@echo "Microservices on a Diet with Istio"

prepare:
	@$(GCP) config set compute/zone europe-west1-b
	@$(GCP) config set container/use_client_certificate False

cluster:
	@$(GCP) container clusters create $(NAME) --num-nodes=7 --enable-autoscaling --min-nodes=7 --max-nodes=10
	@$(K8S) create clusterrolebinding cluster-admin-binding --clusterrole=cluster-admin --user=$$(gcloud config get-value core/account)
	@$(K8S) create -f https://raw.githubusercontent.com/kubernetes/dashboard/master/aio/deploy/recommended/kubernetes-dashboard.yaml
	@$(K8S) cluster-info

get-istio:
	@curl -L https://git.io/getLatestIstio | sh -
	@echo "Make sure to export the PATH variable."

istio-install:
	# deploy Istio
	@$(K8S) apply -f istio-$(VERSION)/install/kubernetes/helm/istio/templates/crds.yaml
	@sleep 5
	@$(K8S) apply -f istio-$(VERSION)/install/kubernetes/istio-demo.yaml
	@sleep 5
	@$(K8S) get pods -n istio-system
	@$(K8S) create namespace istio-demo
	@$(K8S) label namespace istio-demo istio-injection=enabled
	@$(K8S) label namespace default istio-injection=enabled
	@$(K8S) get svc istio-ingressgateway -n istio-system

access-token:
	@$(GCP) config config-helper --format=json | jq .credential.access_token

dashboard:
	@$(K8S) proxy & 2>&1
	@sleep 3
	@$(GCP) config config-helper --format=json | jq .credential.access_token
	@open http://localhost:8001/api/v1/namespaces/kube-system/services/https:kubernetes-dashboard:/proxy/

grafana:
	@$(K8S) -n istio-system port-forward $$(kubectl -n istio-system get pod -l app=grafana -o jsonpath='{.items[0].metadata.name}') 8080:3000 & 2>&1
	@sleep 3
	@open http://localhost:8080/

prometheus:
	@$(K8S) -n istio-system port-forward $$(kubectl -n istio-system get pod -l app=prometheus -o jsonpath='{.items[0].metadata.name}') 8083:9090 & 2>&1
	@sleep 3
	@open http://localhost:8083/graph/

servicegraph:
	@$(K8S) -n istio-system port-forward $$(kubectl -n istio-system get pod -l app=servicegraph -o jsonpath='{.items[0].metadata.name}') 8082:8088 & 2>&1
	@sleep 3
	@open http://localhost:8082/force/forcegraph.html
	@open http://localhost:8082/dotviz

jaeger:
	@$(K8S) port-forward -n istio-system $$(kubectl get pod -n istio-system -l app=jaeger -o jsonpath='{.items[0].metadata.name}') 8084:16686 & 2>&1
	@sleep 3
	@open http://localhost:8084

kiali-install:
	@$(K8S) create -n istio-system -f showcases/kiali/kiali-configmap.yaml
	@$(K8S) create -n istio-system -f showcases/kiali/kiali-secrets.yaml
	@$(K8S) create -n istio-system -f showcases/kiali/kiali.yaml

hello-v1:
	@cd microservices/hello-istio-v1/ && ./gradlew ass
	@cd microservices/hello-istio-v1/ && docker-compose build
	@docker push lreimer/hello-istio:1.0.1

hello-v2:
	@cd microservices/hello-istio-v2/ && ./gradlew ass
	@cd microservices/hello-istio-v2/ && docker-compose build
	@docker push lreimer/hello-istio:2.0.1

alphabet:
	@cd microservices/alphabet-service/ && ./gradlew ass
	@cd microservices/alphabet-service/ && docker-compose build
	@docker push lreimer/alphabet-service:1.0.1

	@cd microservices/spelling-service/ && ./gradlew ass
	@cd microservices/spelling-service/ && docker-compose build
	@docker push lreimer/spelling-service:1.0.1

hello-demo:
	@$(K8S) apply -n istio-demo -f showcases/hello-istio/hello-istio.yaml
	@$(K8S) apply -n istio-demo -f showcases/hello-istio/hello-istio-gateway.yaml
	@$(K8S) apply -n istio-demo -f showcases/hello-istio/hello-istio-virtual-service.yaml
	@$(K8S) apply -n istio-demo -f showcases/hello-istio/hello-istio-destination.yaml

alphabet-demo:
	@$(K8S) apply -n istio-demo -f showcases/alphabet/spelling-service.yaml
	@$(K8S) apply -n istio-demo -f showcases/alphabet/spelling-gateway.yaml
	@$(K8S) apply -n istio-demo -f showcases/alphabet/spelling-service-virtual-service.yaml
	@$(K8S) apply -n istio-demo -f showcases/alphabet/spelling-service-destination.yaml

	@$(K8S) apply -n istio-demo -f showcases/alphabet/alphabet-service.yaml
	@$(K8S) apply -n istio-demo -f showcases/alphabet/alphabet-service-virtual-service.yaml
	@$(K8S) apply -n istio-demo -f showcases/alphabet/alphabet-service-destination.yaml

	@$(K8S) apply -n istio-demo -f showcases/alphabet/a-service.yaml
	@$(K8S) apply -n istio-demo -f showcases/alphabet/a-service-virtual-service.yaml
	@$(K8S) apply -n istio-demo -f showcases/alphabet/a-service-destination.yaml

	@$(K8S) apply -n istio-demo -f showcases/alphabet/b-service.yaml
	@$(K8S) apply -n istio-demo -f showcases/alphabet/b-service-virtual-service.yaml
	@$(K8S) apply -n istio-demo -f showcases/alphabet/b-service-destination.yaml

	@$(K8S) apply -n istio-demo -f showcases/alphabet/c-service.yaml
	@$(K8S) apply -n istio-demo -f showcases/alphabet/c-service-virtual-service.yaml
	@$(K8S) apply -n istio-demo -f showcases/alphabet/c-service-destination.yaml

clean:
	@$(K8S) delete --ignore-not-found=true -f istio-$(VERSION)/install/kubernetes/istio-demo.yaml
	@$(K8S) delete --ignore-not-found=true -f istio-$(VERSION)/install/kubernetes/helm/istio/templates/crds.yaml -n istio-system
	@rm -rf istio-$(VERSION)
	@$(GCP) container clusters delete $(NAME) --async --quiet
