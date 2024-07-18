package br.com.alura.screenmatch.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.List;

public class ConsultaChatGPT {
  public static String obterTraducao(String texto) {
    OpenAiService service = new OpenAiService(System.getenv("OPENAI_APIKEY"));

//    CompletionRequest requisicao = CompletionRequest.builder()
//            .model("gpt-3.5-turbo-instruct")
////            .model("text-davinci-003")
//            .prompt("traduza para o português (Brasil) o texto: " + texto)
//            .maxTokens(1000)
//            .temperature(0.7)
//            .build();
//
//    var resposta = service.createCompletion(requisicao);
//    return resposta.getChoices().get(0).getText();

    List<ChatMessage> messages = new ArrayList<>();
    ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), "traduza para o português (Brasil) o texto: " + texto);
    messages.add(userMessage);
    ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
            .builder()
            .model("gpt-3.5-turbo-0125")
            .messages(messages)
            .maxTokens(256)
            .build();
    String resposta = service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage().getContent().toString();

    System.out.println(resposta);
    return resposta;
  }
}
