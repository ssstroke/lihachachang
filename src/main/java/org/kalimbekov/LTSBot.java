package org.kalimbekov;

import com.ptsmods.mysqlw.Pair;
import org.kalimbekov.entities.*;
import org.kalimbekov.services.DBService;
import org.kalimbekov.services.TranslationService;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class LTSBot extends TelegramLongPollingBot {
    private final String COMMAND_START_MESSAGE = """
                            Рад приветстовать тебя, друг! Спасибо за выбор сервиса Lihachachang! 👋
                            Я очень надеюсь, что время, проведённое со мной, принесёт тебе немало пользы!
                            Отправь мне одну из доступных команд, чтобы начать свой путь в изучении иностранных языков!
                            """;
    private final String COMMAND_UNRECOGNISED_MESSAGE = """
                            Мне жаль, но я не понимаю, как мне реагировать на эту команду... 😶
                            Посмотри список доступных команд в меню!
                            """;
    private final String COMMAND_TRANSLATE_MESSAGE =
            "Введите название (только название) языка, на который вы хотите перевести текст, а " +
                    "на новой строчке напишите сам текст. Язык оригинала я определю сам!";
    private final String COMMAND_ADD_WORD =
            "Введите название (только название) языка, который будет использоваться для проверки слова в дальнейшем," +
                    " а на новой строке введите само слово. Не нужно писать перевод слова, я сделаю это за вас!";

    private final DBService db;
    private final TranslationService translationService;

    @Override
    public String getBotUsername() {
        return "lihachachang_bot";
    }

    public LTSBot(String botToken, DBService db, TranslationService translationService) {
        super(new DefaultBotOptions(), botToken);
        this.db = db;
        this.translationService = translationService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(update);
        System.out.println(update.getCallbackQuery());

        Message message = update.getMessage();

        long chatId;
        if (message != null)
            chatId = message.getFrom().getId();
        else
            chatId = update.getCallbackQuery().getFrom().getId();

        User user = db.getUserByChatId(chatId);
        State state;
        if (user == null) {
            Pair<User, State> pair = db.createUser(chatId);

            user = pair.getLeft();
            state = pair.getRight();
        } else {
            state = db.getStateByUserId(user.getId());
        }

        if (message != null && message.isCommand())
        {
            String messageText = message.getText();
            switch (messageText) {
                case "/start" -> sendText(chatId, COMMAND_START_MESSAGE);
                case "/translate" -> {
                    sendText(user.getChatId(), COMMAND_TRANSLATE_MESSAGE);

                    state.setDescription(State.AWAITS_ORIGINAL_TEXT);
                    db.updateState(state);
                }
                case "/train_translate" -> {
                    Text text = db.getRandomText();

                    sendText(user.getChatId(),
                            "Переведите следующий текст на русский язык:\n" + text.getQuestion());

                    state.setDescription(State.AWAITS_TRANSLATION);
                    state.setText(text);
                    db.updateState(state);
                } case "/train_fill_gaps" -> {
                    Task task = db.getRandomTask();

                    state.setDescription(State.AWAITS_GAP_FILL);
                    state.setTask(task);
                    db.updateState(state);

                    sendAnswerOptions(user.getChatId(), task);
                } case "/add_word" -> {
                    state.setDescription(State.AWAITS_WORD);
                    db.updateState(state);

                    sendText(user.getChatId(), COMMAND_ADD_WORD);
                } case "/get_word" -> {
                    Word word = db.getRandomWord(user);
                    if (word == null) {
                        sendText(user.getChatId(),
                                "Вы не добавили ни одного слова! Исправьте это при помощи команды /add_word");
                    } else {
                        state.setWord(word);
                        state.setDescription(State.AWAITS_WORD_TRANSLATION);
                        db.updateState(state);

                        sendText(user.getChatId(),
                                "Введите перевод слова '" + word.getWord() + "'");
                    }
                } case "/reset" -> {
                    sendText(user.getChatId(),
                            "Вы уверены, что хотите удалить ВСЕ свои данные? Для подтверждения напишите:\n" +
                                    "Да, я хочу удалить данные для аккаунта " +
                                    message.getFrom().getUserName()
                            );

                    state.setDescription(State.AWAITS_RESET_CONFIRMATION);
                    db.updateState(state);
                }
                default -> sendText(chatId, COMMAND_UNRECOGNISED_MESSAGE);
            }
        }
        else if (update.hasCallbackQuery()) // Filling the gaps is the only way when CallbackQuery is being sent
        {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String answer = callbackQuery.getData();

            Task task = state.getTask();
            String correctAnswer = task.getAnswerOptions()[task.getAnswer()];

            if (answer.equals(correctAnswer)) {
                user.setPoints(user.getPoints() + task.getPoints());
                db.updateUser(user);

                state.setDescription(null);
                db.updateState(state);

                sendText(chatId,
                        "Верно! Продолжайте в том же духе!" +
                                "\nНачислено очков: " + task.getPoints() +
                                "\nВсего очков: " + user.getPoints()
                );
            } else {
                sendText(chatId,
                        "Неверно! Попробуйте ещё раз."
                );
            }
        }
        else
        {
            assert message != null;
            String messageText = message.getText();

            switch (state.getDescription()) {
                case State.AWAITS_ORIGINAL_TEXT -> {
                    String[] languageAndText = messageText.split("\\R", 2);
                    if (languageAndText.length < 2) {
                        sendText(user.getChatId(),
                                "Название языка и текст должны быть на разных строках.");
                    } else {
                        sendText(user.getChatId(), "Формирую ответ ⏳");

                        String translation = translationService.getTranslation(
                                languageAndText[0],
                                languageAndText[1]
                        );

                        state.setDescription(null);
                        db.updateState(state);

                        sendText(user.getChatId(), translation);
                    }
                } case State.AWAITS_TRANSLATION -> {
                    Text text = state.getText();

                    String result = translationService.checkTranslation(text.getQuestion(), messageText);
                    if (result.equals("Верно! Отличная работа!")) {
                        user.setPoints(user.getPoints() + text.getPoints());
                        result += "\nНачислено очков: " + text.getPoints() +
                                "\nВсего очков: " + user.getPoints();
                    }

                    state.setDescription(null);
                    db.updateState(state);
                    db.updateUser(user);

                    sendText(user.getChatId(), result);
                } case State.AWAITS_GAP_FILL -> {
                    Task task = state.getTask();

                    String answer = task.getAnswerOptions()[task.getAnswer()];
                    if (messageText.equals(answer)) {
                        user.setPoints(user.getPoints() + task.getPoints());
                        sendText(chatId,
                                "Верно! Так держать!" +
                                        "\nНачислено очков: " + task.getPoints() +
                                        "\nВсего очков: " + user.getPoints()
                                );
                        state.setDescription(null);
                        db.updateState(state);
                        db.updateUser(user);
                    } else {
                        sendText(chatId,
                                "Неверно! Попробуйте ещё раз."
                        );
                    }
                } case State.AWAITS_WORD -> {
                    String[] languageAndWord = messageText.split("\\R", 2);
                    if (languageAndWord.length < 2) {
                        sendText(user.getChatId(),
                                "Название языка и само слово должны быть на разных строках.");
                    } else {
                        String answer = translationService.getWordTranslation(
                                languageAndWord[0],
                                languageAndWord[1]
                        );

                        db.createWord(user, languageAndWord[1], answer);

                        state.setDescription(null);
                        db.updateState(state);

                        sendText(user.getChatId(),
                                "Слово было добавлено в ваш список!\n" +
                                        "\nИспользуйте " + answer + " для перевода" +
                                        "\nИспользуйте /get_word для получения случайного слова");
                    }
                } case State.AWAITS_WORD_TRANSLATION -> {
                    Word word = state.getWord();

                    if (messageText.equalsIgnoreCase(word.getAnswer())) {
                        sendText(user.getChatId(),
                                "Верно! Так держать!");

                        state.setDescription(null);
                        db.updateState(state);
                    }
                    else
                        sendText(user.getChatId(),
                                "Неверно! Попробуйте ещё раз.");
                } case State.AWAITS_RESET_CONFIRMATION -> {
                    if (messageText.equals("Да, я хочу удалить данные для аккаунта " +
                            message.getFrom().getUserName())) {
                        db.deleteUser(user);
                        sendText(chatId,
                                "Все ваши данные были успешно удалены с моего сервера... \uD83D\uDE22");
                    }
                }
            }
        }
    }

    public void sendText(long chatId, String message) {
        SendMessage sm = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .build();

        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendAnswerOptions(long chatId, Task task) {
        String[] answerOptions = task.getAnswerOptions();

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        ArrayList<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = 0; i != answerOptions.length; i++) { // This will make list a bit prettier
            if (i % 2 == 0) {
                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                inlineKeyboardButton.setText(answerOptions[i]);
                inlineKeyboardButton.setCallbackData(answerOptions[i]);

                row.add(inlineKeyboardButton);
            } else {
                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                inlineKeyboardButton.setText(answerOptions[i]);
                inlineKeyboardButton.setCallbackData(answerOptions[i]);

                row.add(inlineKeyboardButton);
                buttons.add(new ArrayList<>(row));

                row.clear();
            }
        }
        if (answerOptions.length % 2 == 1)
            buttons.add(row);
        inlineKeyboardMarkup.setKeyboard(buttons);

        SendMessage sm = SendMessage.builder()
                .chatId(chatId)
                .text(task.getQuestion())
                .replyMarkup(inlineKeyboardMarkup)
                .build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
