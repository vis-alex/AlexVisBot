package com.alex.vis;

import com.alex.vis.entity.Currency;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AlexVisBot extends TelegramLongPollingBot
{
    //Заменяет throws Exception в сигнатуре метода
    @SneakyThrows
    public static void main(String[] args ) {
        //В DefaultBootOptions хранится информация об url на который отправляется какая то информация,
        // время , таймаут, лимиту и прочее
        AlexVisBot bot = new AlexVisBot();
        //Для регистрации бота
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        //После регистрации бота создался отдельный поток который будет непрерывно отправлять сообщения
        // на телеграмм getUpdate, и если пришел новый апдейт он вызовет onUpdateReceive()
        telegramBotsApi.registerBot(bot);
    }

    @Override
    public String getBotUsername() {
        return "AlexVisBot";
    }

    @Override
    public String getBotToken() {
        return "5282339710:AAHGUZjoi3HJQ7gskWvC2c7TnBZmyUGYpaE";
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        //Мы добавили чат боте прямо в телеграмме команду /set_currency
        if (update.hasMessage()) {
            handleMessage(update.getMessage());
        }
    }

    @SneakyThrows
    private void handleMessage(Message message) {
        //обрабатываем команду
        if (message.hasText() && message.hasEntities()) {
            Optional<MessageEntity> commandEntity = message.getEntities().stream()
                    .filter(e -> "bot_command".equals(e.getType()))
                    .findFirst();

            if (commandEntity.isPresent()) {
                String command = message.getText().substring(commandEntity.get().getOffset(), commandEntity.get().getLength());

                switch(command) {
                    case "/set_currency":
                        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
                        for (Currency currency : Currency.values()) {
                            buttons.add(
                                    Arrays.asList(
                                            /* тут мы должны указать какой то URL, опалата ли это pay(), или callBackData
                                            Обычно в callBackData зашивается какойто json обьект,
                                            чтобы при нажатии кнопки к нам приходила только callBackData и нам чтобы знать надо хзашивать в нее команду
                                            однако в нашем простом боте будет только один юз кейс выбор валюты*/
                                            InlineKeyboardButton.builder().text(currency.name()).callbackData("ORIGINAL:" + currency).build(),
                                            InlineKeyboardButton.builder().text(currency.name()).callbackData("TARGET:" + currency).build()
                                    ));
                        }

                        execute(SendMessage.builder()
                                .chatId(message.getChatId().toString())
                                .text("Please choose Original and Target currency")
                                //Это кнопки и всякие такие элементы. Сюда мы предоставляем их массив
                                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                        .build());
                        return;
                }
            }
        }
    }
}
