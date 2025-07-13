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


        stage('Promote to Prod') {
            steps {
                input message: "Deploy to PROD?", ok: "Yes, go live"
            }
        }

        stage('Configure Prod Server') {
            steps {
                sh "ansible-playbook -i inventory_kube.ini playbook.yml --extra-vars \"image=${BUILT_IMAGE} env=prod\" -e \"ansible_gather_subset=!mounts\""
                sh "ansible-playbook -i inventory_kube.ini node_exporter.yml"
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
                script {
                sh '''
                    python3 -m venv venv
                    . venv/bin/activate
                    pip install -r requirements.txt
                    pytest tests/prod/ --prod-url=$PRO_URL
                '''
             }
            }
        }

        stage('Config Prometheus') {
            steps {
                sh '''
                    sudo mv prometheus.yml /etc/prometheus/prometheus.yml
                    sudo systemctl restart prometheus
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
