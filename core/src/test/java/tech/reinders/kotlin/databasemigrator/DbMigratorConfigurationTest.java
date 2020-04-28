package tech.reinders.kotlin.databasemigrator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Only to test defaults
 */
public class DbMigratorConfigurationTest {
    private DbMigratorConfiguration configuration;

    @BeforeEach
    void beforeEach() {
        this.configuration = new DbMigratorConfiguration();
    }

    @Test
    void testDefaults() {
        assertEquals("/dbmigration", this.configuration.getResourceFolder());
        assertFalse(this.configuration.getEnableSqlLogging());
        assertTrue(this.configuration.getTransactionPerScript());
    }
}