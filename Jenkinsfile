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
        DOCKER_HOST = 'npipe:////./pipe/dockerDesktopLinuxEngine'
        DOCKER_CONTEXT = ''
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
                powershell '''
                    Write-Output "Cuenta Windows: $(whoami)"
                    Write-Output "Docker ejecutable: $((Get-Command docker.exe).Source)"
                    Write-Output "DOCKER_HOST: $env:DOCKER_HOST"
                    Write-Output "Contexto Docker: $(docker context show)"
                    docker version --format 'Cliente={{.Client.Version}} Servidor={{.Server.Version}}'
                    docker info --format 'Daemon ID={{.ID}} Nombre={{.Name}}'
                '''
                withCredentials([string(credentialsId: 'dockerhub-pat-v2', variable: 'DOCKER_TOKEN')]) {
                    powershell '''
                        $token = $env:DOCKER_TOKEN.Trim()
                        if ($token -notmatch '^dckr_pat_[A-Za-z0-9_-]+$') {
                            throw 'La credencial dockerhub-pat-v2 no contiene un token con el formato esperado.'
                        }
                        $sha = [Security.Cryptography.SHA256]::Create()
                        $digest = $sha.ComputeHash([Text.Encoding]::UTF8.GetBytes($token))
                        $fingerprint = [BitConverter]::ToString($digest).Replace('-', '').Substring(0, 12)
                        Write-Output "Huella Jenkins: $fingerprint"

                        $basic = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("toan13:$token"))
                        $authUri = 'https://auth.docker.io/token?service=registry.docker.io&scope=repository:toan13/inventario-productos:pull,push'
                        try {
                            $reply = Invoke-WebRequest -UseBasicParsing -Uri $authUri -Headers @{ Authorization = "Basic $basic" } -ErrorAction Stop
                            Write-Output "Docker Hub autenticacion HTTP: $([int]$reply.StatusCode)"
                        } catch {
                            $status = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { 'sin respuesta HTTP' }
                            Write-Output "Docker Hub autenticacion HTTP: $status"
                        }
                        $token | docker login --username toan13 --password-stdin
                        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
                    '''
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
