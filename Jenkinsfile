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
                    powershell '''
                        $token = $env:DOCKER_TOKEN
                        if ($env:DOCKER_USER -ne 'toan13' -or $token -notmatch '^dckr_pat_[A-Za-z0-9_-]+$') {
                            throw 'La credencial dockerhub-inventario no contiene el usuario o el formato de token esperado.'
                        }
                        $startInfo = New-Object System.Diagnostics.ProcessStartInfo
                        $startInfo.FileName = 'docker.exe'
                        $startInfo.Arguments = 'login --username toan13 --password-stdin'
                        $startInfo.UseShellExecute = $false
                        $startInfo.RedirectStandardInput = $true
                        $startInfo.RedirectStandardOutput = $true
                        $startInfo.RedirectStandardError = $true
                        $process = [System.Diagnostics.Process]::Start($startInfo)
                        $process.StandardInput.WriteLine($token)
                        $process.StandardInput.Close()
                        $output = $process.StandardOutput.ReadToEnd()
                        $errorOutput = $process.StandardError.ReadToEnd()
                        $process.WaitForExit()
                        if ($process.ExitCode -ne 0) { Write-Error $errorOutput; exit $process.ExitCode }
                        Write-Output $output
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
