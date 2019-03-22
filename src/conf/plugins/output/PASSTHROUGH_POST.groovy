package conf.plugins.output

import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.springdatarest.entities.InputMessage
import io.infinite.pigeon.springdatarest.entities.OutputMessage
import io.infinite.pigeon.http.HttpRequest
import io.infinite.pigeon.conf.OutputQueue
import org.slf4j.LoggerFactory

def log = LoggerFactory.getLogger(this.getClass())

@BlackBox
def applyPlugin() {
    def log = LoggerFactory.getLogger(this.getClass())
    InputMessage inputMessage = binding.getVariable("inputMessage") as InputMessage
    OutputMessage outputMessage = binding.getVariable("outputMessage") as OutputMessage
    OutputQueue outputQueue = binding.getVariable("outputQueue") as OutputQueue
    HttpRequest httpRequest = binding.getVariable("httpRequest") as HttpRequest
    if (outputQueue.httpProperties.containsKey("contentType")) {
        httpRequest.headers.put("Content-Type", outputQueue.httpProperties.get("contentType") as String)
    }
    httpRequest.method = "POST"
    httpRequest.body = inputMessage.payload
    log.info(httpRequest.body)
}

applyPlugin()