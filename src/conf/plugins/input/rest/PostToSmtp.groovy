package conf.plugins.input.rest

import groovy.json.JsonSlurper
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.springdatarest.controllers.CustomResponse
import io.infinite.supplies.ast.exceptions.ExceptionUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
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
        for (headerName in httpServletRequest.getHeaderNames()) {
            log.info(headerName + ":" + httpServletRequest.getHeader(headerName))
        }
        JsonSlurper jsonSlurper = new JsonSlurper()
        def parsedJson = jsonSlurper.parseText(requestBody)
        Properties props = new Properties()
        props.put("mail.host", "aspmx.l.google.com")
        Session session = Session.getDefaultInstance(props)
        session.setDebug(true)
        MimeMessage message = new MimeMessage(session)
        message.setFrom(parsedJson.from?.first() as String)
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(parsedJson.recipient?.first()))
        message.setSubject(parsedJson.subject?.first())
        message.setText(parsedJson.text?.first())
        Transport.send(message)
        customResponse.setResponse("Sent email successfully.")
        return new ResponseEntity(customResponse, HttpStatus.OK)
    } catch (Exception e) {
        log.error(e.getMessage(), e)
        log.info(httpServletRequest.getRequestURI())
        log.info(httpServletRequest.getRequestURL().toString())
        return new ExceptionUtils().sanitizedStacktrace(e)
    }
}

ResponseEntity responseEntity = applyPlugin()
log.info("Response:")
log.info(responseEntity.getStatusCode().value().toString())
log.info(responseEntity.getHeaders().toString())
log.info(responseEntity.getBody().toString())
return responseEntity