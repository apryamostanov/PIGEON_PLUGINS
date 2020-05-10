package conf.plugins.input.http

import groovy.sql.Sql
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.springdatarest.repositories.InputMessageRepository
import io.infinite.supplies.ast.exceptions.ExceptionUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.stereotype.Component
import org.springframework.web.context.ContextLoader
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.support.SpringBeanAutowiringSupport
import org.springframework.web.context.support.WebApplicationContextUtils

import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.sql.DataSource

def log = LoggerFactory.getLogger(this.getClass())

@Component
class ReconReport {
    @Autowired
    DataSource dataSource

    String run(ServletContext servletContext) {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this)
        Sql sql = new Sql(dataSource)
        WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext)
        applicationContext.autowireCapableBeanFactory.autowireBean(this)
        return sql.firstRow("select * from messages").toString()
    }
}

@BlackBox
def applyPlugin() {
    HttpServletRequest httpServletRequest = binding.getVariable("httpServletRequest") as HttpServletRequest
    HttpServletResponse httpServletResponse = binding.getVariable("httpServletResponse") as HttpServletResponse
    InputMessageRepository inputMessageRepository = binding.getVariable("inputMessageRepository") as InputMessageRepository
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
        return new ReconReport().run(httpServletRequest.servletContext)
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