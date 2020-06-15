package conf.plugins.input.rest

import groovy.json.JsonSlurper
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.pigeon.springdatarest.controllers.CustomResponse
import io.infinite.pigeon.springdatarest.entities.InputMessage
import io.infinite.pigeon.springdatarest.repositories.InputMessageRepository
import io.infinite.supplies.ast.exceptions.ExceptionUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import javax.servlet.http.HttpServletRequest
import java.text.SimpleDateFormat

def log = LoggerFactory.getLogger(this.getClass())

@BlackBox
def applyPlugin() {
    HttpServletRequest httpServletRequest = binding.getVariable("httpServletRequest") as HttpServletRequest
    InputMessageRepository inputMessageRepository = binding.getVariable("inputMessageRepository") as InputMessageRepository
    CustomResponse customResponse = new CustomResponse()
    def log = LoggerFactory.getLogger(this.getClass())
    try {
        log.info("Request:")
        log.info(httpServletRequest.getRequestURI())
        log.info(httpServletRequest.getRequestURL().toString())
        String testPayload = getTestPayload()
        log.info("Test payload: " + testPayload)
        for (headerName in httpServletRequest.getHeaderNames()) {
            log.info(headerName + ":" + httpServletRequest.getHeader(headerName))
        }
        InputMessage inputMessageOtp = new InputMessage()
        String externalIdOtp = System.currentTimeMillis().toString()
        inputMessageOtp.setExternalId(externalIdOtp)
        inputMessageOtp.setSourceName("SELF_TEST_PLUGIN")
        inputMessageOtp.setPayload(testPayload)
        inputMessageOtp.setInputQueueName("SELF_TEST")
        inputMessageOtp.setStatus(MessageStatuses.NEW.value())
        inputMessageOtp.instanceUUID = null
        inputMessageRepository.save(inputMessageOtp)
        customResponse.setResponse(getResponseJson(externalIdOtp))
        return new ResponseEntity(customResponse, HttpStatus.OK)
    } catch (Exception e) {
        log.error(e.getMessage(), e)
        log.info(httpServletRequest.getRequestURI())
        log.info(httpServletRequest.getRequestURL().toString())
        customResponse.setResponse(new ExceptionUtils().sanitizedStacktrace(e))
        return new ResponseEntity(customResponse, HttpStatus.BAD_REQUEST)
    }
}

ResponseEntity responseEntity = applyPlugin()
log.info("Response:")
log.info(responseEntity.getStatusCode().value().toString())
log.info(responseEntity.getHeaders().toString())
log.info(responseEntity.getBody().toString())
return responseEntity

@BlackBox
def getResponseJson(def externalIdOtp) {
    JsonSlurper jsonSlurper = new JsonSlurper()
    return jsonSlurper.parseText("""{
        "gitHub": "https://github.com/INFINITE-TECHNOLOGY/PIGEON_PLUGINS",
        "outputMessages": "https://pigeon-public.herokuapp.com/pigeon/outputMessages/search/searchByInputExternalIdAndSourceName?sourceName=SELF_TEST_PLUGIN&externalId=${externalIdOtp}",
        "httpLogs": "https://pigeon-public.herokuapp.com/pigeon/readableHttpLogs?format=yaml&sourceName=SELF_TEST_PLUGIN&externalId=${externalIdOtp}"
    }""")
}

@BlackBox
String getTestPayload() {
    return """{
"message": "Test ${new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS").format(new Date())}"
}"""
}