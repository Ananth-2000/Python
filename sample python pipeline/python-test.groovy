pipeline {
  agent any

  stages {
    stage('Clone Python Repo') {
      steps {
        git branch: 'main', url: 'https://github.com/Ananth-2000/Python.git', credentialsId: 'github_creds'
      }
    }

    stage('Create ConfigMap from Script') {
      steps {
        sh 'kubectl delete configmap python-script --ignore-not-found'
        sh 'kubectl create configmap python-script --from-file=test-python.py'
      }
    }

    stage('Apply Job to Run Python') {
      steps {
        writeFile file: 'python-job.yaml', text: '''
apiVersion: batch/v1
kind: Job
metadata:
  name: python-runner
spec:
  template:
    spec:
      containers:
      - name: python
        image: python:3.10
        command: ["python", "/scripts/test-python.py"]
        volumeMounts:
        - name: script-volume
          mountPath: /scripts
      restartPolicy: Never
      volumes:
      - name: script-volume
        configMap:
          name: python-script
  backoffLimit: 0
'''
        sh 'kubectl delete job python-runner --ignore-not-found'
        sh 'kubectl apply -f python-job.yaml'
      }
    }

    stage('Wait for Job Completion (2 mins)') {
      steps {
        script {
          sh 'kubectl wait --for=condition=complete --timeout=120s job/python-runner'
        }
      }
    }

    stage('Fetch Job Logs') {
      steps {
        script {
          def podName = sh(
            script: "kubectl get pods --selector=job-name=python-runner -o jsonpath='{.items[0].metadata.name}'",
            returnStdout: true
          ).trim().replaceAll("'", "")
          sh "kubectl logs ${podName}"
        }
      }
    }
  }

  post {
    success {
      echo '✅ Python script executed successfully in Kubernetes.'
    }
    failure {
      echo '❌ Pipeline failed.'
    }
  }
}
