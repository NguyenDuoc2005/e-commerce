pipeline {
  agent any

  options {
    timestamps()
    ansiColor('xterm')
    disableConcurrentBuilds()
    skipDefaultCheckout(true)
  }

  environment {
    DOCKER_IMAGE = "nguyenduoc/datn-be"
    DEPLOY_HOST  = "127.0.0.1"
    DEPLOY_USER  = "root"
    APP_DIR      = "/opt/ecommerce"

    // Gradle cache nằm trong workspace => tránh lỗi permission
    GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        sh '''
          set -e
          git rev-parse --short HEAD
        '''
      }
    }

    stage('Set Commit SHA') {
      steps {
        script {
          env.GIT_SHA = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
          echo "Deploy tag: ${env.GIT_SHA}"
        }
      }
    }

    stage('Check Environment') {
      steps {
        sh '''
          set +e
          echo "=== Jenkins Java ==="
          which java || true
          java -version || true
          echo "=== Docker ==="
          docker version || true
          echo "WORKSPACE=$WORKSPACE"
          echo "GRADLE_USER_HOME=$GRADLE_USER_HOME"
          mkdir -p "$GRADLE_USER_HOME"
          ls -la "$GRADLE_USER_HOME" || true
          set -e
        '''
      }
    }

    stage('Build JAR (Gradle JDK17)') {
      agent {
        docker {
          image 'gradle:8.13-jdk17'
          // KHÔNG mount $HOME/.gradle để tránh lỗi lock file permission
          reuseNode true
        }
      }
      steps {
        sh '''
          set -e
          cd BE
          chmod +x gradlew

          echo "=== Gradle wrapper version ==="
          ./gradlew --version

          echo "=== Build bootJar (skip tests) ==="
          ./gradlew --no-daemon clean bootJar -x test

          echo "=== List jar ==="
          ls -lah build/libs || true
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
          sh '''
            set -e
            echo "$DP" | docker login -u "$DU" --password-stdin
          '''
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

              # Update TAG in .env (create if missing)
              if [ -f .env ]; then
                if grep -q "^TAG=" .env; then
                  sed -i "s/^TAG=.*/TAG=${GIT_SHA}/" .env
                else
                  echo "TAG=${GIT_SHA}" >> .env
                fi
              else
                echo "TAG=${GIT_SHA}" > .env
              fi

              docker compose pull be
              docker compose up -d be
              docker compose ps

              # optional: dọn image dangling (an toàn)
              docker image prune -f
            '
          """
        }
      }
    }
  }

  post {
    always {
      sh '''
        set +e
        echo "=== Cleanup workspace gradle locks (optional) ==="
        rm -rf "$GRADLE_USER_HOME/wrapper/dists/"*/*.lck 2>/dev/null || true
        set -e
      '''
      cleanWs()
    }
  }
}