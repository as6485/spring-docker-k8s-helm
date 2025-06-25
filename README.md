
# spring-docker-k8s-helm

Simple Hello World Application to demo Spring Boot + Docker + Kubernetes + Helm in local (Windows)





## Prerequisites

Install Docker Desktop
1. https://docs.docker.com/desktop/setup/install/windows-install/
2. Ensure it is up and running

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
## Authors

- [@as6485](https://www.github.com/as6485)

