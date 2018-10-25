NAME = istio-in-action
VERSION = 1.0.2
GCP = gcloud
K8S = kubectl

.PHONY: info

info:
	@echo "Microservices on a Diet with Istio"

prepare:
	@$(GCP) config set compute/zone europe-west1-b
	@$(GCP) config set container/use_client_certificate False

cluster:
	@$(GCP) container clusters create $(NAME) --num-nodes=5 --enable-autoscaling --min-nodes=5 --max-nodes=7
	@$(K8S) cluster-info

install:
	@curl -L https://git.io/getLatestIstio | sh -
	@export PATH=$PWD/istio-$(VERSION)/bin:$PATH
	@istioctl version

	# deploy Istio
	@$(K8S) create clusterrolebinding cluster-admin-binding --clusterrole=cluster-admin --user=$(gcloud config get-value core/account)
	@$(K8S) apply -f istio-$(VERSION)/install/kubernetes/helm/istio/templates/crds.yaml
	@sleep 5
	@$(K8S) apply -f istio-$(VERSION)/install/kubernetes/istio-demo.yaml
	@sleep 5
	@$(K8S) get pods -n istio-system
	@$(K8S) label namespace default istio-injection=enabled

access-token:
	@$(GCP) config config-helper --format=json | jq .credential.access_token

dashboard:
	@$(K8S) proxy & 2>&1
	@sleep 3
	@$(GCP) config config-helper --format=json | jq .credential.access_token
	@open http://localhost:8001/api/v1/namespaces/kube-system/services/https:kubernetes-dashboard:/proxy/

clean:
	@$(K8S) delete -f istio-$(VERSION)/install/kubernetes/istio-demo.yaml
	@$(K8S) delete -f istio-$(VERSION)/install/kubernetes/helm/istio/templates/crds.yaml -n istio-system
	@rm -rf istio-$(VERSION)
	@$(GCP) container clusters delete $(NAME) --async --quiet
