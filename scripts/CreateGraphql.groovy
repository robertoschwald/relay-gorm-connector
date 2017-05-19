includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("_GrailsCreateArtifacts")

target('default': "Creates a Graphql artifact") {
  depends(checkVersion, parseArguments)

  def type = "Graphql"
  promptForName(type: type)

  for (name in argsMap["params"]) {
    name = purgeRedundantArtifactSuffix(name, type)
    //see src/templates/artifacts/graphql.groovy
    createArtifact(name: name, suffix: type, type: type, path: "grails-app/graphql")
    createUnitTest(name: name, suffix: type, superClass: "GraphqlUnitTestCase")
  }

}