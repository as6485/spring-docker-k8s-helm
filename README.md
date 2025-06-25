
# spring-docker-k8s-helm

Simple Hello World Application to demo Spring Boot + Docker + Kubernetes + Helm in local (Windows)

## Prerequisites

Install Docker Desktop
1. https://docs.docker.com/desktop/setup/install/windows-install/
2. Ensure Docker Desktop is up and running. We can check in command line as well (no errors)
```
docker info
```

Install minikube

1. https://minikube.sigs.k8s.io/docs/start/?arch=%2Fwindows%2Fx86-64%2Fstable%2F.exe+download
2. Ensure it is started. After it starts successfully there will be a conatiner named "minikube" running in Docker Desktop.
```
> minikube version
minikube version: v1.36.0
commit: f8f52f5de11fc6ad8244afac475e1d0f96841df1-dirty

> minikube status
* Profile "minikube" not found. Run "minikube profile list" to view all profiles.
  To start a cluster, run: "minikube start"

> minikube start --driver=docker
* minikube v1.36.0 on Microsoft Windows 11 Home Single Language 10.0.26100.4349 Build 26100.4349
* Using the docker driver based on user configuration
* Using Docker Desktop driver with root privileges
* Starting "minikube" primary control-plane node in "minikube" cluster
* Pulling base image v0.0.47 ...
* Downloading Kubernetes v1.33.1 preload ...
    > preloaded-images-k8s-v18-v1...:  347.04 MiB / 347.04 MiB  100.00% 8.85 Mi
    > gcr.io/k8s-minikube/kicbase...:  502.26 MiB / 502.26 MiB  100.00% 10.07 M
* Creating docker container (CPUs=2, Memory=4000MB) ...
! Failing to connect to https://registry.k8s.io/ from inside the minikube container
* To pull new external images, you may need to configure a proxy: https://minikube.sigs.k8s.io/docs/reference/networking/proxy/
* Preparing Kubernetes v1.33.1 on Docker 28.1.1 ...
  - Generating certificates and keys ...
  - Booting up control plane ...
  - Configuring RBAC rules ...
* Configuring bridge CNI (Container Networking Interface) ...
* Verifying Kubernetes components...
  - Using image gcr.io/k8s-minikube/storage-provisioner:v5
* Enabled addons: storage-provisioner, default-storageclass
* Done! kubectl is now configured to use "minikube" cluster and "default" namespace by default

> minikube status
minikube
type: Control Plane
host: Running
kubelet: Running
apiserver: Running
kubeconfig: Configured
```
## Stage 1: Spring Boot + Docker
In this stage we will create a spring boot app and dockerize it.

1. Add Dockerfile at project root
```
FROM openjdk:17-jdk-slim
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```
2. Build an docker image
```
docker build -t spring-docker-k8s-helm-image .
```
3. Check if the image is built successfully
```
> docker images
REPOSITORY                     TAG       IMAGE ID       CREATED         SIZE
spring-docker-k8s-helm-image   latest    0fd5efd961a5   3 minutes ago   682MB

```
4. Run the image. Maps port 9090 of the host machine to port 8080 inside the container
```
docker run -p 9090:8080 spring-docker-k8s-helm-image
```
5. Access the app at http://localhost:9090/hello
```
2025-06-25T14:46:28.075Z  INFO 1 --- [spring-docker-k8s-helm] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
2025-06-25T14:46:28.098Z  INFO 1 --- [spring-docker-k8s-helm] [           main] o.a.s.SpringDockerK8sHelmApplication     : Started SpringDockerK8sHelmApplication in 2.193 seconds (process running for 2.882)
```
## Stage 2: Spring Boot + Kubernetes

1. To use the docker inside minikube (not the local docker) point the shell to use minikube docker
```
> docker images
REPOSITORY                     TAG       IMAGE ID       CREATED         SIZE
spring-docker-k8s-helm-image   latest    0fd5efd961a5   2 hours ago     682MB
mongo                          latest    98028cf281bb   3 weeks ago     1.2GB
gcr.io/k8s-minikube/kicbase    v0.0.47   8311be96a0a8   4 weeks ago     1.86GB
gcr.io/k8s-minikube/kicbase    <none>    6ed579c9292b   4 weeks ago     1.86GB
mongo-express                  latest    1b23d7976f02   15 months ago   286MB

> minikube docker-env
SET DOCKER_TLS_VERIFY=1
SET DOCKER_HOST=tcp://127.0.0.1:51224
SET DOCKER_CERT_PATH=C:\Users\urmis\.minikube\certs
SET MINIKUBE_ACTIVE_DOCKERD=minikube
REM To point your shell to minikube's docker-daemon, run:
REM @FOR /f "tokens=*" %i IN ('minikube -p minikube docker-env --shell cmd') DO @%i

> @FOR /f "tokens=*" %i IN ('minikube -p minikube docker-env --shell cmd') DO @%i

>docker images
REPOSITORY                                TAG        IMAGE ID       CREATED         SIZE
registry.k8s.io/kube-apiserver            v1.33.1    c6ab243b29f8   5 weeks ago     102MB
registry.k8s.io/kube-controller-manager   v1.33.1    ef43894fa110   5 weeks ago     94.6MB
registry.k8s.io/kube-scheduler            v1.33.1    398c985c0d95   5 weeks ago     73.4MB
registry.k8s.io/kube-proxy                v1.33.1    b79c189b052c   5 weeks ago     97.9MB
registry.k8s.io/etcd                      3.5.21-0   499038711c08   2 months ago    153MB
registry.k8s.io/coredns/coredns           v1.12.0    1cf5f116067c   7 months ago    70.1MB
registry.k8s.io/pause                     3.10       873ed7510279   13 months ago   736kB
gcr.io/k8s-minikube/storage-provisioner   v5         6e38f40d628d   4 years ago     31.5MB
```

