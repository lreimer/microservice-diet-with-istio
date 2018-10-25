# Putting Microservices on a Diet: with Istio.

The demo repository for different Istio showcases and the talk _Putting Microservices
on a diet with Istio._

## Prerequisites

## Istio in Action

### 1. Create Kubernetes cluster

In this first step we are going to create a Kubernetes cluster on GCP. Issue the
following command fire up the infrastructure:
```
$ make cluster
```

### 2. Install istio

```
$ make install
```

### 9: Delete Kubernetes cluster

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
