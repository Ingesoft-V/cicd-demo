pipeline {
    agent any

    stages {
        // 1. Obtener el código fuente
        stage('Checkout') {
            steps {
                // Reemplaza esta URL con la de tu propio repositorio si usas el tuyo
                git 'https://github.com/Ingesoft-V/cicd-demo.git'
            }
        }

        // 2. Compilar la aplicación
        stage('Build') {
            steps {
                // Comando de compilación (ejemplo para Maven)
                sh './mvnw clean package -DskipTests'
                
                // Si fuera Node.js usarías: sh 'npm install'
            }
        }

        // 3. Ejecutar pruebas básicas
        stage('Test') {
            steps {
                // Comando para ejecutar pruebas
                sh './mvnw test'
                
                // Si fuera Node.js usarías: sh 'npm test'
            }
        }

        // 4. Construir la imagen Docker
        stage('Docker Build') {
            steps {
                sh 'docker build -t mi-app:latest .'
            }
        }
    }
}
