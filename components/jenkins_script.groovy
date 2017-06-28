node {
    //currentBuild.result = 'FAILURE'
    try{
       stage('Preparation') { // for display purposes
          // Get some code from a GitHub repositories
          parallel( 
              main:{
                dir('Tmain') {
                    git credentialsId: '714b269f-241b-4c02-8c29-16e172046721', url: 'https://github.com/mukkachaitanya/Tmain'
                }
              }, s1:{
                dir('Source1') {
                    git credentialsId: '714b269f-241b-4c02-8c29-16e172046721', url: 'https://github.com/mukkachaitanya/Source1'
                }
              }, s2:{
                dir('Source2') {
                    git credentialsId: '714b269f-241b-4c02-8c29-16e172046721', url: 'https://github.com/mukkachaitanya/Source2'
                }
              }
          )
          sh 'pwd'
          
       }
       stage('Build') {
          // Run the  build
            sh 'printenv'
            sh 'cp -R --remove-destination Tmain App'
            sh 'cp -R --remove-destination Source1 Source2 App'
            dir('builds'){
                sh 'tar -zvcf Appliance-$BUILD_TAG.tar.gz ../App'
            }
       }
       stage('Results') {
          archiveArtifacts 'builds/*.tar.gz'
          currentBuild.result = 'SUCCESS'
       }
    }
    catch(err){
        sh 'echo ${err}'
        currentBuild.result = 'FAILURE'
    }
}