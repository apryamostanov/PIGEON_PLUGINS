package conf.plugins.input.rest

import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.springdatarest.controllers.CustomResponse
import io.infinite.supplies.ast.exceptions.ExceptionUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import javax.servlet.http.HttpServletRequest

def log = LoggerFactory.getLogger(this.getClass())

@BlackBox
def applyPlugin() {
    HttpServletRequest httpServletRequest = binding.getVariable("httpServletRequest") as HttpServletRequest
    String requestBody = binding.getVariable("requestBody")
    CustomResponse customResponse = new CustomResponse()
    def log = LoggerFactory.getLogger(this.getClass())
    try {
        log.info("Request:")
        log.info(httpServletRequest.getRequestURI())
        log.info(httpServletRequest.getRequestURL().toString())
        log.info(requestBody)
        customResponse.setResponse(requestBody.replace("\r", ""))
        return new ResponseEntity(customResponse, HttpStatus.OK)
    } catch (Exception e) {
        log.error(e.getMessage(), e)
        log.error(requestBody)
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