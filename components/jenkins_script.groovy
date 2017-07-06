node {
    //currentBuild.result = 'FAILURE'
    try{
       stage('Preparation') { // for display purposes
          // Get some code from a GitHub repositories
          parallel( 
              appliance:{
                dir('certification-appliance') {
                    git credentialsId: '714b269f-241b-4c02-8c29-16e172046721', url: 'https://github.com/hortonworks/certification-appliance', branch: '$APPLIANCE_BRANCH'
                }
              }, certification_repo:{
                dir('certification') {
                    git credentialsId: '714b269f-241b-4c02-8c29-16e172046721', url: 'https://github.com/hortonworks/certification', branch: 'testHCK'
                }
              }
          )
          
       }
       stage('Build') {
          // Run the  build
            sh 'rsync -r --exclude=.git certification-appliance/* App'
            sh 'rsync -r --del --exclude=.git certification App/build/common'
            dir('builds'){
                sh 'rm -rf ./*' //clean('builds')
                sh 'tar -zvcf Appliance-$BUILD_TAG.tar.gz ../App --exclude-vcs'
                def changelog = "CHANGELOG\n---------\n" + getChangeString()
                
                if(changelog){
                    sh "echo '${changelog}'"
                    sh "echo '${changelog}' > $BUILD_TAG-changelog.log"
                }
                
            }
       }
       stage('Results') {
          archiveArtifacts 'builds/*'
          currentBuild.result = 'SUCCESS'
       }
    }
    catch(err){
        sh "echo '${err}'"
        currentBuild.result = 'FAILURE'
    }
}


@NonCPS
def getChangeString() {
    MAX_MSG_LEN = 100
    def changeString = ""

    echo "Gathering SCM changes"
    def changeLogSets = currentBuild.rawBuild.changeSets
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            truncated_msg = entry.msg.take(MAX_MSG_LEN)
            changeString += " - ${truncated_msg} [${entry.author}]\n"
        }
    }

    if (!changeString) {
        changeString = " - No new changes"
    }
    return changeString
}
