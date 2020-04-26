package tech.reinders.kotlin.databasemigrator;

import tech.reinders.kotlin.databasemigrator.util.JdbcUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public final class H2Connector {
    private static final String FILE = "/tmp/test.tmp";
    private static final String URL = "jdbc:h2:" + FILE;
    private static final String USR = "sa";
    private static final String PWD = "";

    private final List<Connection> connections = new ArrayList<>();

    public Connection getConnection() {
        try {
            final Connection connection = DriverManager.getConnection(URL, USR, PWD);
            this.connections.add(connection);
            return connection;
        } catch (SQLException e) {
            fail(e);
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            getConnection().createStatement().execute("DROP ALL OBJECTS");
        } catch (Exception ex) {
            throw new RuntimeException("Error while dropping all objects", ex);
        }

        connections.forEach(JdbcUtil.INSTANCE::close);

        new File(FILE).delete();
    }
}
