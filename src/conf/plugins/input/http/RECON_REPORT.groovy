package conf.plugins.input.http

import io.infinite.blackbox.BlackBox
import io.infinite.supplies.ast.exceptions.ExceptionUtils
import org.slf4j.LoggerFactory

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

def log = LoggerFactory.getLogger(this.getClass())

@BlackBox
def applyPlugin() {
    HttpServletRequest httpServletRequest = binding.getVariable("httpServletRequest") as HttpServletRequest
    HttpServletResponse httpServletResponse = binding.getVariable("httpServletResponse") as HttpServletResponse
    String requestBody = binding.getVariable("requestBody")
    def log = LoggerFactory.getLogger(this.getClass())
    try {
        String externalId = System.currentTimeMillis().toString()
        log.info("Request:")
        log.info(httpServletRequest.getRequestURI())
        log.info(httpServletRequest.getRequestURL().toString())
        for (headerName in httpServletRequest.getHeaderNames()) {
            log.info(headerName + ":" + httpServletRequest.getHeader(headerName))
        }
        return "recon"
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