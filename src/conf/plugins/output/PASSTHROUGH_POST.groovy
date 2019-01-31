package conf.plugins.output

import io.infinite.pigeon.springdatarest.InputMessage
import io.infinite.pigeon.springdatarest.OutputMessage
import io.infinite.pigeon.http.HttpRequest
import io.infinite.pigeon.conf.OutputQueue
import org.slf4j.MDC

import java.text.SimpleDateFormat

InputMessage inputMessage = binding.getVariable("inputMessage") as InputMessage
OutputMessage outputMessage = binding.getVariable("outputMessage") as OutputMessage
OutputQueue outputQueue = binding.getVariable("outputQueue") as OutputQueue
HttpRequest httpRequest = binding.getVariable("httpRequest") as HttpRequest

httpRequest.method = "GET"