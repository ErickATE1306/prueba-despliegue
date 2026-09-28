pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
    }

    // GitHub no puede llamar a un Jenkins que solo existe en localhost.
    triggers {
        pollSCM('H/2 * * * *')
    }

    environment {
        IMAGE = 'toan13/inventario-productos'
    }

    stages {
        stage('Obtener codigo') {
            steps {
                checkout scm
            }
        }

        stage('Pruebas') {
            steps {
                bat 'call gradlew.bat --no-daemon test'
            }
        }

        stage('Construir imagen') {
            steps {
                bat 'docker build -t %IMAGE%:%BUILD_NUMBER% .'
            }
        }

        stage('Publicar imagen') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-inventario',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_TOKEN'
                )]) {
                    powershell '$env:DOCKER_TOKEN | docker login --username $env:DOCKER_USER --password-stdin; if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }'
                    bat 'docker push %IMAGE%:%BUILD_NUMBER%'
                }
            }
            post {
                always {
                    bat 'docker logout'
                }
            }
        }

        stage('Desplegar en Kubernetes local') {
            steps {
                withCredentials([file(
                    credentialsId: 'kubeconfig-docker-desktop',
                    variable: 'KUBECONFIG'
                )]) {
                    bat 'kubectl --context docker-desktop set image deployment/inventario-productos inventario-productos=%IMAGE%:%BUILD_NUMBER%'
                    bat 'kubectl --context docker-desktop rollout status deployment/inventario-productos --timeout=120s'
                }
            }
        }
    }
}
