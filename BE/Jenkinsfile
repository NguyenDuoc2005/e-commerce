pipeline {
  agent any

  environment {
    DOCKER_IMAGE = "nguyenduoc/datn-be"
    DEPLOY_HOST  = "127.0.0.1"        // deploy ngay trên server Jenkins
    DEPLOY_USER  = "root"
    APP_DIR      = "/opt/ecommerce"
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Set Commit SHA') {
      steps {
        script {
          env.GIT_SHA = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
          echo "Deploy tag: ${env.GIT_SHA}"
        }
      }
    }

    stage('Build JAR (Gradle)') {
      steps {
        sh '''
          set -e
          cd BE
          chmod +x gradlew
          ./gradlew clean bootJar -x test
        '''
      }
    }

    stage('Docker Build') {
      steps {
        sh """
          set -e
          docker build -t ${DOCKER_IMAGE}:latest -t ${DOCKER_IMAGE}:${GIT_SHA} .
        """
      }
    }

    stage('Docker Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DU', passwordVariable: 'DP')]) {
          sh 'echo "$DP" | docker login -u "$DU" --password-stdin'
        }
        sh """
          set -e
          docker push ${DOCKER_IMAGE}:latest
          docker push ${DOCKER_IMAGE}:${GIT_SHA}
        """
      }
    }

    stage('Deploy (docker compose)') {
      steps {
        sshagent(credentials: ['deploy-ssh']) {
          sh """
            ssh -o StrictHostKeyChecking=no ${DEPLOY_USER}@${DEPLOY_HOST} '
              set -e
              cd "${APP_DIR}"

              # Update TAG in .env
              if grep -q "^TAG=" .env; then
                sed -i "s/^TAG=.*/TAG=${GIT_SHA}/" .env
              else
                echo "TAG=${GIT_SHA}" >> .env
              fi

              docker compose pull be
              docker compose up -d be
              docker compose ps
            '
          """
        }
      }
    }
  }
}