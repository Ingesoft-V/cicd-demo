
# CICD-DEMO — Taller CI/CD SE5

Proyecto de demostración de CI/CD para el taller de Ingeniería de Software 5.

## Pipeline CI/CD (Taller SE5)

### Infraestructura

| Servicio   | URL                        | Descripción                     |
|------------|----------------------------|---------------------------------|
| Jenkins    | http://localhost:8080      | Servidor de CI/CD               |
| SonarQube  | http://localhost:9000      | Análisis estático de código     |
| App        | http://localhost:80        | Aplicación desplegada           |

Levantar la infraestructura:
```bash
docker-compose up -d jenkins sonarqube
```

### Flujo del Pipeline (7 etapas)

```
Checkout → Build & Test → SonarQube → Quality Gate → Docker Build → Trivy → Deploy
```

1. **Checkout**: obtiene el código desde el repositorio
2. **Build & Test**: compila con Maven y ejecuta pruebas unitarias (excluye Selenium)
3. **Static Analysis (SonarQube)**: analiza calidad y seguridad del código
4. **Quality Gate**: falla el pipeline si SonarQube detecta hotspots de seguridad sin revisar
5. **Docker Build**: construye la imagen `mi-app:latest`
6. **Container Security Scan (Trivy)**: escanea la imagen buscando CVEs CRITICAL con fix disponible
7. **Deploy**: despliega el contenedor en el puerto 80 (solo en rama `master`)

### Puertas de calidad (Gatekeeping)

- **SonarQube**: el pipeline falla si hay Security Hotspots sin revisar o condiciones ERROR
- **Trivy**: el pipeline falla si hay vulnerabilidades CRITICAL con fix disponible en la imagen Docker

### Cambios realizados al código base

- `Dockerfile`: imagen base actualizada a `eclipse-temurin:11-jre`
- `pom.xml`: jacoco `0.8.11`, surefire `3.2.5` para compatibilidad con Java 11
- `SeleniumExampleTest.java`: anotación `@Ignore` para evitar fallos sin Selenium hub
- `DemoController.java`: controlador con deuda técnica intencional (detectada por SonarQube)
- `docker-compose.yml`: servicios Jenkins y SonarQube en red `cicd-network`

---

This project aims to be the basic skeleton to apply continuous integration and continuous delivery.

## Topology

CICD Demo uses some kubernetes primitives to deploy:

* Deployment
* Services
* Ingress ( with TLS )

```bash
     internet
        |
   [ Ingress ]
   --|-----|--
   [ Services ]
   --|-----|--
   [   Pods   ]

```

This project includes:

* Spring Boot java app
* Jenkinsfile integration to run pipelines
* Dockerfile containing the base image to run java apps
* Makefile and docker-compose to make the pipeline steps much simpler
* Kubernetes deployment file demonstrating how to deploy this app in a simple Kubernetes cluster

## Pipeline Setup

Pipelines exist at Travis.

Some pipelines are configured by **GitHub/Projects**. If you have created a repository in one of these, your project will be **automatically** built if it has a Jenkinsfile/Travis/Gitlab/CircleCI.

Other pipelines are configured manually under folders. You can create a project manually with the following steps:

How to run the app:

```make
make
```

## Testing

Unit tests and integrations tests are separated using [JUnit Categories][].

[JUnit Categories]: https://maven.apache.org/surefire/maven-surefire-plugin/examples/junit.html

### Unit Tests

```java
mvn test -Dgroups=UnitTest
```

Or using Docker:

```bash
make build
```

### Integration Tests

```java
mvn integration-test -Dgroups=IntegrationTests
```

Or using Docker:

```bash
make integrationTest
```

### System Tests

System tests run with Selenium using docker-compose to run a [Selenium standalone container][] with Chrome.

[Selenium standalone container]: https://github.com/SeleniumHQ/docker-selenium

Using Docker:

* If you are running locally, make sure the `$APP_URL` is populated and points to a valid instance of your application. This variable is populated automatically in Jenkins.

```bash
APP_URL=http://dev-cicd-demo-master.anzcd.internal/ make systemTest
```