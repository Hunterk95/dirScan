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
    private List<Path> paths = new ArrayList<>();
    private Set<Path> ignorePaths = new HashSet<>();

    @Test
    void isOrderConst() {
        paths.add(new File("/usr").toPath());
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
}