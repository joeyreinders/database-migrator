package tech.reinders.kotlin.databasemigrator.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SqlUtilTest {
    private SqlUtil util;

    @BeforeEach
    void beforeEach() {
        this.util = SqlUtil.INSTANCE;
    }

    @Test
    void lineIsComment() {
        assertFalse(util.lineIsComment(""));
        assertFalse(util.lineIsComment("    "));
        assertFalse(util.lineIsComment("this is no comment"));

        assertTrue(util.lineIsComment("--this is a comment"));
        assertTrue(util.lineIsComment("  --this is a comment"));

        assertTrue(util.lineIsComment("//this is a comment"));
        assertTrue(util.lineIsComment("  //this is a comment"));

        assertTrue(util.lineIsComment("/* this is a comment */"));
    }
}