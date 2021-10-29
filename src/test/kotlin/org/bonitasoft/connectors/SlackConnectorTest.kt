package org.bonitasoft.connectors

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.bonitasoft.engine.connector.ConnectorValidationException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.every
import com.slack.api.Slack
import com.slack.api.methods.MethodsClient
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.model.block.Blocks
import com.slack.api.model.block.composition.BlockCompositions.plainText
import org.bonitasoft.connectors.model.SlackConnectorBlocks
import org.bonitasoft.engine.connector.ConnectorException

class SlackConnectorTest {

    lateinit var connector: SlackConnector

    @BeforeEach
    fun setUp() {
        connector = SlackConnector()
    }

    @Test
    fun `should throw exception if mandatory input is missing`() {
        val params1 = mapOf(SlackConnector.TOKEN_INPUT to null, SlackConnector.ID_INPUT to "id")
        val params2 = mapOf(SlackConnector.TOKEN_INPUT to "token", SlackConnector.ID_INPUT to null)

        connector.setInputParameters(params1)
        assertThatThrownBy { connector.validateInputParameters() }
                .isExactlyInstanceOf(ConnectorValidationException::class.java)
        
        connector.setInputParameters(params2)
        assertThatThrownBy { connector.validateInputParameters() }
                .isExactlyInstanceOf(ConnectorValidationException::class.java)
    }

    @Test
    fun `should throw exception if mandatory input is empty`() {
        val params1 = mapOf(SlackConnector.TOKEN_INPUT to "", SlackConnector.ID_INPUT to "id")
        val params2 = mapOf(SlackConnector.TOKEN_INPUT to "token", SlackConnector.ID_INPUT to "")

        connector.setInputParameters(params1)
        assertThatThrownBy { connector.validateInputParameters() }
                .isExactlyInstanceOf(ConnectorValidationException::class.java)
        
        connector.setInputParameters(params2)
        assertThatThrownBy { connector.validateInputParameters() }
                .isExactlyInstanceOf(ConnectorValidationException::class.java)
    }

    @Test
    fun `should throw exception if mandatory input is not a string`() {
        val params1 = mapOf(SlackConnector.TOKEN_INPUT to 1, SlackConnector.ID_INPUT to "id")
        val params2 = mapOf(SlackConnector.TOKEN_INPUT to "token", SlackConnector.ID_INPUT to 1)

        connector.setInputParameters(params1)
        assertThatThrownBy { connector.validateInputParameters() }
                .isExactlyInstanceOf(ConnectorValidationException::class.java)
        
        connector.setInputParameters(params2)
        assertThatThrownBy { connector.validateInputParameters() }
                .isExactlyInstanceOf(ConnectorValidationException::class.java)
    }

    @Test
    fun `should throw exception if message and blocks are empty`() {
        val params1 = mapOf(SlackConnector.TOKEN_INPUT to "token", SlackConnector.ID_INPUT to "id")
        val params2 = mapOf(SlackConnector.TOKEN_INPUT to "token", SlackConnector.ID_INPUT to "id",
            SlackConnector.MESSAGE_INPUT to "")
        val params3 = mapOf(SlackConnector.TOKEN_INPUT to "token", SlackConnector.ID_INPUT to "id",
            SlackConnector.BLOCKS_INPUT to SlackConnectorBlocks())

        connector.setInputParameters(params1)
        assertThatThrownBy { connector.validateInputParameters() }
            .isExactlyInstanceOf(ConnectorValidationException::class.java)

        connector.setInputParameters(params2)
        assertThatThrownBy { connector.validateInputParameters() }
            .isExactlyInstanceOf(ConnectorValidationException::class.java)

        connector.setInputParameters(params3)
        assertThatThrownBy { connector.validateInputParameters() }
            .isExactlyInstanceOf(ConnectorValidationException::class.java)
    }

    @Test
    fun `should validate valid input`() {
        val params1 = mapOf(SlackConnector.TOKEN_INPUT to "token",
            SlackConnector.ID_INPUT to "id",
            SlackConnector.MESSAGE_INPUT to "message")

        val blocks = SlackConnectorBlocks()
        blocks.addBlockAtTheEnd(Blocks.section { section -> section.text(plainText("Hello!")) })
        val params2 = mapOf(SlackConnector.TOKEN_INPUT to "token",
            SlackConnector.ID_INPUT to "id",
            SlackConnector.BLOCKS_INPUT to blocks)

        connector.setInputParameters(params1)
        connector.validateInputParameters()

        connector.setInputParameters(params2)
        connector.validateInputParameters()
    }
    
    @Test
    fun `should set output parameter when result is OK`() {
        // given
        val expectedOutput = "ts"
        val slack = mockk<Slack>()
        val methodsClient = mockk<MethodsClient>()
        val request = mockk<ChatPostMessageRequest>()
        val result  = ChatPostMessageResponse()
        result.isOk = true
        result.ts = expectedOutput
        connector = spyk()
        
        every { methodsClient.chatPostMessage(request) } returns result
        every { slack.methods(any()) } returns methodsClient
        every { connector.createSlackClient() } returns slack
        every { connector.createPostMessageRequest() } returns request
        
        val params = mapOf(SlackConnector.TOKEN_INPUT to "token",
            SlackConnector.ID_INPUT to "id",
            SlackConnector.MESSAGE_INPUT to "message",
            SlackConnector.BLOCKS_INPUT to SlackConnectorBlocks())

        connector.setInputParameters(params)
        
        // when
        connector.executeBusinessLogic()
        
        // then
        assertThat(connector.getOutputParameter(SlackConnector.TS_OUTPUT)).isEqualTo(expectedOutput)
    }
    
    @Test
    fun `should throw exception when result is not ok`() {
        // given
        val slack = mockk<Slack>()
        val methodsClient = mockk<MethodsClient>()
        val request = mockk<ChatPostMessageRequest>()
        val result  = ChatPostMessageResponse()
        result.isOk = false
        connector = spyk()
        
        every { methodsClient.chatPostMessage(request) } returns result
        every { slack.methods(any()) } returns methodsClient
        every { connector.createSlackClient() } returns slack
        every { connector.createPostMessageRequest() } returns request
        
        val params = mapOf(SlackConnector.TOKEN_INPUT to "token", SlackConnector.ID_INPUT to "id", SlackConnector.MESSAGE_INPUT to "message")
        connector.setInputParameters(params)
        
        // when
        assertThatThrownBy { connector.executeBusinessLogic() }
                // then
                .isExactlyInstanceOf(ConnectorException::class.java)
    }
}