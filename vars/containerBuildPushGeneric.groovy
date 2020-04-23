// vars/kanikoBuildPush.groovy
def call(String imageName, String imageTag = env.BUILD_NUMBER, String gcpProject = "core-workshop", Closure body) {
  def dockerReg = "gcr.io/${gcpProject}"
  def label = "img-gcloud-${UUID.randomUUID().toString()}"
  def podYaml = libraryResource 'podtemplates/containerBuildPush.yml'
  podTemplate(name: 'img-gcloud', label: label, yaml: podYaml) {
    node(label) {
      body()
      try {
        env.VERSION = readFile 'version.txt'
        env.VERSION = env.VERSION.trim()
        imageTag = env.VERSION
      } catch(e) {}
      gitShortCommit()
      container('img-gcloud') {
        sh """
          img build --build-arg buildNumber=${BUILD_NUMBER} --build-arg shortCommit=${env.SHORT_COMMIT} --build-arg commitAuthor="${env.COMMIT_AUTHOR}" -t ${dockerReg}/${imageName}:${imageTag} ${pwd()}
          gcloud  auth configure-docker --quiet
          img push ${dockerReg}/${imageName}:${imageTag}
        """
      }
    }
  }
}
