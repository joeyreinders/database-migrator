package tech.reinders.kotlin.databasemigrator.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.reinders.kotlin.databasemigrator.H2Connector;
import tech.reinders.kotlin.databasemigrator.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

public class SemaphoreServiceTest {
    private static final String TABLE = "t_dbmig_semaphore";

    private H2Connector connector;
    private Connection connection;
    private SemaphoreService service;

    @BeforeEach
    void beforeEach() {
        this.connector = new H2Connector();
        this.connection = this.connector.getConnection();
        this.service = new DefaultSemaphoreService(this.connection);
    }

    @AfterEach
    void afterEach() {
        this.connector.close();
    }

    @Test
    void createTable() {
        assertFalse(JdbcUtil.INSTANCE.tableExists(TABLE, this.connection));
        this.service.createTable();
        assertTrue(JdbcUtil.INSTANCE.tableExists(TABLE, this.connection));
    }

    @Test
    void locked() {
        this.service.createTable();

        assertFalse(this.service.locked());
        lockTable();
        assertTrue(this.service.locked());
    }

    @Test
    void lock() {
        this.service.createTable();
        assertFalse(this.service.locked());

        assertTrue(this.service.lock());
    }

    @Test
    void lockNotPossible() {
        this.service.createTable();
        lockTable();
        assertTrue(this.service.locked());

        assertFalse(this.service.lock());
    }

    @Test
    void releaseOwnLock() {
        this.service.createTable();
        assertFalse(this.service.locked());
        assertTrue(this.service.lock());
        assertTrue(this.service.locked());

        this.service.release();
        assertFalse(this.service.locked());
    }

    @Test
    void releaseOtherLock() {
        this.service.createTable();
        lockTable();
        assertTrue(this.service.locked());
        this.service.release();
        assertTrue(this.service.locked());
    }

    @Test
    void forceRelease() {
        this.service.createTable();
        lockTable();
        assertTrue(this.service.locked());
        this.service.forceRelease();
        assertFalse(this.service.locked());
    }

    private void lockTable() {
        try {
            final Connection c = this.connector.getConnection();
            final PreparedStatement stmt = c.prepareStatement(DefaultSemaphoreService.QRY_INSERT);
            stmt.setInt(1, 1);
            stmt.setString(2, "testHost");
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setString(4, "pid-123456");
            stmt.execute();
            c.commit();
        } catch (Exception ex) {
            fail(ex);
        }
    }
}
