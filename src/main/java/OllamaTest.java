import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

public class OllamaTest {
    static void main() {
        // Works against a model hosted using Ollama (gemma3:1b)

        ChatModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                //.modelName("ai/smollm2:360M-Q4_K_M")
                .modelName("gemma3:1b")
                .build();

        //String answer = model.chat("Give me a fact about whales.");
        String answer = model.chat("What is your name?");
        System.out.println(answer);
    }
}
