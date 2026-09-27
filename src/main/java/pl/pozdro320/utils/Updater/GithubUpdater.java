package pl.pozdro320.utils.Updater;

import lombok.AllArgsConstructor;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

@AllArgsConstructor
public class GithubUpdater {

    private final String versionUrl;

    public boolean hasUpdate(SemanticVersion currentVersion) {
        try {
            URL url = new URL(this.versionUrl);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String latestVersionString = reader.readLine();
                if (latestVersionString == null) return false;

                SemanticVersion latest = new SemanticVersion(latestVersionString.trim());
                return latest.isNewerThan(currentVersion);
            }
        } catch (Exception e) {
            return false;
        }
    }
}