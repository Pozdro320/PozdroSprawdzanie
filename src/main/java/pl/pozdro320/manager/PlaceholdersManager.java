package pl.pozdro320.manager;

import pl.pozdro320.helper.MessageHelper;

public class PlaceholdersManager {

    public static String replacePlaceholders(String text, String targetName, String moderatorName) {
        if (text == null) return "";
        if (targetName != null) text = text.replace("{PLAYER}", targetName);
        if (moderatorName != null) text = text.replace("{MODERATOR}", moderatorName);
        return MessageHelper.colored(text);
    }

}
