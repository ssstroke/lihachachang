package org.kalimbekov.services;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import java.util.List;

public class TranslationService {
    private String OPENAI_API_TOKEN = "";

    public TranslationService(String token) {
        this.OPENAI_API_TOKEN = token;
    }

    public String getWordTranslation(String translateTo, String word) {
        final String SYSTEM_TASK_MESSAGE =
                "You are a part of application for studying foreign languages. I will give you a word for you " +
                        "to translate. You should translate it to '" + translateTo + "' language and respond in " +
                        "a specific format:\n" +
                        "<translation of a word in lowercase>"
                ;

        OpenAiService service = new OpenAiService(this.OPENAI_API_TOKEN);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .temperature(0.2)
                .messages(
                        List.of(
                                new ChatMessage("system", SYSTEM_TASK_MESSAGE),
                                new ChatMessage("user", word)
                        )
                )
                .build();

        return service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage().getContent();
    }

    public String getTranslation(String translateTo, String originalText) {
        final String SYSTEM_TASK_MESSAGE =
                "You are a part of application for studying foreign languages. I will give you a text for you to" +
                        "translate. You should detect a language of original text, translate it to '"
                        + translateTo + "' language and respond in a specific format:\n" +
                        "Язык текста: <language you have detected>" +
                        "Перевод: <translation>"
                ;

        OpenAiService service = new OpenAiService(this.OPENAI_API_TOKEN);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .temperature(0.5)
                .messages(
                        List.of(
                                new ChatMessage("system", SYSTEM_TASK_MESSAGE),
                                new ChatMessage("user", originalText)
                        )
                )
                .build();

        return service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage().getContent();
    }

    public String detectLanguage(String originalText) {
        final String SYSTEM_TASK_MESSAGE = """
                You are a part of application for studying foreign languages. I will give you a text for you to
                detect a language. You should detect a language of original text and your confidence in the answer
                (confidence must be a percent value). You should respond in a specific format:
                Язык текста: <language you have detected in Russian language>
                Вероятность правильного определения: <your confidence>
                """
                ;

        OpenAiService service = new OpenAiService(this.OPENAI_API_TOKEN);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .temperature(0.7)
                .messages(
                        List.of(
                                new ChatMessage("system", SYSTEM_TASK_MESSAGE),
                                new ChatMessage("user", originalText)
                        )
                )
                .build();

        return service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage().getContent();
    }

    public String checkTranslation(String originalText, String translatedText) {
        final String SYSTEM_TASK_MESSAGE = """
                You are a part of application for studying foreign languages. I will give you two texts: first one is
                in English language and the second one is in Russian language. You should check if text in Russian
                is a correct translation of the text in English. You should respond in a specific format:
                "Верно! Отличная работа!" if translation is correct;
                "Неверно! Правильным переводом будет: "<correct translation>"" if translation is wrong.
                """
                ;

        OpenAiService service = new OpenAiService(this.OPENAI_API_TOKEN);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .temperature(0.7)
                .messages(
                        List.of(
                                new ChatMessage("system", SYSTEM_TASK_MESSAGE),
                                new ChatMessage("user",
                                        "Original text: " + originalText + '\n' +
                                                "Russian translation: " + translatedText
                                        )
                        )
                )
                .build();

        return service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage().getContent();
    }
}
