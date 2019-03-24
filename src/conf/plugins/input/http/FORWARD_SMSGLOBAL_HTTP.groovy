package conf.plugins.input.http

import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.pigeon.springdatarest.entities.InputMessage
import io.infinite.pigeon.springdatarest.repositories.InputMessageRepository
import io.infinite.supplies.ast.exceptions.ExceptionUtils
import org.slf4j.LoggerFactory

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

def log = LoggerFactory.getLogger(FORWARD_SMSGLOBAL_HTTP.getClass())

@BlackBox
def applyPlugin() {
    HttpServletRequest httpServletRequest = binding.getVariable("httpServletRequest") as HttpServletRequest
    HttpServletResponse httpServletResponse = binding.getVariable("httpServletResponse") as HttpServletResponse
    InputMessageRepository inputMessageRepository = binding.getVariable("inputMessageRepository") as InputMessageRepository
    def log = LoggerFactory.getLogger(FORWARD_SMSGLOBAL_HTTP.getClass())
    try {
        String externalId = System.currentTimeMillis().toString()
        requestBody = httpServletRequest.getReader().getText()
        log.info("Request:")
        log.info(httpServletRequest.getRequestURI())
        log.info(httpServletRequest.getRequestURL().toString())
        for (headerName in httpServletRequest.getHeaderNames()) {
            log.info(headerName + ":" + httpServletRequest.getHeader(headerName))
        }
        InputMessage inputMessageSmsGlobal = new InputMessage()
        String externalIdSmsglobal = System.currentTimeMillis().toString()
        SmsglobalMessageFormat smsGlobalMessageFormat = new SmsglobalMessageFormat()
        smsGlobalMessageFormat.text = URLEncoder.encode(httpServletRequest.getParameter("text"), "UTF-8")
        smsGlobalMessageFormat.to = URLEncoder.encode(httpServletRequest.getParameter("to"), "UTF-8")
        smsGlobalMessageFormat.user = URLEncoder.encode(httpServletRequest.getParameter("user"), "UTF-8")
        smsGlobalMessageFormat.password = URLEncoder.encode(httpServletRequest.getParameter("password"), "UTF-8")
        smsGlobalMessageFormat.from = URLEncoder.encode(httpServletRequest.getParameter("from"), "UTF-8")
        inputMessageSmsGlobal.setExternalId(externalIdSmsglobal)
        inputMessageSmsGlobal.setSourceName("FORWARD_SMSGLOBAL_HTTP_PLUGIN")
        inputMessageSmsGlobal.setInputQueueName("SMSGLOBAL_PROXY")
        inputMessageSmsGlobal.setPayload(getTemplatePayloadUrl(smsGlobalMessageFormat))
        inputMessageSmsGlobal.setStatus(MessageStatuses.NEW.value())
        inputMessageRepository.save(inputMessageSmsGlobal)
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*")
        httpServletResponse.setHeader("Connection", "Keep-Alive")
        httpServletResponse.setHeader("Content-Type", "text/html; charset=UTF-8")
        httpServletResponse.setHeader("Keep-Alive", "timeout=5, max=96")
        httpServletResponse.setHeader("Server", "Apache/2.4.18 (Ubuntu)")
        httpServletResponse.setHeader("Vary", "Accept-Encoding")
        httpServletResponse.setHeader("X-Been-Served-By", "api02")
        return "OK: 0; Sent queued message ID: ${Long.toHexString(Long.valueOf(externalId))} SMSGlobalMsgID:${externalId}"
    } catch (Exception e) {
        log.error(e.getMessage(), e)
        log.info(httpServletRequest.getRequestURI())
        log.info(httpServletRequest.getRequestURL().toString())
        return new ExceptionUtils().sanitizedStacktrace(e)
    }
}

String response = applyPlugin()
log.info("Response:")
log.info(response)
return response

String getTemplatePayloadUrl(SmsglobalMessageFormat smsglobalMessageFormat) {
    return """?action=sendsms&user=${smsglobalMessageFormat.user}&password=${smsglobalMessageFormat.password}&from=${
        smsglobalMessageFormat.from
    }&to=${smsglobalMessageFormat.to}&text=${smsglobalMessageFormat.text}"""
}

class SmsglobalMessageFormat {
    String text = ""
    String to = ""
    String user = ""
    String password = ""
    String from = ""
}