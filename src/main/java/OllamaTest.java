import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class OllamaTest {

    static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {

        LOG.info("Starting chat with local LLM model: " + Consts.GPT_OSS_20B);
        ChatModel model = OllamaChatModel.builder()
                .baseUrl(Consts.OLLAMA_BASE_URL)
                .modelName(Consts.GPT_OSS_20B)
                .build();

        //String answer = model.chat("Give me a fact about whales.");
        //String answer = model.chat("What is your name?");
        //String answer = model.chat("Explain String Theory to me");
        //String answer2 = model.chat("write me a Java Hello World class");
        // String answer3 = model.chat("how long is a piece of string?");
        String answer3 = model.chat("Hello");
        LOG.info(answer3);

    }
}
