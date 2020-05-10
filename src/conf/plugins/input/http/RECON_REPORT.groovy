package conf.plugins.input.http

import groovy.sql.Sql
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.springdatarest.entities.InputMessage
import io.infinite.pigeon.springdatarest.repositories.InputMessageRepository
import io.infinite.supplies.ast.exceptions.ExceptionUtils
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.commons.lang3.time.FastDateParser
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
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


@RepositoryRestResource
interface CustomInputMessageRepository extends JpaRepository<InputMessage, Long> {

    @Query("""select i from InputMessage i
        join i.outputMessages o
        join o.httpLogs h
        where i.inputQueueName = :inputQueueName
        and i.insertTime between :dateFrom and :dateTo""")
    Set<InputMessage> findByDates(
            @Param("inputQueueName") String inputQueueName,
            @Param("dateFrom") Date dateFrom,
            @Param("dateTo") Date dateTo
    )

}

@Component
class ReconReport {

    @Autowired
    CustomInputMessageRepository customInputMessageRepository

    String run(ServletContext servletContext, String inputQueueName, Date dateFrom, Date dateTo) {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this)
        WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext)
        applicationContext.autowireCapableBeanFactory.autowireBean(this)

        return customInputMessageRepository.findByDates(
                inputQueueName,
                dateFrom,
                dateTo
        )
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
        FastDateFormat fastDateFormat = FastDateFormat.getInstance("yyyyMMdd")
        return new ReconReport().run(
                httpServletRequest.servletContext,
                httpServletRequest.getParameter("inputQueueName"),
                fastDateFormat.parse(httpServletRequest.getParameter("dateFrom")),
                fastDateFormat.parse(httpServletRequest.getParameter("dateTo"))
        )
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