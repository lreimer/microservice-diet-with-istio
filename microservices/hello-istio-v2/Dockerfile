FROM qaware/zulu-centos-payara-micro:8u181-5.183

CMD ["--noCluster", "--deploymentDir", "/opt/payara/deployments"]

COPY build/libs/hello-istio-v2.war /opt/payara/deployments/
