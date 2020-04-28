package tech.reinders.kotlin.databasemigrator.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.reinders.kotlin.databasemigrator.DatabaseMigratorException;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class FileUtilTest {
    private FileUtil util;

    @BeforeEach
    void beforeEach() {
        this.util = FileUtil.INSTANCE;
    }

    @Test
    void getResourceDir() throws URISyntaxException {
        RuntimeException ex = assertThrows(IllegalArgumentException.class,
                () -> util.getResourceDir(null));
        assertEquals("Parameter specified as non-null is null: method tech.reinders.kotlin.databasemigrator.util.FileUtil.getResourceDir, parameter aLocation", ex.getMessage());

        ex = assertThrows(DatabaseMigratorException.class,
                () -> util.getResourceDir(""));

        assertEquals("FileUtil - No resource location is defined, this is a required parameter", ex.getMessage());

        final File file = new File(FileUtilTest.class.getResource("/FileUtilTest").toURI());
        assertEquals(file.toURI(), util.getResourceDir("/FileUtilTest").toURI());
    }

    @Test
    void getFiles() {
        final File[] files = Arrays.stream(this.util.getFiles("/FileUtilTest")).map(FileWrapper::getFile).toArray(File[]::new);
        final String[] actual = Arrays.stream(files).map(File::getName).toArray(String[]::new);

        final String[] expected = {
                "00_Script.sql",
                "script1.sql",
                "script2.sql"
        };

        assertArrayEquals(expected, actual);
    }
}
