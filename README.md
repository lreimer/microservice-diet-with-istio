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
$ make get-istio
$ make istio-install
```

### Step 3: Hello Istio Showcase

In this first showcase are going to deploy two versions of the same microservice and
use different traffic management features to demonstrate the power and simplicity of Istio.

```
$ kubectl get svc istio-ingressgateway -n istio-system
$ export INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

$ echo $INGRESS_HOST hello-istio.cloud >> /etc/hosts

$ make hello-demo

$ http get hello-istio.cloud/api/hello
$ watch -n 1 -d http get hello-istio.cloud/api/hello

# apply the version specific virtual services
$ kubectl apply -n istio-demo -f showcases/hello-istio/hello-istio-v1.yaml
$ http get hello-istio.cloud/api/hello

$ kubectl apply -n istio-demo -f showcases/hello-istio/hello-istio-v2.yaml
$ http get hello-istio.cloud/api/hello

$ kubectl apply -n istio-demo -f showcases/hello-istio/hello-istio-v1.yaml
$ kubectl apply -n istio-demo -f showcases/hello-istio/hello-istio-75-25.yaml
$ kubectl apply -n istio-demo -f showcases/hello-istio/hello-istio-50-50.yaml
$ kubectl apply -n istio-demo -f showcases/hello-istio/hello-istio-25-75.yaml
$ kubectl apply -n istio-demo -f showcases/hello-istio/hello-istio-v2.yaml

$ kubectl apply -n istio-demo -f showcases/hello-istio/hello-istio-user-agent.yaml
$ http get hello-istio.cloud/api/hello User-Agent:Chrome

$ kubectl apply -n istio-demo -f showcases/hello-istio/hello-istio-user-cookie.yaml
$ http get hello-istio.cloud/api/hello Cookie:user=oreilly
```

### Step 4: Alphabet Showcase

This showcase demonstrates more advances features like introducing delays,
failure and circuit breakers.

```
$ export INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

$ echo $INGRESS_HOST spelling.cloud >> /etc/hosts

$ make alphabet-demo

$ http get spelling.cloud/api/spelling\?word=abc
$ http get spelling.cloud/api/spelling\?word=hello

$ http get spelling.cloud/api/spelling\?word=abc Accept-Language:de
$ http get spelling.cloud/api/spelling\?word=hello Accept-Language:de

$ kubectl apply -n istio-demo -f showcases/alphabet/alphabet-service-delay.yaml
$ http get spelling.cloud/api/spelling\?word=hello

$ kubectl apply -n istio-demo -f showcases/alphabet/alphabet-service-fault.yaml
$ http get spelling.cloud/api/spelling\?word=hello
```

### Step 5: Diagnosability

As part of Istio there are a few diagnosability features included: logging,
monitoring, tracing and service graphs.

```
$ make prometheus
$ make grafana
$ make servicegraph
$ make jaeger
```

### Step X: Delete Kubernetes cluster

Do not forget to shutdown everything, otherwise you will have a bad surprise on
your credit card bill at the end of the month!

```
$ make clean
```

## References

- https://istio.io
- https://www.kiali.io
- https://conferences.oreilly.com/software-architecture/sa-eu/public/schedule/detail/70784

## Maintainer

M.-Leander Reimer (@lreimer), <mario-leander.reimer@qaware.de>

## License

This software is provided under the MIT open source license, read the `LICENSE`
file for details.
