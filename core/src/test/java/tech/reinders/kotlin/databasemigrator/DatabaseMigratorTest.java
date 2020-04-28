package tech.reinders.kotlin.databasemigrator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.reinders.kotlin.databasemigrator.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseMigratorTest {
    private H2Connector connector;
    private DbMigratorConfiguration config;
    private JdbcUtil util;

    @BeforeEach
    void beforeEach() {
        this.connector = new H2Connector();
        this.config = new DbMigratorConfiguration();
        this.config.setTransactionPerScript(true);

        this.util = JdbcUtil.INSTANCE;
    }

    @AfterEach
    void afterEach() {
        this.connector.close();
    }

    @Test
    void migrate1() {
        this.config.setResourceFolder("/DatabaseMigratorTest_1");

        new DatabaseMigrator(this.connector.getConnection(), this.config).run();
        final List<String> scripts = getRunnedScripts();
        assertEquals(2, scripts.size());
        assertEquals("001_hello.sql", scripts.get(0));
        assertEquals("002_helloagain.sql", scripts.get(1));

        final Connection c = this.connector.getConnection();
        assertTrue(util.tableExists("hello", c));
        assertTrue(util.tableExists("hello2", c));
        assertTrue(util.tableExists("hello3", c));
    }

    @Test
    void migrate1WithUpgrade() {
        migrate1();

        this.config.setResourceFolder("/DatabaseMigratorTest_2");
        new DatabaseMigrator(this.connector.getConnection(), this.config).run();

        final List<String> scripts = getRunnedScripts();
        assertEquals(3, scripts.size());
        assertEquals("001_hello.sql", scripts.get(0));
        assertEquals("002_helloagain.sql", scripts.get(1));
        assertEquals("003_remove_hello.sql", scripts.get(2));

        final Connection c = this.connector.getConnection();
        assertFalse(util.tableExists("hello", c));
        assertTrue(util.tableExists("hello2", c));
        assertTrue(util.tableExists("hello3", c));
    }

    @Test
    void migrateWithError() {
        this.config.setResourceFolder("/DatabaseMigratorTest_3_With_Error");
        this.config.setTransactionPerScript(false);

        try {
            new DatabaseMigrator(this.connector.getConnection(), this.config).run();
            fail("An error should have been thrown");
        } catch (DatabaseMigratorException dme) {
            //This is an expected exception
            assertEquals("ScriptRunner - Table \"HELLOASDFASFAFAF\" not found; SQL statement:\n" +
                    "drop table helloasdfasfafaf; [42102-200]", dme.getMessage());
        } catch (Exception ex) {
            fail("No other exception expected", ex);
        }

        final List<String> scripts = getRunnedScripts();
        assertTrue(scripts.isEmpty());
    }

    @Test
    void migrateWithErrorTransactionPerScript() {
        this.config.setResourceFolder("/DatabaseMigratorTest_3_With_Error");

        try {
            new DatabaseMigrator(this.connector.getConnection(), this.config).run();
            fail("An error should have been thrown");
        } catch (DatabaseMigratorException dme) {
            //This is an expected exception
            assertEquals("ScriptRunner - Table \"HELLOASDFASFAFAF\" not found; SQL statement:\n" +
                    "drop table helloasdfasfafaf; [42102-200]", dme.getMessage());
        } catch (Exception ex) {
            fail("No other exception expected", ex);
        }

        final List<String> scripts = getRunnedScripts();
        assertEquals(2, scripts.size());
        assertEquals("001_script1.sql", scripts.get(0));
        assertEquals("002_script2.sql", scripts.get(1));

        assertTrue(util.tableExists("hello3", this.connector.getConnection()));
    }

    @Test
    void initWithClosedConnection() throws SQLException {
        final Connection c = this.connector.getConnection();
        c.close();

        final DatabaseMigratorException ex = assertThrows(DatabaseMigratorException.class, () -> {
            new DatabaseMigrator(c, this.config).run();
        });

        assertEquals("DatabaseMigrator - The provided connection is already closed", ex.getMessage());
    }

    private List<String> getRunnedScripts() {
        try {
            final PreparedStatement stmt = this.connector.getConnection().prepareStatement("SELECT filename FROM t_dbmig_executed_scripts ORDER BY starttime");
            final List<String> scripts = new ArrayList<>();
            stmt.execute();
            final ResultSet rs = stmt.getResultSet();
            while(rs.next()) {
                scripts.add(rs.getString("filename"));
            }

            return scripts;
        } catch (Exception ex) {
            fail(ex);
            return Collections.emptyList();
        }
    }
}
