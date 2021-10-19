package org.bonitasoft.connectors

import com.slack.api.Slack
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import org.bonitasoft.engine.connector.AbstractConnector
import org.bonitasoft.engine.connector.ConnectorException
import org.bonitasoft.engine.connector.ConnectorValidationException
import java.util.logging.Logger

open class SlackConnector : AbstractConnector() {

    private val logger: Logger = Logger.getLogger(SlackConnector::class.java.name)

    companion object {
        const val TOKEN_INPUT = "tokenInput"
        const val ID_INPUT = "channelIdInput"
        const val MESSAGE_INPUT = "messageInput"
        const val TS_OUTPUT = "tsOutput"
    }

    override fun validateInputParameters() {
        checkMandatoryStringInput(TOKEN_INPUT)
        checkMandatoryStringInput(ID_INPUT)
        checkMandatoryStringInput(MESSAGE_INPUT)
    }

    private fun checkMandatoryStringInput(inputName: String) {
        val value = getInputParameter(inputName)
        if (value !is String) {
            throw ConnectorValidationException(this, "'$inputName' parameter must be a String.")
        } else if (value.isEmpty()) {
            throw ConnectorValidationException(this, "Mandatory parameter '$inputName' is missing.")
        }
    }

    fun getOutputParameter(name: String): Any? {
        return super.getOutputParameters()[name]
    }

    public override fun executeBusinessLogic() {
        val client = createSlackClient()
        val token = getInputParameter(TOKEN_INPUT) as String // We do not want to log the secret token!
        val request = createPostMessageRequest()
        val result = client
            .methods(token)
            .chatPostMessage(request)
        if (result.isOk) {
            setOutputParameter(TS_OUTPUT, result.ts)
        } else {
            throw ConnectorException("Send slack message failed with the following error: ${result.error}")
        }
    }

    fun createPostMessageRequest(): ChatPostMessageRequest {
        val channel = getAndLogStringParameter(ID_INPUT)
        val message = getAndLogStringParameter(MESSAGE_INPUT)
        return ChatPostMessageRequest.builder()
            .channel(channel)
            .text(message)
            .build()
    }

    fun createSlackClient(): Slack {
        return Slack.getInstance()
    }

    private fun getAndLogStringParameter(parameterName: String): String {
        val value: String = getInputParameter(parameterName) as String
        logger.info { "$parameterName: $value" }
        return value
    }
}