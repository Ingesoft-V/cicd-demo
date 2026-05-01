pipeline {
    agent any

    environment {
        IMAGE_NAME        = 'mi-app'
        IMAGE_TAG         = 'latest'
        CONTAINER_NAME    = 'mi-app-container'
        SONAR_HOST_URL    = 'http://sonarqube:9000'
        SONAR_PROJECT_KEY = 'cicd-demo'
        SONAR_TOKEN       = 'squ_ca83ec8f55baa29de4f7cf58d79d769520efedf3'
        SONAR_LOGIN       = 'admin'
        SONAR_PASSWORD    = 'admin123'
    }

    stages {

        // ── 1. CHECKOUT ────────────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // ── 2. BUILD & TEST ────────────────────────────────────────────────────
        // Compila y corre pruebas unitarias; excluye las pruebas de Selenium
        // que requieren un navegador real.
        stage('Build & Test') {
            steps {
                sh 'mvn clean package -Dtest="!SeleniumExampleTest" -Dfailsafe.excludes="**/*IntTest.java" --batch-mode'
            }
            post {
                always {
                    script {
                        try {
                            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                        } catch (ignored) {
                            archiveArtifacts allowEmptyArchive: true, artifacts: '**/target/surefire-reports/*.xml'
                        }
                    }
                }
            }
        }

        // ── 3. ANÁLISIS ESTÁTICO (SONARQUBE) ───────────────────────────────────
        stage('Static Analysis (SonarQube)') {
            steps {
                script {
                    sh """
                        mvn sonar:sonar \
                            -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                            -Dsonar.host.url=${SONAR_HOST_URL} \
                            -Dsonar.token=${SONAR_TOKEN} \
                            -Dsonar.coverage.exclusions="**/controller/DemoController.java" \
                            --batch-mode
                    """
                }
            }
        }

        // ── 4. QUALITY GATE ────────────────────────────────────────────────────
        // Consulta la API de SonarQube para verificar el estado del Quality Gate.
        // El pipeline falla si SonarQube detecta condición ERROR (Security Hotspot
        // o bugs de nivel blocker).
        stage('Quality Gate') {
            steps {
                script {
                    sleep(time: 15, unit: 'SECONDS')
                    def qgResponse = sh(
                        script: """
                            curl -s -u ${SONAR_TOKEN}: \
                                "${SONAR_HOST_URL}/api/qualitygates/project_status?projectKey=${SONAR_PROJECT_KEY}"
                        """,
                        returnStdout: true
                    ).trim()

                    echo "SonarQube Quality Gate response: ${qgResponse}"

                    if (qgResponse.contains('"status":"ERROR"')) {
                        error("Quality Gate FAILED: SonarQube detectó problemas críticos de seguridad o calidad.")
                    } else if (qgResponse.contains('"status":"WARN"')) {
                        echo "ADVERTENCIA: Quality Gate con advertencias. Revisando detalles..."
                    } else {
                        echo "Quality Gate PASSED."
                    }
                }
            }
        }

        // ── 5. DOCKER BUILD ────────────────────────────────────────────────────
        stage('Docker Build') {
            steps {
                sh """
                    sed -i 's|FROM openjdk:12-alpine|FROM eclipse-temurin:11-jre|g' Dockerfile || true
                    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                """
            }
        }

        // ── 6. ESCANEO DE SEGURIDAD (TRIVY) ───────────────────────────────────
        // Primero imprime el reporte completo (HIGH + CRITICAL) y luego verifica
        // vulnerabilidades CRITICAL con fix disponible.
        stage('Container Security Scan (Trivy)') {
            steps {
                script {
                    echo "Generando reporte de vulnerabilidades (HIGH y CRITICAL)..."
                    sh """
                        docker run --rm \
                            -v /var/run/docker.sock:/var/run/docker.sock \
                            aquasec/trivy image \
                            --severity HIGH,CRITICAL \
                            --exit-code 0 \
                            --format table \
                            --timeout 15m \
                            ${IMAGE_NAME}:${IMAGE_TAG}
                    """

                    echo "Verificando gate de CRITICAL vulnerabilities (solo con fix disponible)..."
                    def trivyExitCode = sh(
                        script: """
                            docker run --rm \
                                -v /var/run/docker.sock:/var/run/docker.sock \
                                aquasec/trivy image \
                                --severity CRITICAL \
                                --ignore-unfixed \
                                --exit-code 1 \
                                --quiet \
                                --timeout 15m \
                                ${IMAGE_NAME}:${IMAGE_TAG}
                        """,
                        returnStatus: true
                    )

                    if (trivyExitCode != 0) {
                        error("Trivy encontró vulnerabilidades CRITICAL en la imagen. Deploy cancelado.")
                    } else {
                        echo "Reporte Trivy completado. Imagen aprobada para deploy."
                    }
                }
            }
        }

        // ── 7. DEPLOY ──────────────────────────────────────────────────────────
        // Despliega cuando la rama es master (GIT_BRANCH funciona en Pipeline from SCM).
        stage('Deploy') {
            when { expression { env.GIT_BRANCH ==~ /.*master.*/ } }
            steps {
                script {
                    sh """
                        echo "Deteniendo contenedor anterior si existe..."
                        docker stop ${CONTAINER_NAME} || true
                        docker rm   ${CONTAINER_NAME} || true

                        echo "Iniciando nueva versión de la aplicación..."
                        docker run -d \
                            --name ${CONTAINER_NAME} \
                            -p 8081:8080 \
                            ${IMAGE_NAME}:${IMAGE_TAG}

                        echo "Aplicación desplegada en http://localhost:8081"
                    """
                }
            }
        }
    }

    // ── POST ───────────────────────────────────────────────────────────────────
    post {
        always {
            echo 'Limpiando espacio de trabajo...'
            script {
                try { cleanWs() } catch (ignored) { deleteDir() }
            }
        }
        success {
            echo "Pipeline completado exitosamente. La aplicacion esta lista."
        }
        failure {
            echo "Pipeline FALLIDO. Revisa los logs de la etapa en rojo para mas detalles."
        }
        unstable {
            echo "Pipeline inestable: algunas pruebas fallaron o el Quality Gate tiene advertencias."
        }
    }
}
