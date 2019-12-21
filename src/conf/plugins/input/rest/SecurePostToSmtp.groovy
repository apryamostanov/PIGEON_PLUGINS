package conf.plugins.input.rest

import groovy.json.JsonSlurper
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.other.PigeonException
import io.infinite.pigeon.springdatarest.controllers.CustomResponse
import io.infinite.supplies.ast.exceptions.ExceptionUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper

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
        prepareAndSendEmail(parsedJson.subject, parsedJson.text, parsedJson.recipient, parsedJson.username, parsedJson.password)
        customResponse.setResponse("Sent email successfully.")
        return new ResponseEntity(customResponse, HttpStatus.OK)
    } catch (Exception e) {
        log.error(e.getMessage(), e)
        log.info(httpServletRequest.getRequestURI())
        log.info(httpServletRequest.getRequestURL().toString())
        customResponse.setResponse(new ExceptionUtils().sanitizedStacktrace(e))
        return new ResponseEntity(customResponse, HttpStatus.BAD_REQUEST)
    }
}

void prepareAndSendEmail(String subject, String htmlMessage, String email, String username, String password) {
    if (subject == null) {
        throw new PigeonException("Missing subject")
    }
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl()
    mailSender.setHost("smtp.gmail.com")
    mailSender.setPort(465)
    mailSender.setUsername(username)
    mailSender.setPassword(password)
    Properties mailProp = mailSender.getJavaMailProperties()
    mailProp.put("mail.transport.protocol", "smtp")
    mailProp.put("mail.smtp.auth", "true")
    mailProp.put("mail.smtp.starttls.enable", "true")
    mailProp.put("mail.smtp.starttls.required", "true")
    mailProp.put("mail.debug", "true")
    mailProp.put("mail.smtp.ssl.enable", "true")
    mailProp.put("mail.smtp.user", username)
    MimeMessage mimeMessage = mailSender.createMimeMessage()
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true)
    helper.setTo(email)
    helper.setSubject(subject)
    helper.setText(htmlMessage, true)
    mailSender.send(mimeMessage)
}

ResponseEntity responseEntity = applyPlugin()
log.info("Response:")
log.info(responseEntity.getStatusCode().value().toString())
log.info(responseEntity.getHeaders().toString())
log.info(responseEntity.getBody().toString())
return responseEntity