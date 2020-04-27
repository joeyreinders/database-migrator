package tech.reinders.kotlin.databasemigrator.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void startLineComment() {
        assertTrue(util.startLineComment("/*"));
        assertTrue(util.startLineComment("     /* this is a comment start"));

        assertFalse(util.startLineComment("no comment"));
        assertFalse(util.startLineComment("no comment /* this is comment but no */"));
    }

    @Test
    void endLineComment() {
        assertTrue(util.endLineComment("*/"));
        assertTrue(util.endLineComment("   asfsafa  */       "));

        assertFalse(util.endLineComment("   asfsafa  */      ddafsdfa "));
    }

    @Test
    void readScriptFile() throws URISyntaxException {
        final File f = new File(SqlUtilTest.class.getResource("/SqlUtilTest/script_with_comments.sql").toURI());
        final String sql = this.util.readScriptFile(f);
        assertEquals("CREATE TABLE TEST (ID INT NOT NULL);CREATE TABLE TEST2 (ID INT NOT NULL);", sql);
    }
}