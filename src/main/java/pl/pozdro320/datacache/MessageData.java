package pl.pozdro320.datacache;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import pl.pozdro320.helper.MessageHelper;

@Getter
public class MessageData {
    private final List<String> rawChat;
    private final String rawActionBar;
    private final String rawTitle;
    private final String rawSubtitle;
    private final String sound;
    private final List<String> rawBroadcast;

    private final List<String> coloredChat;
    private final String coloredActionBar;
    private final String coloredTitle;
    private final String coloredSubtitle;
    private final List<String> coloredBroadcast;

    public MessageData(List<String> chat, String actionBar, String title, String subtitle, String sound, List<String> broadcast) {
        this.rawChat = chat != null ? chat : Collections.emptyList();
        this.rawActionBar = actionBar != null ? actionBar : "";
        this.rawTitle = title != null ? title : "";
        this.rawSubtitle = subtitle != null ? subtitle : "";
        this.sound = sound != null ? sound : "";
        this.rawBroadcast = broadcast != null ? broadcast : Collections.emptyList();

        this.coloredChat = this.rawChat.stream()
                .map(MessageHelper::colored)
                .toList();

        this.coloredActionBar = MessageHelper.colored(this.rawActionBar);
        this.coloredTitle = MessageHelper.colored(this.rawTitle);
        this.coloredSubtitle = MessageHelper.colored(this.rawSubtitle);

        this.coloredBroadcast = this.rawBroadcast.stream()
                .map(MessageHelper::colored)
                .toList();
    }
}