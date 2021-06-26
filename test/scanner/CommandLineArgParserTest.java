package scanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class CommandLineArgParserTest {
    private ArrayList<Path> paths = new ArrayList<>();
    private Set<Path> ignorePaths = new HashSet<>();

    @Test
    void parsePaths() {
        String[] argv = {"/qwerty 1234"};
        CommandLineArgParser parser = new CommandLineArgParser(argv);

        parser.parse();
        paths.add(new File("/qwerty 1234").toPath());
        Assertions.assertEquals(paths, parser.getPaths());
    }

    @Test
    void parseIgnorePaths() {
        String[] argv = {"-", "/qwerty 1234"};
        CommandLineArgParser parser = new CommandLineArgParser(argv);

        parser.parse();
        ignorePaths.add(new File("/qwerty 1234").toPath());
        Assertions.assertEquals(ignorePaths, parser.getIgnorePaths());
    }

    @Test
    void parseException() {
        String[] argv = {"1", "-", "2", "-"};
        CommandLineArgParser parser = new CommandLineArgParser(argv);

        Assertions.assertThrows(IllegalArgumentException.class, () -> parser.parse());
    }
}