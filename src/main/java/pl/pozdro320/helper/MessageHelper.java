package pl.pozdro320.helper;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageHelper {
    private static final Pattern MULTI_GRADIENT_PATTERN = Pattern.compile("<gradient:((#[a-fA-F0-9]{6}:?)+)>(.*?)</gradient>");
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})|#([a-fA-F0-9]{6})");
    private static final Pattern LINK_PATTERN = Pattern.compile("<link:(.*?)>(.*?)</link>");

    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    private final String message;

    private MessageHelper(final String message) {
        this.message = message;
    }

    public static void clearCache() {
        CACHE.clear();
    }

    public static MessageHelper build(final String message) {
        return new MessageHelper(message);
    }

    public static String colored(String message) {
        if (message == null || message.isEmpty()) return "";

        return CACHE.computeIfAbsent(message, msg -> {
            String processed = msg;

            processed = processed.replace("<b>", "&l").replace("</b>", "&r")
                            .replace("<i>", "&o").replace("</i>", "&r")
                            .replace("<u>", "&n").replace("</u>", "&r")
                            .replace("<obf>", "&k").replace("</obf>", "&r")
                            .replace("<strike>", "&m").replace("</strike>", "&r")
                            .replace(">>", "»");

            processed = ChatColor.translateAlternateColorCodes('&', processed);

            Matcher gradientMatcher = MULTI_GRADIENT_PATTERN.matcher(processed);
            while (gradientMatcher.find()) {
                String colorsStr = gradientMatcher.group(1);
                String content = gradientMatcher.group(3);
                String[] hexes = colorsStr.split(":");
                processed = processed.replace(gradientMatcher.group(), applyMultiGradient(content, hexes));
            }

            Matcher hexMatcher = HEX_PATTERN.matcher(processed);
            StringBuilder buffer = new StringBuilder();
            while (hexMatcher.find()) {
                String hex = hexMatcher.group(1) != null ? hexMatcher.group(1) : hexMatcher.group(2);
                hexMatcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
            }
            hexMatcher.appendTail(buffer);

            return buffer.toString();
        });
    }

    public static BaseComponent[] parseToComponent(String message) {
        if (message == null || message.isEmpty()) return new BaseComponent[]{new TextComponent("")};

        TextComponent root = new TextComponent("");
        Matcher matcher = LINK_PATTERN.matcher(message);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String beforeText = message.substring(lastEnd, matcher.start());
                for (BaseComponent comp : TextComponent.fromLegacyText(colored(beforeText))) {
                    root.addExtra(comp);
                }
            }

            String url = matcher.group(1);
            String linkText = matcher.group(2);

            TextComponent linkComponent = new TextComponent("");
            for (BaseComponent comp : TextComponent.fromLegacyText(colored(linkText))) {
                linkComponent.addExtra(comp);
            }

            linkComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
            root.addExtra(linkComponent);

            lastEnd = matcher.end();
        }

        if (lastEnd < message.length()) {
            String afterText = message.substring(lastEnd);
            for (BaseComponent comp : TextComponent.fromLegacyText(colored(afterText))) {
                root.addExtra(comp);
            }
        }

        return new BaseComponent[]{root};
    }

    private static String applyMultiGradient(String text, String[] hexes) {
        StringBuilder builder = new StringBuilder();
        List<Color> colors = new ArrayList<>();
        for (String hex : hexes) {
            colors.add(Color.decode(hex));
        }

        StringBuilder styles = new StringBuilder();
        String cleanText = text;
        if (text.contains("§")) {
            Matcher styleMatcher = Pattern.compile("§[l-okmn]").matcher(text);
            while (styleMatcher.find()) {
                styles.append(styleMatcher.group());
            }
            cleanText = text.replaceAll("§[l-okm n]", "");
        }

        int len = cleanText.length();
        if (len == 0) return "";

        for (int i = 0; i < len; i++) {
            float globalRatio = (float) i / (float) (len > 1 ? len - 1 : 1);
            float section = globalRatio * (colors.size() - 1);
            int index = (int) section;
            float localRatio = section - index;

            if (index >= colors.size() - 1) {
                index = colors.size() - 2;
                localRatio = 1.0f;
            }

            Color start = colors.get(index);
            Color end = colors.get(index + 1);

            int r = (int) (start.getRed() * (1 - localRatio) + end.getRed() * localRatio);
            int g = (int) (start.getGreen() * (1 - localRatio) + end.getGreen() * localRatio);
            int b = (int) (start.getBlue() * (1 - localRatio) + end.getGreen() * localRatio);

            builder.append(ChatColor.of(new Color(r, g, b)))
                    .append(styles.toString())
                    .append(cleanText.charAt(i));
        }
        return builder.toString();
    }

    public static List<String> colored(final List<String> texts) {
        return texts.stream().map(MessageHelper::colored).collect(Collectors.toList());
    }

    public static void sendBar(Player player, String text) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(colored(text)));
    }

    public static void sendTitle(Player p, String title, String subttitle) {
        sendTitle(p, title, subttitle, 10, 40, 10);
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (title == null) title = "";
        if (subtitle == null) subtitle = "";
        player.sendTitle(colored(title), colored(subtitle), fadeIn, stay, fadeOut);
    }

    public void send(final Player player) {
        player.spigot().sendMessage(parseToComponent(message));
    }

    public void broadcast() {
        Bukkit.getOnlinePlayers().forEach(this::send);
    }

    public void send(CommandSender sender) {
        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(parseToComponent(message));
        } else {
            sender.sendMessage(colored(message));
        }
    }
}