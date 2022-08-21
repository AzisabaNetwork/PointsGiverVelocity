package net.azisaba.pointsgivervelocity.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.azisaba.pointsgivervelocity.PointsGiverVelocity;
import org.yaml.snakeyaml.Yaml;

@Getter
@RequiredArgsConstructor
public class PointsGiverVelocityConfig {

  private final PointsGiverVelocity plugin;

  private static final String CONFIG_FILE_PATH = "./plugins/PointsGiverVelocity/config.yml";

  private String hostname;
  private int port;
  private String username;
  private String password;

  private int pointAmount;

  private boolean debugging;

  public void load() throws IOException {
    File file = new File(CONFIG_FILE_PATH);
    // save if not exist
    saveDefaultConfig(file);

    Yaml yaml = new Yaml();
    Map<String, Object> data = yaml.load(new FileReader(file));

    Map<String, Object> mysql = dig(data, "mysql");
    hostname = (String) mysql.get("hostname");
    port = (Integer) mysql.get("port");
    username = (String) mysql.get("username");
    password = (String) mysql.get("password");

    pointAmount = (Integer) data.get("point-amount");

    debugging = (Boolean) data.get("debug");
  }

  private void saveDefaultConfig(File configFilePath) throws IOException {
    if (configFilePath.exists()) {
      return;
    }

    InputStream is = plugin.getClass().getClassLoader().getResourceAsStream("config.yml");
    if (is == null) {
      throw new IllegalStateException("Failed to load config.yml from resource.");
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    Files.createDirectories(configFilePath.getParentFile().toPath());

    byte[] byteStr =
        reader
            .lines()
            .collect(Collectors.joining(System.lineSeparator()))
            .getBytes(StandardCharsets.UTF_8);

    Files.write(configFilePath.toPath(), byteStr);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> dig(Map<String, Object> data, String key) throws IOException {
    Object o = data.get(key);
    if (!(o instanceof Map)) {
      throw new IOException("Failed to get new map from key: " + key);
    }

    return (Map<String, Object>) o;
  }
}
