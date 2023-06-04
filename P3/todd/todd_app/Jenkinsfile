pipeline {
    agent any

    stages {
      stage('Checkout') {
          steps {
              echo 'Checking out...'
              git credentialsId: 'atb-bitbucket-credentials', url: 'https://bitbucket.org/mei-isep/todd/'
          }
      }
      stage('Build') {
          steps {
              echo 'Building...'
              sh './gradlew clean build'
          }
      }
      stage('Jenkins Archiving') {
          steps {
              echo 'Jenkins Archiving...'
              archiveArtifacts 'build/libs/*'
          }
      }
      stage ('Nexus Archiving') {
          steps {
            echo 'Nexus Archiving...'
              sh './gradlew uploadArchives'
          }
      }
    }
}
