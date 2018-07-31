/**
 * @param hook the API Hook representing the project
 * @param params a map containing the overrides you may want to apply
 * @param mode either 'project', 'tag' or 'single'. If null, project will be assumed
 * @param id required only if mode is 'tag' or 'single'. Either the tag or the id of the test you want to runù
 * @param format either 'integer', 'json' or 'junit'
 * @param token: may not be required based on instance settings. The authorization token for the proposed hook
 */
return { String hook, Map params, String mode, String id, String format, String token ->
    if(!params)
        params = [:]
    String reqBody = groovy.json.JsonOutput.toJson([params:params])
    String url = "$hook/tests/"
    switch(mode) {
        case 'tag':
            url+="tag/$id/run"
            break
        case 'single':
            url+=id+'/run'
            break
        default:
            url+='run-all'
    }
    url+='?sync=true'
    switch(format){
        case 'junit':
            url+='&format=junit'
            break
    }
    def customHeaders = []
    if(token)
        customHeaders += [name: 'Authorization', value: 'Bearer '+token]
    def response = httpRequest(httpMode:'POST',
            url:url,
            acceptType:'APPLICATION_JSON',
            contentType:'APPLICATION_JSON',
            requestBody: reqBody,
            ignoreSslErrors: true,
            customHeaders: customHeaders
    )
    if(response.status==200){
        switch(format){
            case 'junit':
            case 'json':
                return response.content
            default:
                def json = new groovy.json.JsonSlurper().parseText(response.content)
                int failed = json.count {it.failuresCount > 0 }
                return failed
        }
    } else {
        throw new IOException('APIF: server response status code is: '+response.status+'. Cannot proceed')
    }
}
