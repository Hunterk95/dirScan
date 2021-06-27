package scanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

class FileScannerTest {
    private List<Path> paths;
    private Set<Path> ignorePaths;
    private Set<String> results;

    @Test //порядок от сканирования к сканированию должен оставаться постоянным,
    //также проверяем что от сканирования к сканированию не теряем файлы
    void isOrderConst() {
        paths = new ArrayList<>();
        paths.add(new File("/usr").toPath());
        ignorePaths = new HashSet<>();
        ignorePaths.add(new File("/usr/share").toPath());
        ignorePaths.add(new File("/usr/libexec").toPath());
        ignorePaths.add(new File("/usr/bin").toPath());
        ignorePaths.add(new File("/usr/include").toPath());
        FileScanner scanner = new FileScanner(paths, ignorePaths);
        scanner.scan(1);
        String scan1 = scanner.toString();
        scanner.scan(4);
        String scan2 = scanner.toString();
        Assertions.assertEquals(scan1, scan2);
    }

    @Test //при добавлении путей с исключением всех добавленных не должен ничего сканировать
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

    @Test
    public void scan() throws IOException, InterruptedException {
        results = new ConcurrentSkipListSet<>();
        paths = new ArrayList<>();
        paths.add(new File("./test/testdir/").toPath());
        ignorePaths = new HashSet<>();
        BlockingQueue<Runnable> queue = createQueue();
        Files.createDirectory(Path.of("./test/testdir"));
        Files.createDirectory(Path.of("./test/testdir/0"));
        Files.createDirectory(Path.of("./test/testdir/0/0"));
        Files.createDirectory(Path.of("./test/testdir/0/0/0"));
        Files.createDirectory(Path.of("./test/testdir/1"));
        Files.createFile(Path.of("./test/testdir/1/1"));
        Files.createDirectory(Path.of("./test/testdir/2"));
        Files.createFile(Path.of("./test/testdir/2/2"));
        Files.createDirectory(Path.of("./test/testdir/3"));
        Files.createFile(Path.of("./test/testdir/3/3"));
        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(1, 1, 100, TimeUnit.MILLISECONDS, queue);
        Files.delete(Path.of("./test/testdir/1/1"));//delete file before scan it
        executor.prestartAllCoreThreads();
        while(results.isEmpty()){
            Thread.sleep(10);
        }
        Files.createFile(Path.of("./test/testdir/1/1"));//restore file after directory scan
        while (executor.getTaskCount() != executor.getCompletedTaskCount()) {
        }
        executor.shutdown();
        try {
            executor.awaitTermination(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.out.println("WTF");
            e.printStackTrace();
        }finally {
            Files.delete(Path.of("./test/testdir/0/0/0"));
            Files.delete(Path.of("./test/testdir/0/0"));
            Files.delete(Path.of("./test/testdir/0"));
            Files.delete(Path.of("./test/testdir/1/1"));
            Files.delete(Path.of("./test/testdir/2/2"));
            Files.delete(Path.of("./test/testdir/3/3"));
            Files.delete(Path.of("./test/testdir/1"));
            Files.delete(Path.of("./test/testdir/2"));
            Files.delete(Path.of("./test/testdir/3"));
            Files.delete(Path.of("./test/testdir"));
        }

        String actual = toString();
        //check that if file has been deleted (lost connect to network directory) and restore after scan him path
        // that it not been scanned one more time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Assertions.assertEquals("[\n" +
                "file = /home/hunt/IdeaProjects/dirScan/./test/testdir/2/2\n" +
                "date = " + dateFormat.format(System.currentTimeMillis()) + "\n" +
                "size = 0][\n" +
                "file = /home/hunt/IdeaProjects/dirScan/./test/testdir/3/3\n" +
                "date = " + dateFormat.format(System.currentTimeMillis()) + "\n" +
                "size = 0]", actual);
    }

    private BlockingQueue<Runnable> createQueue() {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        try {
            for (Path path : paths) {
                queue.add(new ThreadFileScan(path, ignorePaths, queue, results));
            }
        } catch (Exception e) {
            System.out.println("WTF");
            e.printStackTrace();
        }
        return queue;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (String file : results) {
            result.append(file);
        }
        return result.toString();
    }
}