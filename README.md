
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


Install Helm
1. Get Helm from https://github.com/helm/helm/releases and keep at local somewhere
2. Add Enviorment Variable PATH to this location
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

5. Expose the deployment outside the cluster through a Nodeport. Refer https://minikube.sigs.k8s.io/docs/handbook/accessing/ 
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

7. Access app at http://127.0.0.1:52801

8. Verify all
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


9. Shutdown
```
> kubectl delete service --all
> kubectl delete deployment --all
> minikube stop
```
## Stage 3: Spring Boot + Helm chart


Overall configuration

ayan.org --> [Ingress --> Service (ClusterIP)  --> POD1, POD2]

1. Create Helm Directory
```
src\main\resources\helm> helm create spring-docker-k8s-helm
Creating spring-docker-k8s-helm

\src\main\resources\helm> tree spring-docker-k8s-helm
Folder PATH listing for volume OS
Volume serial number is EA32-0916
C:\USERS\URMIS\IDEAPROJECTS\SPRING-DOCKER-K8S-HELM\SRC\MAIN\RESOURCES\HELM\SPRING-DOCKER-K8S-HELM
├───charts
└───templates
    └───tests
```

2. Make necessary edits to image.repository, service.type, service.port,
   Remove liveliness and readiness probe from deployment.yaml
   Refer \src\main\resources\helm for details

3. Deploy using Helm
```
src\main\resources\helm> helm install release-1 .\spring-docker-k8s-helm\    
NAME: release-1
LAST DEPLOYED: Thu Jun 26 14:44:59 2025
NAMESPACE: default
STATUS: deployed
REVISION: 1
NOTES:
1. Get the application URL by running these commands:
  export NODE_PORT=$(kubectl get --namespace default -o jsonpath="{.spec.ports[0].nodePort}" services release-1-spring-docker-k8s-helm)
  export NODE_IP=$(kubectl get nodes --namespace default -o jsonpath="{.items[0].status.addresses[0].address}")
  echo http://$NODE_IP:$NODE_PORT


src\main\resources\helm> kubectl get all
NAME                                                        READY   STATUS    RESTARTS   AGE
pod/release-1-spring-docker-k8s-helm-5586786d5d-tlfts       1/1     Running   0          96s
pod/release-1.0.0-spring-docker-k8s-helm-56d64577b6-d86cb   1/1     Running   0          2m12s

NAME                                       TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
service/kubernetes                         ClusterIP   10.96.0.1        <none>        443/TCP          14h
service/release-1-spring-docker-k8s-helm   NodePort    10.110.152.228   <none>        8080:30959/TCP   96s

NAME                                                   READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/release-1-spring-docker-k8s-helm       1/1     1            1           96s
deployment.apps/release-1.0.0-spring-docker-k8s-helm   1/1     1            1           2m12s

NAME                                                              DESIRED   CURRENT   READY   AGE
replicaset.apps/release-1-spring-docker-k8s-helm-5586786d5d       1         1         1       96s
replicaset.apps/release-1.0.0-spring-docker-k8s-helm-56d64577b6   1         1         1       2m12s

```

4. Since we are running on minikube we can get the url to access this NodePort service
```
src\main\resources\helm> minikube service release-1-spring-docker-k8s-helm --url
http://127.0.0.1:57923
! Because you are using a Docker driver on windows, the terminal needs to be open to run it.
```

