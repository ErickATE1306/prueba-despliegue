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
                withCredentials([string(credentialsId: 'dockerhub-pat-v2', variable: 'DOCKER_TOKEN')]) {
                    powershell '''
                        $token = $env:DOCKER_TOKEN.Trim()
                        if ($token -notmatch '^dckr_pat_[A-Za-z0-9_-]+$') {
                            throw 'La credencial dockerhub-pat-v2 no contiene un token con el formato esperado.'
                        }
                        $tempRoot = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\') + '\'
                        $configDir = [IO.Path]::GetFullPath((Join-Path $tempRoot ('jenkins-docker-' + [guid]::NewGuid().ToString('N'))))
                        if (-not $configDir.StartsWith($tempRoot, [StringComparison]::OrdinalIgnoreCase)) {
                            throw 'La carpeta temporal de Docker queda fuera del directorio esperado.'
                        }
                        New-Item -ItemType Directory -Path $configDir | Out-Null
                        $configFile = Join-Path $configDir 'config.json'
                        $previousConfig = $env:DOCKER_CONFIG
                        try {
                            $auth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("toan13:$token"))
                            $config = @{ auths = @{ 'https://index.docker.io/v1/' = @{ auth = $auth } } }
                            $json = $config | ConvertTo-Json -Compress -Depth 4
                            [IO.File]::WriteAllText($configFile, $json, [Text.UTF8Encoding]::new($false))
                            $env:DOCKER_CONFIG = $configDir
                            $imageTag = '{0}:{1}' -f $env:IMAGE, $env:BUILD_NUMBER
                            & docker.exe push $imageTag
                            if ($LASTEXITCODE -ne 0) { throw "docker push fallo con codigo $LASTEXITCODE" }
                        } finally {
                            $env:DOCKER_CONFIG = $previousConfig
                            if (Test-Path -LiteralPath $configDir) {
                                Remove-Item -LiteralPath $configDir -Recurse -Force
                            }
                        }
                    '''
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
