package net.azisaba.pointsgivervelocity.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SQLConnector {

  private final String hostname;
  private final int port;
  private final String database;
  private final String username;
  private final String password;

  private HikariDataSource hikariDataSource;

  public boolean isConnected() {
    return hikariDataSource != null && !hikariDataSource.isClosed();
  }

  public void connect() {
    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException ignore) {
      // pass
    }

    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database);
    config.setUsername(username);
    config.setPassword(password);

    hikariDataSource = new HikariDataSource(config);
  }

  public void close() {
    if (isConnected()) {
      hikariDataSource.close();
    }
  }

  @Nonnull
  public HikariDataSource getHikariDataSource() {
    if (hikariDataSource == null) {
      throw new IllegalStateException("SQL connection not established.");
    }
    return hikariDataSource;
  }
}