5. Enable Ingress Addon
```
src\main\resources\helm> minikube addons enable ingress
* ingress is an addon maintained by Kubernetes. For any concerns contact minikube on GitHub.
You can view the list of minikube maintainers at: https://github.com/kubernetes/minikube/blob/master/OWNERS
* After the addon is enabled, please run "minikube tunnel" and your ingress resources would be available at "127.0.0.1"
  - Using image registry.k8s.io/ingress-nginx/kube-webhook-certgen:v1.5.3
  - Using image registry.k8s.io/ingress-nginx/controller:v1.12.2
  - Using image registry.k8s.io/ingress-nginx/kube-webhook-certgen:v1.5.3
* Verifying ingress addon...
* The 'ingress' addon is enabled

Verify ...
src\main\resources\helm> kubectl get pods -n ingress-nginx
NAME                                       READY   STATUS      RESTARTS   AGE
ingress-nginx-admission-create-2t8sh       0/1     Completed   0          110s
ingress-nginx-admission-patch-tnx82        0/1     Completed   0          110s
ingress-nginx-controller-67c5cb88f-w7xdj   1/1     Running     0          110s

```

6. Enable Ingress and custom domain in Helm
```
ingress:
  enabled: true
  className: ""
  annotations: {}
    # kubernetes.io/ingress.class: nginx
    # kubernetes.io/tls-acme: "true"
  hosts:
    - host: ayan.org
      paths:
        - path: /
          pathType: Prefix
```

7. Change service.type=ClusterIP
8. Change the version of the chart to 0.1.1
9. Upgrade the helm deployment
```
src\main\resources\helm> helm upgrade release-1 .\spring-docker-k8s-helm\
Release "release-1" has been upgraded. Happy Helming!
NAME: release-1
LAST DEPLOYED: Thu Jun 26 15:22:00 2025
NAMESPACE: default
STATUS: deployed
REVISION: 2
NOTES:
1. Get the application URL by running these commands:
  http://ayan.org/


src\main\resources\helm> kubectl get ingress
NAME                               CLASS   HOSTS      ADDRESS        PORTS   AGE
release-1-spring-docker-k8s-helm   nginx   ayan.org   192.168.49.2   80      61s

```

10. Edit C:\Windows\System32\drivers\etc\hosts file
```
# Added custom domain for spring-docker-k8s-helm project
127.0.0.1 ayan.org
```

11. Start the tunnel so that ingress resources would be available at "127.0.0.1"
```
PS C:\Users\urmis\IdeaProjects\spring-docker-k8s-helm\src\main\resources\helm> minikube tunnel
* Tunnel successfully started

* NOTE: Please do not close this terminal as this process must stay alive for the tunnel to be accessible ...

! Access to ports below 1024 may fail on Windows with OpenSSH clients older than v8.1. For more information, see: https://minikube.sigs.k8s.io/docs/handbook/accessing/#access-to-ports-1024-on-windows-requires-root-permission
* Starting tunnel for service release-1-spring-docker-k8s-helm.
```

12. Access app at https://ayan.org/hello

13. Check how the Helm template is exactly rendered
```
src\main\resources\helm> helm template release-1 .\spring-docker-k8s-helm\
```
14. Shutdown
```
> helm uninstall release-1    
release "release-1" uninstalled
PS C:\Users\urmis\IdeaProjects\spring-docker-k8s-helm\src\main\resources\helm> kubectl get all
NAME                 TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)   AGE
service/kubernetes   ClusterIP   10.96.0.1    <none>        443/TCP   15h
```


## Authors

- [@as6485](https://www.github.com/as6485)


## Acknowledgements

1. [Deploy Your Spring Boot App on Kubernetes Cluster(K8s) | Spring Boot | Docker | Kubernetes Cluster](https://www.youtube.com/watch?v=zudtloZ8c9w)

2. [Deploy Spring Boot App on Kubernetes cluster (minikube) using Helm Chart - Kubernetes tutorial](https://www.youtube.com/watch?v=M6ZavWvKlcc)

3. [Minikube and Kubectl explained | Setup for Beginners | Kubernetes Tutorial 17](https://www.youtube.com/watch?v=E2pP1MOfo3g)

4. [Kubernetes Crash Course: Learn the Basics and Build a Microservice Application](https://www.youtube.com/watch?v=XuSQU5Grv1g)

5. [Spring on Kubernetes](https://spring.io/guides/topicals/spring-on-kubernetes)
