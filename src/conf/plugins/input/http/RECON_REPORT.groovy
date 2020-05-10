package conf.plugins.input.http

import groovy.sql.Sql
import io.infinite.blackbox.BlackBox
import io.infinite.supplies.ast.exceptions.ExceptionUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.ContextLoader
import org.springframework.web.context.WebApplicationContext

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.sql.DataSource

def log = LoggerFactory.getLogger(this.getClass())

class ReconReport {
    @Autowired
    DataSource dataSource
}

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
        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext()
        ReconReport reconReport = new ReconReport()
        context.autowireCapableBeanFactory.autowireBean(reconReport)
        Sql sql = new Sql(reconReport.dataSource)
        return sql.firstRow("select * from messages")
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