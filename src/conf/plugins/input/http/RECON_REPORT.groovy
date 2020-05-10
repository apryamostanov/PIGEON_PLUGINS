package conf.plugins.input.http

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.springdatarest.entities.OutputMessage
import io.infinite.pigeon.springdatarest.repositories.InputMessageRepository
import io.infinite.supplies.ast.exceptions.ExceptionUtils
import org.apache.commons.lang3.time.FastDateFormat
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.support.SpringBeanAutowiringSupport
import org.springframework.web.context.support.WebApplicationContextUtils

import javax.persistence.EntityManager
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

def log = LoggerFactory.getLogger(this.getClass())

@Component
@BlackBox
class ReconReport {

    @Autowired
    EntityManager entityManager

    List<OutputMessage> findByDates(
            String outputQueueName,
            Date dateFrom,
            Date dateTo
    ) {
        return entityManager.createQuery(
                """select o from OutputMessage o
                join o.httpLogs h
                where o.outputQueueName = :outputQueueName
                and o.insertTime between :dateFrom and :dateTo
                order by h.id desc"""
                , OutputMessage.class
        )
                .setParameter("outputQueueName", outputQueueName)
                .setParameter("dateFrom", dateFrom)
                .setParameter("dateTo", dateTo)
                .getResultList()
    }

    String run(ServletContext servletContext, String outputQueueName, Date dateFrom, Date dateTo) {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this)
        WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext)
        applicationContext.autowireCapableBeanFactory.autowireBean(this)
        JsonSlurper jsonSlurper = new JsonSlurper()

        FastDateFormat fastDateFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss")
        return findByDates(
                outputQueueName,
                dateFrom,
                dateTo
        ).collect {
            def slurped = jsonSlurper.parseText(content)
            slurped << ["recon": [
                    "pigeon_time"                 : fastDateFormat.format(it.insertTime),
                    "wireconnect_url"             : it.url,
                    "pigeon_status"               : it.status,
                    "attempts_count"              : it.attemptsCount,
                    "wdc_response_time"           : it.httpLogs.first().responseDate,
                    "wdc_response_HTTP_statuscode": it.httpLogs.first().responseStatus,
                    "wdc_response_body_text"      : it.httpLogs.first().responseBody
            ]]
        }.join()
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
                httpServletRequest.getParameter("outputQueueName"),
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