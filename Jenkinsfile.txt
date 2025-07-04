pipeline {
    agent { label 'Project_Slave1' }

    environment {
        APP_NAME = 'bank_finance_app'
        DOCKER_IMAGE = "rohitvarmaujeeni/${APP_NAME}:${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/Rohitvarma-ujeeni/bank_finance.git', branch: 'master'
            }
        }

        stage('Build & Unit Test') {
            steps {
                sh 'mvn clean package'
                sh 'mvn test -DsuiteXmlFile=testng.xml'
                sh 'mvn surefire-report:report'
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

        // ---------- DEV ENV ----------

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
                sleep(time: 59, unit: 'SECONDS')
            }
        }

        stage('Prepare Dev Inventory') {
            steps {
                script {
                    def instanceIP = sh(script: 'terraform -chdir=terraform/dev output -raw instance_ip', returnStdout: true).trim()
                    writeFile file: 'inventory_dev.ini', text: """
[dev]
${instanceIP} ansible_user=ubuntu ansible_ssh_private_key_file=~/.ssh/id_ecdsa ansible_ssh_common_args='-o StrictHostKeyChecking=no'
"""
                }
            }
        }

        stage('Configure Dev Server') {
            steps {
                sh 'ansible-playbook -i inventory_dev.ini playbook.yml'
            }
        }

        stage('Deploy to Dev') {
            steps {
                sh "ansible-playbook -i inventory_dev.ini deploy.yml --extra-vars \"image=${DOCKER_IMAGE}\""
            }
        }

        stage('Test on Dev') {
            steps {
                sh 'mvn test -DsuiteXmlFile=testng.xml'
                junit '**/target/surefire-reports/*.xml'
            }
        }

        // ---------- STAGE ----------

        stage('Promote to Stage') {
            steps {
                input message: 'Approve deployment to STAGE?'
            }
        }

        stage('Provision Stage Infra') {
            steps {
                withCredentials([
                    string(credentialsId: 'AWS_ACCESS_KEY_ID', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'AWS_SECRET_ACCESS_KEY', variable: 'AWS_SECRET_ACCESS_KEY')
                ]) {
                    dir('terraform/stage') {
                        sh '''
                            export AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
                            export AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
                            terraform init
                            terraform apply -auto-approve
                        '''
                    }
                }
                sleep(time: 59, unit: 'SECONDS')
            }
        }

        stage('Prepare Stage Inventory') {
            steps {
                script {
                    def instanceIP = sh(script: 'terraform -chdir=terraform/stage output -raw instance_ip', returnStdout: true).trim()
                    writeFile file: 'inventory_stage.ini', text: """
[stage]
${instanceIP} ansible_user=ubuntu ansible_ssh_private_key_file=~/.ssh/id_ecdsa ansible_ssh_common_args='-o StrictHostKeyChecking=no'
"""
                }
            }
        }

        stage('Configure Stage Server') {
            steps {
                sh 'ansible-playbook -i inventory_stage.ini playbook.yml'
            }
        }

        stage('Deploy to Stage') {
            steps {
                sh "ansible-playbook -i inventory_stage.ini deploy.yml --extra-vars \"image=${DOCKER_IMAGE}\""
            }
        }

        // ---------- PROD ----------

        stage('Promote to Prod') {
            steps {
                input message: 'Approve deployment to PROD?'
            }
        }

        stage('Provision Prod Infra') {
            steps {
                withCredentials([
                    string(credentialsId: 'AWS_ACCESS_KEY_ID', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'AWS_SECRET_ACCESS_KEY', variable: 'AWS_SECRET_ACCESS_KEY')
                ]) {
                    dir('terraform/prod') {
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

        stage('Prepare Prod Inventory') {
            steps {
                script {
                    def instanceIP = sh(script: 'terraform -chdir=terraform/prod output -raw instance_ip', returnStdout: true).trim()
                    writeFile file: 'inventory_prod.ini', text: """
[prod]
${instanceIP} ansible_user=ubuntu ansible_ssh_private_key_file=~/.ssh/id_ecdsa ansible_ssh_common_args='-o StrictHostKeyChecking=no'
"""
                }
            }
        }

        stage('Configure Prod Server') {
            steps {
                sh 'ansible-playbook -i inventory_prod.ini playbook.yml'
            }
        }

        stage('Deploy to Prod') {
            steps {
                sh "ansible-playbook -i inventory_prod.ini deploy.yml --extra-vars \"image=${DOCKER_IMAGE}\""
            }
        }

        stage('Test on Prod') {
            steps {
                sh 'mvn test -DsuiteXmlFile=testng.xml'
                junit '**/target/surefire-reports/*.xml'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
