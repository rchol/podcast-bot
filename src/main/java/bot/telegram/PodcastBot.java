package bot.telegram;

import bot.data.H2DataSource;
import bot.data.H2Repository;
import bot.data.Repository;
import bot.rss.RSSService;
import bot.telegram.audio.AudioPreparator;
import bot.telegram.commands.AddCommand;
import bot.telegram.commands.Command;
import bot.telegram.message.TgMsg;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class PodcastBot extends TelegramLongPollingBot {


    private final Repository repo;
    private static final String BOT_USERNAME;
    private static final String BOT_TOKEN;
    private static final String CHANEL_ID;

    private Timer timer = null;

    public PodcastBot() {
        repo = new H2Repository(new H2DataSource());
    }

    @Override
    public void onUpdateReceived(Update update) {
        String commandMsg = update.getMessage().getText();
        List<String> separated = new LinkedList<>(Arrays.asList(commandMsg.split("\\s+")));
        if (separated.remove(0).equals("/add")) {
            String url = separated.remove(0);
            Command addCommand = new AddCommand(url, separated, repo);
            addCommand.doIt();
        }
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new PodcastTask(), 0, 300_000L);
        }
    }

    private int sendMessage(String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(false);
        sendMessage.setChatId(CHANEL_ID);
        sendMessage.disableWebPagePreview();
        sendMessage.setParseMode("HTML");
        sendMessage.setText(text);
        try {
            Message sent = execute(sendMessage);
            return sent.getMessageId();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendAudio(TgMsg post, int replyId) {
        AudioPreparator preparator = new AudioPreparator();
        String audioFile = preparator.retrive(post.getMp3link());
        List<String> filesToSend = preparator.splitAudio(audioFile, 49);
        AtomicInteger idx = new AtomicInteger(1);
        filesToSend.forEach(filename -> {
            File file = new File(filename);
            try (FileInputStream ain = new FileInputStream(file)) {
                SendAudio sendAudio = new SendAudio();
                sendAudio.setChatId(CHANEL_ID);
                String audioName = String.format("%s - %2d", post.getTitle(), idx.getAndIncrement());
                sendAudio.setAudio(audioName, ain);
                sendAudio.setCaption(audioName);
                sendAudio.setReplyToMessageId(replyId);
                execute(sendAudio);
            } catch (IOException | TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
    }


    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    private class PodcastTask extends TimerTask {

        @Override
        public void run() {
            RSSService rss = new RSSService(repo);
            List<TgMsg> posts = rss.getPosts();
            posts.forEach(post -> {
                int replyId = sendMessage(post.buildText());
                sendAudio(post, replyId);
                repo.addMessage(post);
            });
        }
    }
}
