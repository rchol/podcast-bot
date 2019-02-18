package bot.telegram.commands;

import bot.data.Repository;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class AddCommand implements Command {

    private final Repository repo;

    private List<String> params;

    private String url;

    public AddCommand(String url, List<String> params, Repository repo) {
        this.url = url;
        this.params = params;
        this.repo = repo;
    }

    public AddCommand(String url, Repository repo) {
        this.url = url;
        this.repo = repo;

    }

    @Override
    public void doIt() {
        try {
            // Это очень простая проверка на валидность урла!
            URL urlValid = new URL(url);

            if (params.isEmpty()) {
                repo.addChannel(url);
            } else {
                repo.addChannel(url, params);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Not a valid URL", e);
        }

    }

    private String getHashtagsAsString(List<String> hashtags) {
        StringBuilder builder = new StringBuilder();
        hashtags.forEach(tag -> builder.append("&#35")
            .append(tag)
            //.append(CHANEL_ID)
            .append(" "));
        return builder.toString().toLowerCase();
    }
}
