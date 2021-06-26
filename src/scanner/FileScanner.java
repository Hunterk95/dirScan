package scanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.List;

/**
 *
 */
public class FileScanner {
    final private List<Path> paths;
    final private Set<Path> ignorePaths;
    private List<String> results;

    public FileScanner(List<Path> paths, Set<Path> ignorePaths) {
        this.paths = paths;
        this.ignorePaths = ignorePaths;
    }

    public void scan(int numOfThreads) {
        results = new ArrayList<>();//создаем при начале сканирования,
        //если нам нужно отсканировать несколько раз, старые результаты нам не нужны
        BlockingQueue<Runnable> queue = createQueue();

        //таймаут с потолка, если обращаемся к файлу дольше значит что-то идет не так
        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(numOfThreads, numOfThreads, 100, TimeUnit.MILLISECONDS, queue);
        executor.prestartAllCoreThreads();
        //непосредственно сканирование
        while (executor.getTaskCount() != executor.getCompletedTaskCount()) {
        }
        executor.shutdown();
        try {
            //дожидаемся окончания всех потоков
            executor.awaitTermination(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.out.println("WTF");
            e.printStackTrace();
        }
        //сортируем для получения одного и того же порядка от сканирования к сканированию как указано в задаче
        Collections.sort(results);
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

    public Path saveToFile() {
        try {
            Path rootDir = new File("./scan results/").toPath();
            if (!Files.exists(rootDir)) {
                Files.createDirectory(rootDir);
            }
            //создаем разные директории для разных входных данных так как по условию предполагается
            // сравнение результатов сканирования с одними и теми же входными данными
            String parentDir = "./scan results/directories "
                    + Math.abs(paths.hashCode() ^ ignorePaths.hashCode())
                    + " scan results/";
            Path dir = new File(parentDir).toPath();
            if (!Files.exists(dir)) {
                Files.createDirectory(dir);
            }
            //создаем новый файл для каждого сканирования
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
            String newFileName = "dirScan " + dateFormat.format(System.currentTimeMillis()) + ".txt";
            File newFile = new File(parentDir, newFileName);
            Files.write(newFile.toPath(), List.of(toString()), StandardOpenOption.CREATE);

            return newFile.toPath();

        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
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
