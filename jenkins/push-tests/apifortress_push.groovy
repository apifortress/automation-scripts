/**
 * @param hook: the API Hook of the project you want to push the tests to
 * @param branch: the "branch" (or generically, id of build) you are associating these tests with
 * @param relativeDirectory: the relative path of where the API Fortress tests can be found
 * @param token: may not be required based on instance settings. The authorization token for the proposed hook
 */
return { String hook, String branch, String relativeDirectory, String token ->
  File directory = new File(workspace+relativeDirectory)
  def body = [resources:[]]
  if(directory.exists()){
    directory.listFiles().each {
        if(it.isDirectory()) {
            println 'APIF PUSH: test "'+it.name+'" added to push'
            File unitFile = new File(it.absolutePath + File.separator + 'unit.xml')
            if (unitFile.exists()) {
                def resource = [path: it.name + File.separator + 'unit.xml', branch: branch, content: unitFile.getText()]
                body.resources.add(resource)
            }
            File inputFile = new File(it.absolutePath + File.separator + 'input.xml')
            if (inputFile.exists()) {
                def resource = [path: it.name + File.separator + 'input.xml', branch: branch, content: inputFile.getText()]
                body.resources.add(resource)
            }
        }
    }
    if(body.resources.size() > 0 ) {
        def customHeaders = []
        if(token)
            customHeaders += [name: 'Authorization', value: 'Bearer '+token]

        def response = httpRequest(httpMode:'POST',
                url:hook+'/tests/push',
                acceptType:'APPLICATION_JSON',
                contentType:'APPLICATION_JSON',
                requestBody: groovy.json.JsonOutput.toJson(body),
                customHeaders: customHeaders,
                ignoreSslErrors:true
        )
        println 'APIF PUSH: '+response.status
        if(response.status > 300)
            currentBuild.result='UNSTABLE'
    }
  } else {
    println 'Test directory does not exist. No test pushed.'
  }

}
