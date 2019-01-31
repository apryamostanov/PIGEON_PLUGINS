package conf.plugins.input.http


import io.infinite.blackbox.BlackBox
import io.infinite.supplies.ast.exceptions.ExceptionUtils
import org.slf4j.LoggerFactory
import org.slf4j.MDC

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.text.SimpleDateFormat

MDC.put("messageTimestamp", new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS").format(new Date()))

def log = LoggerFactory.getLogger(this.getClass())

@BlackBox
def applyPlugin() {
    HttpServletRequest httpServletRequest = binding.getVariable("httpServletRequest") as HttpServletRequest
    HttpServletResponse httpServletResponse = binding.getVariable("httpServletResponse") as HttpServletResponse
    def log = LoggerFactory.getLogger(this.getClass())
    String requestBody = ""
    try {
        String externalId = System.currentTimeMillis().toString()
        requestBody = httpServletRequest.getReader().getText()
        log.info("Request:")
        log.info(httpServletRequest.getRequestURI())
        log.info(httpServletRequest.getRequestURL().toString())
        log.info(requestBody)
        for (headerName in httpServletRequest.getHeaderNames()) {
            log.info(headerName + ":" + httpServletRequest.getHeader(headerName))
        }
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
        log.error(requestBody)
        return new ExceptionUtils().sanitizedStacktrace(e)
    }
}

String response = applyPlugin()
log.info("Response:")
log.info(response)
return response