2. Goto project directory where the Dockerfile resides and build an docker image (this time inside k8s)
```
> docker build -t spring-docker-k8s-helm-image2 .

> docker images
REPOSITORY                                TAG        IMAGE ID       CREATED         SIZE
spring-docker-k8s-helm-image2             latest     b54b066a7cf1   2 minutes ago   429MB
```

3. Create a deployment.yaml. Ensure imagePullPolicy is set to Never
```
>kubectl create deployment spring-docker-k8s-helm --image spring-docker-k8s-helm-image2:latest -o yaml --port=8080 --dry-run=client > deployment.yaml
```

4. Apply the deployment
```
> kubectl apply -f deployment.yaml
deployment.apps/spring-docker-k8s-helm configured
```

5. Expose the deployment outside the cluster through a Nodeport
```
> kubectl expose deployment spring-docker-k8s-helm --type=NodePort --name=spring-docker-k8s-helm-nodeport-service
service/spring-docker-k8s-helm-nodeport-service exposed

>kubectl describe services spring-docker-k8s-helm-nodeport-service
Name:                     spring-docker-k8s-helm-nodeport-service
Namespace:                default
Labels:                   app=spring-docker-k8s-helm
Annotations:              <none>
Selector:                 app=spring-docker-k8s-helm
Type:                     NodePort
IP Family Policy:         SingleStack
IP Families:              IPv4
IP:                       10.98.252.169
IPs:                      10.98.252.169
Port:                     <unset>  8080/TCP
TargetPort:               8080/TCP
NodePort:                 <unset>  30813/TCP
Endpoints:                10.244.0.4:8080
Session Affinity:         None
External Traffic Policy:  Cluster
Internal Traffic Policy:  Cluster
Events:                   <none>
```

6. Since we are running on minikube we can get the url to access this NodePort service
```
> minikube service spring-docker-k8s-helm-nodeport-service --url
http://127.0.0.1:52801
! Because you are using a Docker driver on windows, the terminal needs to be open to run it.
```

7. Verify all
```
> kubectl get all
NAME                                          READY   STATUS    RESTARTS   AGE
pod/spring-docker-k8s-helm-6d6d8f4bfb-l2dhz   1/1     Running   0          20m

NAME                                              TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
service/kubernetes                                ClusterIP   10.96.0.1       <none>        443/TCP          128m
service/spring-docker-k8s-helm-nodeport-service   NodePort    10.98.252.169   <none>        8080:30813/TCP   33s

NAME                                     READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/spring-docker-k8s-helm   1/1     1            1           22m

NAME                                                DESIRED   CURRENT   READY   AGE
replicaset.apps/spring-docker-k8s-helm-6d6d8f4bfb   1         1         1       20m
replicaset.apps/spring-docker-k8s-helm-9f65bf679    0         0         0       22m
```


8. Shutdown
```
> kubectl delete service --all
> kubectl delete deployment --all
> minikube stop
```
## Authors

- [@as6485](https://www.github.com/as6485)


## Acknowledgements

1. [Deploy Your Spring Boot App on Kubernetes Cluster(K8s) | Spring Boot | Docker | Kubernetes Cluster](https://www.youtube.com/watch?v=zudtloZ8c9w)

2. [Deploy Spring Boot App on Kubernetes cluster (minikube) using Helm Chart - Kubernetes tutorial](https://www.youtube.com/watch?v=M6ZavWvKlcc)

3. [Minikube and Kubectl explained | Setup for Beginners | Kubernetes Tutorial 17](https://www.youtube.com/watch?v=E2pP1MOfo3g)

4. [Kubernetes Crash Course: Learn the Basics and Build a Microservice Application](https://www.youtube.com/watch?v=XuSQU5Grv1g)

5. [Spring on Kubernetes](https://spring.io/guides/topicals/spring-on-kubernetes)