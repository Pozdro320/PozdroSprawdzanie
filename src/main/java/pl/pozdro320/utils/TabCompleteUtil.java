package pl.pozdro320.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class TabCompleteUtil {

    private TabCompleteUtil() {}

    /**
     * @param sender CommandSender
     * @param args argumenty komendy
     * @param options mapa: tekst -> permisja (null = bez permisji)
     */

    public static List<String> tab(
            CommandSender sender,
            String[] args,
            Map<String, String> options
    ) {
        List<String> completions = new ArrayList<>();

        if (!(sender instanceof Player)) {
            return completions;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            return completions;
        }

        for (Map.Entry<String, String> entry : options.entrySet()) {
            String option = entry.getKey();
            String permission = entry.getValue();

            if (permission == null || player.hasPermission(permission)) {
                completions.add(option);
            }
        }

        return StringUtil.copyPartialMatches(
                args[0],
                completions,
                new ArrayList<>()
        );
    }

    /**
     * @param args argumenty komendy
     * @param targetIndex indeks, na którym podpowiedź ma się wyświetlić (0 to pierwszy argument, 1 to drugi itd.)
     * @param options lista dostępnych opcji do wyboru
     */
    public static List<String> tab(
            String[] args,
            int targetIndex,
            List<String> options
    ) {
        if (args.length != (targetIndex + 1)) {
            return new ArrayList<>();
        }

        return StringUtil.copyPartialMatches(
                args[targetIndex],
                options,
                new ArrayList<>()
        );
    }
}
