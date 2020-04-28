package tech.reinders.kotlin.databasemigrator.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.reinders.kotlin.databasemigrator.H2Connector;
import tech.reinders.kotlin.databasemigrator.util.JdbcUtil;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExecutedScriptServiceTest {
    private H2Connector connector;
    private ExecutedScriptService service;

    @BeforeEach
    void beforeEach() {
        this.connector = new H2Connector();
        this.service = new DefaultExecutedScriptService(this.connector.getConnection());
    }

    @AfterEach
    void afterEach() {
        this.connector.close();
    }

    @Test
    void createTable() {
        assertFalse(JdbcUtil.INSTANCE.tableExists("t_dbmig_executed_scripts", this.connector.getConnection()));
        this.service.createTable();
        assertTrue(JdbcUtil.INSTANCE.tableExists("t_dbmig_executed_scripts", this.connector.getConnection()));
    }

    @Test
    void hasRun() {
        this.service.createTable();
        assertFalse(this.service.hasRun("randomscript.sql"));
    }

    @Test
    void insert() {
        this.service.createTable();
        assertFalse(this.service.hasRun("randomscript.sql"));

        this.service.insert("randomscript.sql", new Date(), new Date());
        assertTrue(this.service.hasRun("randomscript.sql"));
    }
}
