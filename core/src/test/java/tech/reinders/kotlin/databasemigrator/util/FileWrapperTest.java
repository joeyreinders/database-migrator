package tech.reinders.kotlin.databasemigrator.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class FileWrapperTest {
    private FileWrapper dir;
    private FileWrapper file;

    @BeforeEach
    void beforeEach() throws URISyntaxException {
        final File f = new File(FileWrapperTest.class.getResource("/FileUtilTest").toURI());
        this.dir = new FileWrapper(f, f);
        this.file = new FileWrapper(f, new File(FileWrapperTest.class.getResource("/FileUtilTest/00_Script.sql").toURI()));
    }

    @Test
    void isDirectory() {
        assertTrue(this.dir.isDirectory());
        assertFalse(this.file.isDirectory());
    }

    @Test
    void name() {
        assertEquals("FileUtilTest", this.dir.getName());
        assertEquals("00_Script.sql", this.file.getName());
    }

    @Test
    void getFile() throws URISyntaxException {
        assertEquals(new File(FileWrapperTest.class.getResource("/FileUtilTest").toURI()), this.dir.getFile());
        assertEquals(new File(FileWrapperTest.class.getResource("/FileUtilTest/00_Script.sql").toURI()), this.file.getFile());
    }

    @Test
    void isSql() throws URISyntaxException {
        assertTrue(this.file.isSqlFile());
        assertFalse(this.dir.isSqlFile());
        assertFalse(new FileWrapper(dir.getFile(), new File(FileWrapperTest.class.getResource("/FileUtilTest/notsql.txt").toURI())).isSqlFile());
    }

    @Test
    void relativeName() {
        assertEquals("FileUtilTest", this.dir.getRelativeName());
        assertEquals("00_Script.sql", this.file.getRelativeName());
    }
}