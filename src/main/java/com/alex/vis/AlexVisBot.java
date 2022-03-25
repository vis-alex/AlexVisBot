package com.alex.vis;

import com.alex.vis.entity.Currency;
import com.alex.vis.service.CurrencyConversionService;
import com.alex.vis.service.CurrencyModeService;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
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

public class AlexVisBot extends TelegramLongPollingBot {

    private final CurrencyModeService currencyModeService = CurrencyModeService.getInstance();
    private final CurrencyConversionService conversionService = CurrencyConversionService.getInstance();

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

    //Метод для вывода в чате смайлика валюты рядом с текущей выбранной валютой
    private String getCurrencyButton(Currency saved, Currency current) {
        return saved == current ?  current + "✅" :  current.name();
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
        //CallBackQuery - это нажатие на кнопку в чате. Тут делаем его обработку
        if (update.hasCallbackQuery()) {
            handleCallBack(update.getCallbackQuery());
        }

        //Мы добавили чат боте прямо в телеграмме команду /set_currency
        if (update.hasMessage()) {
            handleMessage(update.getMessage());
        }
    }

    @SneakyThrows
    private void handleCallBack(CallbackQuery callbackQuery) {
        Message message = callbackQuery.getMessage();
        //getData() - Тут наш payLoad - полезная нагрузка. у нас тут зашита Currency: имя валюты
        String[] param = callbackQuery.getData().split(":");
        String action = param[0];
        Currency newCurrency = Currency.valueOf(param[1]);

        switch (action) {
            case "ORIGINAL":
                currencyModeService.setOriginalCurrency(message.getChatId(), newCurrency);
                break;
            case "TARGET":
                currencyModeService.setTargetCurrency(message.getChatId(), newCurrency);
                break;
        }

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
        Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());

        for (Currency currency : Currency.values()) {
            buttons.add(
                    Arrays.asList(
                                            /* тут мы должны указать какой то URL, опалата ли это pay(), или callBackData
                                            Обычно в callBackData зашивается какойто json обьект,
                                            чтобы при нажатии кнопки к нам приходила только callBackData и нам чтобы знать надо хзашивать в нее команду
                                            однако в нашем простом боте будет только один юз кейс выбор валюты*/
                            InlineKeyboardButton.builder()
                                    .text(getCurrencyButton(originalCurrency, currency))
                                    .callbackData("ORIGINAL:" + currency)
                                    .build(),
                            InlineKeyboardButton.builder()
                                    .text(getCurrencyButton(targetCurrency, currency))
                                    .callbackData("TARGET:" + currency)
                                    .build()
                    ));
        }
        //Можно использовать метод UpdateEditMessage но мы используем EditMessageReplyMarkUp для изменения исключительно кнопок
        //Мы обновляем галочки у выбранных валют
        execute(
                EditMessageReplyMarkup.builder()
                        .chatId(message.getChatId().toString())
                        .messageId(message.getMessageId())
                        .replyMarkup(InlineKeyboardMarkup.builder()
                                .keyboard(buttons)
                                .build())
                        .build());
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
                        Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
                        Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());

                        for (Currency currency : Currency.values()) {
                            buttons.add(
                                    Arrays.asList(
                                            /* тут мы должны указать какой то URL, опалата ли это pay(), или callBackData
                                            Обычно в callBackData зашивается какойто json обьект,
                                            чтобы при нажатии кнопки к нам приходила только callBackData и нам чтобы знать надо хзашивать в нее команду
                                            однако в нашем простом боте будет только один юз кейс выбор валюты*/
                                            InlineKeyboardButton.builder()
                                                    .text(getCurrencyButton(originalCurrency, currency))
                                                    .callbackData("ORIGINAL:" + currency)
                                                    .build(),
                                            InlineKeyboardButton.builder()
                                                    .text(getCurrencyButton(targetCurrency, currency))
                                                    .callbackData("TARGET:" + currency)
                                                    .build()
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

        if (message.hasText()) {
            String text = message.getText();
            Optional<Double> value = parseDouble(text);
            Currency original = currencyModeService.getOriginalCurrency(message.getChatId());
            Currency target = currencyModeService.getTargetCurrency(message.getChatId());
            double ratio = conversionService.getConversionRatio(original, target);

            if (value.isPresent()) {
                //Так мы выводм сообщения  в чат
                execute(SendMessage.builder()
                        .chatId(message.getChatId().toString())
                        .text(String.format(
                                "%4.2f %s is %4.2f %s",
                                value.get(),
                                original,
                                value.get() * ratio,
                                target
                        ))
                        .build());
            }
        }
    }

    private Optional<Double> parseDouble(String text) {
        try {
            return Optional.of(Double.parseDouble(text));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
