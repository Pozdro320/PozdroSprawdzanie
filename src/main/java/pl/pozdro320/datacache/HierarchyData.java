package pl.pozdro320.datacache;

import java.util.Map;

public class HierarchyData {
    private final Map<String, Integer> groupPriorities;
    private final boolean enabled;

    public HierarchyData(Map<String, Integer> groupPriorities, boolean enabled) {
        this.groupPriorities = groupPriorities;
        this.enabled = enabled;
    }

    public boolean canCheck(String moderatorGroup, String targetGroup) {
        if (!enabled) return true;

        int modPower = groupPriorities.getOrDefault(moderatorGroup.toLowerCase(), 0);
        int targetPower = groupPriorities.getOrDefault(targetGroup.toLowerCase(), 0);

        return modPower > targetPower;
    }
}