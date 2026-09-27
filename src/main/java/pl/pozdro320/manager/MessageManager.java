package pl.pozdro320.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import pl.pozdro320.datacache.MessageData;
import pl.pozdro320.helper.MessageHelper;

public class MessageManager {
    private final Map<String, MessageData> messagesData = new HashMap<>();
    private final FileConfiguration config;

    public MessageManager(FileConfiguration messagesConfig) {
        this.config = messagesConfig;
        this.loadMessages();
    }

    public void loadMessages() {
        this.messagesData.clear();
        ConfigurationSection root = config.getConfigurationSection("messages");
        if (root == null)
            return;

        for (String key : root.getKeys(true)) {
            if (root.isConfigurationSection(key)) {
                ConfigurationSection bundle = root.getConfigurationSection(key);

                if (bundle != null && (bundle.contains("chat") ||
                        bundle.contains("title") ||
                        bundle.contains("subtitle") ||
                        bundle.contains("actionbar") ||
                        bundle.contains("sound") ||
                        bundle.contains("broadcast"))) {

                    List<String> chat = bundle.isList("chat") ? bundle.getStringList("chat")
                            : (bundle.contains("chat") ? List.of(bundle.getString("chat", "")) : new ArrayList<>());

                    List<String> broadcast = bundle.isList("broadcast") ? bundle.getStringList("broadcast")
                            : (bundle.contains("broadcast") ? List.of(bundle.getString("broadcast", "")) : new ArrayList<>());

                    this.messagesData.put(key, new MessageData(
                            chat,
                            bundle.getString("actionbar", ""),
                            bundle.getString("title", ""),
                            bundle.getString("subtitle", ""),
                            bundle.getString("sound", ""),
                            broadcast));
                }
            } else if (root.isString(key)) {
                List<String> simpleChat = new ArrayList<>();
                simpleChat.add(root.getString(key));

                this.messagesData.put(key, new MessageData(
                        simpleChat, "", "", "", "", new ArrayList<>()));
            }
        }
    }

    public void broadcast(String path, String... placeholders) {
        String cleanPath = path.startsWith("messages.") ? path.substring(9) : path;
        MessageData data = messagesData.get(cleanPath);
        if (data == null) return;

        boolean hasPlaceholders = placeholders != null && placeholders.length >= 2;

        List<String> lines = !data.getRawBroadcast().isEmpty() ? data.getRawBroadcast() : data.getRawChat();
        if (lines == null || lines.isEmpty()) return;

        for (String line : lines) {
            String processedLine = hasPlaceholders ? replace(line, placeholders) : line;
            BaseComponent[] component = MessageHelper.parseToComponent(processedLine);
            Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(component));
        }

        if (!data.getSound().isEmpty()) {
            try {
                Sound sound = Sound.valueOf(data.getSound().toUpperCase());
                Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), sound, 1f, 1f));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void sendMessages(CommandSender sender, String path, String... placeholders) {
        if (sender instanceof Player player) {
            sendMessagesToPlayer(player, path, placeholders);
            return;
        }

        MessageData data = messagesData.get(path);
        if (data == null)
            return;

        data.getRawChat().forEach(line -> sender.sendMessage(MessageHelper.colored(replace(line, placeholders))));

        if (!data.getRawBroadcast().isEmpty()) {
            data.getRawBroadcast().forEach(line -> {
                BaseComponent[] component = MessageHelper.parseToComponent(replace(line, placeholders));
                Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(component));
            });
        }
    }

    public void sendMessagesToPlayer(Player player, String path, String... placeholders) {
        MessageData data = messagesData.get(path);
        if (data == null)
            return;

        boolean hasPlaceholders = placeholders != null && placeholders.length >= 2;

        data.getRawChat().forEach(line -> {
            String processedLine = hasPlaceholders ? replace(line, placeholders) : line;
            player.spigot().sendMessage(MessageHelper.parseToComponent(processedLine));
        });

        if (!data.getRawActionBar().isEmpty()) {
            String bar = hasPlaceholders ? MessageHelper.colored(replace(data.getRawActionBar(), placeholders))
                    : data.getColoredActionBar();
            MessageHelper.sendBar(player, bar);
        }

        if (!data.getRawTitle().isEmpty() || !data.getRawSubtitle().isEmpty()) {
            String t = hasPlaceholders ? MessageHelper.colored(replace(data.getRawTitle(), placeholders))
                    : data.getColoredTitle();
            String s = hasPlaceholders ? MessageHelper.colored(replace(data.getRawSubtitle(), placeholders))
                    : data.getColoredSubtitle();
            player.sendTitle(t, s, 10, 40, 10);
        }

        if (!data.getSound().isEmpty()) {
            try {
                Sound sound = Sound.valueOf(data.getSound().toUpperCase());
                player.playSound(player.getLocation(), sound, 1f, 1f);
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (!data.getRawBroadcast().isEmpty()) {
            data.getRawBroadcast().forEach(line -> {
                String processedLine = hasPlaceholders ? replace(line, placeholders) : line;
                BaseComponent[] component = MessageHelper.parseToComponent(processedLine);
                Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(component));
            });
        }
    }

    public void broadcastJoinMessage(Player enteringPlayer, String path) {
        MessageData data = messagesData.get(path);
        if (data == null)
            return;

        data.getRawChat().forEach(line -> {
            String formattedLine = formatPlaceholders(enteringPlayer, line);
            BaseComponent[] component = MessageHelper.parseToComponent(formattedLine);
            Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(component));
        });

        if (!data.getRawActionBar().isEmpty()) {
            String formattedBar = formatPlaceholders(enteringPlayer, data.getRawActionBar());
            String coloredBar = MessageHelper.colored(formattedBar);
            Bukkit.getOnlinePlayers().forEach(p -> MessageHelper.sendBar(p, coloredBar));
        }

        if (!data.getRawTitle().isEmpty() || !data.getRawSubtitle().isEmpty()) {
            String formattedTitle = formatPlaceholders(enteringPlayer, data.getRawTitle());
            String formattedSub = formatPlaceholders(enteringPlayer, data.getRawSubtitle());

            String t = MessageHelper.colored(formattedTitle);
            String s = MessageHelper.colored(formattedSub);
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(t, s, 10, 40, 10));
        }

        if (!data.getSound().isEmpty()) {
            try {
                Sound sound = Sound.valueOf(data.getSound().toUpperCase());
                Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), sound, 1f, 1f));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public String getRawMessage(String path) {
        String cleanPath = path.startsWith("messages.") ? path.substring(9) : path;

        MessageData data = messagesData.get(cleanPath);
        if (data == null || data.getRawChat().isEmpty()) {
            return "§cError: " + cleanPath;
        }

        return data.getRawChat().get(0);
    }

    public String getRawMessage(String path, String... placeholders) {
        String message = getRawMessage(path);
        if (placeholders != null && placeholders.length >= 2) {
            return replace(message, placeholders);
        }
        return message;
    }

    private String replace(String text, String... placeholders) {
        if (text == null || placeholders == null || placeholders.length < 2)
            return text;
        String result = text;
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                result = result.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        return result;
    }

    public String formatPlaceholders(Player player, String text) {
        if (text == null)
            return "";

        if (pl.pozdro320.PozdroSprawdzanieMain.isPlaceholderAPIEnabled()) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }

        return text;
    }

    public FileConfiguration getConfig() {
        return config;
    }
}