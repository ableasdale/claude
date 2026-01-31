import dev.langchain4j.model.openai.OpenAiChatModel;

public class ModelTest {

    static void main() {

        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("http://localhost:12434/engines/v1")
                //.modelName("ai/smollm2:360M-Q4_K_M")
                .modelName("ai/gemma3")
                .build();

        String answer = model.chat("Give me a fact about whales.");
        System.out.println(answer);
    }
}
