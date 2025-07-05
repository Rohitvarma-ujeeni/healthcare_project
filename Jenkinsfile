pipeline {
    agent { label 'Project_Slave1' }

    environment {
        IMAGE_NAME = 'rohitvarmaujeeni/healthcare-app'
        DOCKER_IMAGE = "${IMAGE_NAME}:${BUILD_NUMBER}"
        BUILT_IMAGE = "${IMAGE_NAME}:${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/Rohitvarma-ujeeni/healthcare_project.git', branch: 'master'
            }
        }

        stage('Build & Unit Test') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Docker Build & Push') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE} ."

                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    sh "echo ${PASS} | docker login -u ${USER} --password-stdin"
                    sh "docker push ${DOCKER_IMAGE}"
                }
            }
        }

        stage('Provision Dev Infra') {
            steps {
                withCredentials([
                    string(credentialsId: 'AWS_ACCESS_KEY_ID', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'AWS_SECRET_ACCESS_KEY', variable: 'AWS_SECRET_ACCESS_KEY')
                ]) {
                    dir('terraform/dev') {
                        sh '''
                            export AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
                            export AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
                            terraform init
                            terraform apply -auto-approve
                        '''
                    }
                }
                sleep(time: 60, unit: 'SECONDS')
            }
        }

        stage('Prepare Dev Inventory') {
            steps {
                script {
                    def instanceIP = sh(script: 'cd terraform/dev && terraform output -raw instance_ip', returnStdout: true).trim()
                    writeFile file: 'inventory_dev.ini', text: """
[dev]
${instanceIP} ansible_user=ubuntu ansible_ssh_private_key_file=~/.ssh/id_ecdsa ansible_ssh_common_args='-o StrictHostKeyChecking=no'
"""
                }
            }
        }

        stage('Configure Dev Server') {
            steps {
                sh "ansible-playbook -i inventory_dev.ini playbook.yml --extra-vars 'image=${BUILT_IMAGE} env=dev'"
            }
        }

        stage('Deploy to Dev') {
            steps {
                withCredentials([file(credentialsId: 'kubeconfig-credentials-id', variable: 'KUBECONFIG_FILE')]) {
                    sh '''
                        export KUBECONFIG=$KUBECONFIG_FILE
                        sed "s|{{IMAGE_TAG}}|${BUILT_IMAGE}|g" k8s/deployment.yaml | kubectl apply -f -
                    '''
                }
            }
        }

        stage('Test on Dev') {
            steps {
                sh '''
                    pip install -r requirements.txt
                    pytest tests/dev/
                '''
            }
        }

        stage('Promote to Stage') {
            steps {
                input message: "Deploy to STAGE?", ok: "Yes, proceed"
            }
        }

        stage('Configure Stage Server') {
            steps {
                sh 'ansible-playbook -i inventory_kube.ini playbook.yml'
            }
        }

        stage('Deploy to Stage') {
            steps {
                withCredentials([file(credentialsId: 'kubeconfig-credentials-id', variable: 'KUBECONFIG_FILE')]) {
                    sh '''
                        export KUBECONFIG=$KUBECONFIG_FILE
                        sed "s|{{IMAGE_TAG}}|${BUILT_IMAGE}|g" k8s/deployment.yaml | kubectl apply -f -
                    '''
                }
            }
        }

        stage('Promote to Prod') {
            steps {
                input message: "Deploy to PROD?", ok: "Yes, go live"
            }
        }

        stage('Configure Prod Server') {
            steps {
                sh 'ansible-playbook -i inventory_kube.ini playbook.yml'
            }
        }

        stage('Deploy to Prod') {
            steps {
                withCredentials([file(credentialsId: 'kubeconfig-credentials-id', variable: 'KUBECONFIG_FILE')]) {
                    sh '''
                        export KUBECONFIG=$KUBECONFIG_FILE
                        sed "s|{{IMAGE_TAG}}|${BUILT_IMAGE}|g" k8s/deployment.yaml | kubectl apply -f -
                    '''
                }
            }
        }

        stage('Test on Prod') {
            steps {
                sh '''
                    pip install -r requirements.txt
                    pytest tests/prod/
                '''
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
