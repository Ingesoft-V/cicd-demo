
    pipeline {
        agent any
    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/Ingesoft-V/cicd-demo.git'
            }
        }
        stage('Build & Test') {
            steps {
                sh 'mvn clean package' // Compila y ejecuta pruebas unitarias automáticamente
            }
        }
        stage('Static Analysis (SonarQube)') {
            steps {
            // Requiere el plugin de SonarQube instalado en Jenkins
                script {
                    sh 'mvn sonar:sonar -Dsonar.projectKey=my-app -Dsonar.host.url=http://sonarqube:9000'
                }
            }
        }
        stage('Container Security Scan (Trivy)') {
            steps {
                // Escanea la imagen Docker buscando vulnerabilidades conocidas
                sh 'docker build -t mi-app:latest .'
                sh 'trivy image mi-app:latest'
            }
        }
        stage('Deploy') {
            when { branch 'main' }
                steps {
                    sh 'docker run -d -p 8080:8080 mi-app:latest'
                }
        }
    }
    post {
        always {
            echo 'Limpiando entorno...'
            cleanWs() // Limpia el espacio de trabajo después de cada ejecución
        }
    }
    }