# Putting Microservices on a Diet: with Istio.

The demo repository for different Istio showcases and the talk _Putting Microservices
on a diet with Istio._

## Istio in Action

### Step 1: Create Kubernetes cluster

In this first step we are going to create a Kubernetes cluster on GCP. Issue the
following command fire up the infrastructure:
```
$ make prepare cluster
```

### Step 2: Install Istio

In this step we are going to install the latest (1.0.2) version of Istio. We are
not going to install the mutual TLS version here. Also, we are labeling the `default`
namespace to perform the Istio sidecar injection automatically.

```
$ make istio-install
```

### Step 3: Hello Istio Showcase

In this first showcase are going to deploy two versions of the same microservice and
use different traffic management features to demonstrate the power and simplicity of Istio.

```
$ kubectl get svc istio-ingressgateway -n istio-system
$ export INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

# apply the basic Kubernetes primitives
$ kubectl apply -f showcases/hello-istio/hello-istio.yaml
$ kubectl apply -f showcases/hello-istio/hello-istio-destination.yaml
$ kubectl apply -f showcases/hello-istio/hello-istio-gateway.yaml
$ kubectl apply -f showcases/hello-istio/hello-istio-all.yaml

$ http get $INGRESS_HOST/api/hello

# apply the version specific virtual services
$ kubectl apply -f showcases/hello-istio/hello-istio-v1.yaml
$ http get $INGRESS_HOST/api/hello
$ kubectl apply -f showcases/hello-istio/hello-istio-v2.yaml
$ http get $INGRESS_HOST/api/hello

# apply the weighted or rule base virtual services
$ kubectl apply -f showcases/hello-istio/hello-istio-user-agent.yaml
$ http get $INGRESS_HOST/api/hello
$ kubectl apply -f showcases/hello-istio/hello-istio-70-30.yaml
$ http get $INGRESS_HOST/api/hello
```

### Step 4: Alphabet Showcase


### Step X: Delete Kubernetes cluster

Do not forget to shutdown everything, otherwise you will have a bad surprise on
your credit card bill at the end of the month!

```
$ make clean
```

## Maintainer

M.-Leander Reimer (@lreimer), <mario-leander.reimer@qaware.de>

## License

This software is provided under the MIT open source license, read the `LICENSE`
file for details.
