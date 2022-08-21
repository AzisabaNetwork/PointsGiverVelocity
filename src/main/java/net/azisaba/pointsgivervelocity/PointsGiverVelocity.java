package net.azisaba.pointsgivervelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.azisaba.pointsgivervelocity.config.PointsGiverVelocityConfig;
import net.azisaba.pointsgivervelocity.sql.SQLConnector;

@Plugin(
    id = "points-giver-velocity",
    name = "PointsGiverVelocity",
    version = "0.0.1-SNAPSHOT",
    url = "https://github.com/AzisabaNetwork/PointsGiverVelocity",
    description = "PointsGiver for Velocity",
    authors = {"Azisaba Network"})
public class PointsGiverVelocity {

  private final ProxyServer proxy;
  private final Logger logger;

  private PointsGiverVelocityConfig pointsGiverVelocityConfig;

  private SQLConnector sqlConnector;

  @Inject
  public PointsGiverVelocity(ProxyServer server, Logger logger) {
    this.proxy = server;
    this.logger = logger;
  }

  @Subscribe
  public void onProxyInitialization(ProxyInitializeEvent event) {
    pointsGiverVelocityConfig = new PointsGiverVelocityConfig(this);
    try {
      pointsGiverVelocityConfig.load();
    } catch (IOException ex) {
      logger.warning("Failed to load config.yml");
      return;
    }

    sqlConnector =
        new SQLConnector(
            pointsGiverVelocityConfig.getHostname(),
            pointsGiverVelocityConfig.getPort(),
            "player_points",
            pointsGiverVelocityConfig.getUsername(),
            pointsGiverVelocityConfig.getPassword());
    sqlConnector.connect();

    proxy
        .getScheduler()
        .buildTask(
            this,
            () -> {
              if (proxy.getPlayerCount() <= 0) {
                return;
              }

              try (Connection connection = sqlConnector.getHikariDataSource().getConnection()) {
                executeSQLCommand(connection);
                logger.info("Successfully gave points to " + proxy.getPlayerCount() + " players.");
              } catch (SQLException e) {
                logger.warning("Failed to give points to " + proxy.getPlayerCount() + " players.");
                e.printStackTrace();
              }
            })
        //        .repeat(20, TimeUnit.MINUTES);
        .repeat(1, TimeUnit.SECONDS)
        .schedule();

    logger.info(getName() + " enabled.");
  }

  @Subscribe
  public void onProxyShutdown(ProxyShutdownEvent event) {
    if (sqlConnector != null) {
      sqlConnector.close();
    }

    logger.info(getName() + " disabled.");
  }

  private void executeSQLCommand(Connection connection) throws SQLException {
    String cmd = "INSERT INTO `playerpoints` (`playername`, `points`) VALUES ";

    cmd +=
        proxy.getAllPlayers().stream()
            .map(
                player ->
                    "(\""
                        + player.getUniqueId().toString()
                        + "\", "
                        + pointsGiverVelocityConfig.getPointAmount()
                        + ")")
            .collect(Collectors.joining(", "));
    cmd += " ON DUPLICATE KEY UPDATE `points` = `points` + VALUES(`points`);";

    if (pointsGiverVelocityConfig.isDebugging()) {
      logger.info("Executing: " + cmd);
    }

    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate(cmd);
    }
  }

  private String getName() {
    return "PointsGiverVelocity";
  }
}
