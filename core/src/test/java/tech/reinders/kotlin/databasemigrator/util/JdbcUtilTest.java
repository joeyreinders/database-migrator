package tech.reinders.kotlin.databasemigrator.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.reinders.kotlin.databasemigrator.H2Connector;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class JdbcUtilTest {
    private H2Connector connector;
    private JdbcUtil util;

    @BeforeEach
    void beforeEach() {
        this.util = JdbcUtil.INSTANCE;
        this.connector = new H2Connector();
    }

    @AfterEach
    void afterEach() {
        this.connector.close();
    }

    @Test
    void close() throws Exception {
        final AutoCloseable c = mock(AutoCloseable.class);
        this.util.close(c);
        verify(c, times(1)).close();
        verifyNoMoreInteractions(c);
    }

    @Test
    void closeWithError() throws Exception {
        final AutoCloseable c = mock(AutoCloseable.class);
        doThrow(new RuntimeException()).when(c).close();

        this.util.close(c);

        verify(c, times(1)).close();
        verifyNoMoreInteractions(c);
    }

    @Test
    void tableExists() {
        assertFalse(util.tableExists("t_test_table", connector.getConnection()));
    }

    @Test
    void createTable() {
        final Connection c = connector.getConnection();
        assertFalse(util.tableExists("t_test_table", c));

        util.createTable("create table t_test_table(name VARCHAR(10))", c);
        assertTrue(util.tableExists("t_test_table", c));
    }
}
