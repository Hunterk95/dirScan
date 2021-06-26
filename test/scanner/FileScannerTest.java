package scanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class FileScannerTest {
    private List<Path> paths;
    private Set<Path> ignorePaths;

    @Test
    void isOrderConst() {
        paths = new ArrayList<>();
        paths.add(new File("/usr").toPath());
        ignorePaths = new HashSet<>();
        ignorePaths.add(new File("/usr/share").toPath());
        ignorePaths.add(new File("/usr/lib").toPath());
        ignorePaths.add(new File("/usr/bin").toPath());
        ignorePaths.add(new File("/usr/include").toPath());
        FileScanner scanner = new FileScanner(paths, ignorePaths);
        scanner.scan(4);
        String scan1 = scanner.toString();
        scanner.scan(4);
        String scan2 = scanner.toString();
        Assertions.assertEquals(scan1, scan2);
    }

    @Test
    void noNeedToScan() {
        String[] args = {"\\\\epbyminsd0235\\Video Materials",
                "\\\\EPUALVISA0002.kyiv.com\\Workflow\\ORG\\Employees\\Special",
                "\\\\EPUALVISA0002.kyiv.com\\Workflow\\ORG\\Employees\\Lviv",
                "-",
                "\\\\epbyminsd0235\\Video Materials",
                "\\\\EPUALVISA0002.kyiv.com\\Workflow\\ORG\\Employees\\Special",
                "\\\\EPUALVISA0002.kyiv.com\\Workflow\\ORG\\Employees\\Lviv"};
        CommandLineArgParser parser = new CommandLineArgParser(args);
        parser.parse();
        FileScanner scanner = new FileScanner(parser.getPaths(), parser.getIgnorePaths());
        scanner.scan(parser.getNumOfThreads());
        Assertions.assertEquals(scanner.toString(), "");
    }
}