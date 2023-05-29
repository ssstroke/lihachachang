package org.kalimbekov;

import org.kalimbekov.services.DBService;
import org.kalimbekov.services.TranslationService;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws TelegramApiException, FileNotFoundException, SQLException {
        try (
                Scanner dbCredentialsScanner = new Scanner(new File("data/MYSQL_CREDENTIALS"));
                Scanner telegramTokenScanner = new Scanner(new File("data/TELEGRAM_BOT_TOKEN"));
                Scanner openAiTokenScanner = new Scanner(new File("data/OPENAI_API_KEY"))
        ) {
            DBService db = new DBService("localhost", 3306, "lts",
                    dbCredentialsScanner.nextLine(),    // username
                    dbCredentialsScanner.nextLine()     // password
            );

            final String TELEGRAM_BOT_TOKEN = telegramTokenScanner.nextLine();
            final String OPENAI_API_TOKEN = openAiTokenScanner.nextLine();

            TranslationService translationService = new TranslationService(OPENAI_API_TOKEN);

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new LTSBot(TELEGRAM_BOT_TOKEN, db, translationService));
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }
}
