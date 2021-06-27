package scanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.List;

/**
 * Class to scan List of directories
 */
public class FileScanner {
    final private List<Path> paths;
    final private Set<Path> ignorePaths;
    private Set<String> results;

    /**
     * Construct Scanner
     * @param paths List of Paths to scan
     * @param ignorePaths Set of Paths to ignore
     */
    public FileScanner(List<Path> paths, Set<Path> ignorePaths) {
        this.paths = paths;
        this.ignorePaths = ignorePaths;
    }

    /**
     * Method to start directories scan in numOfThreads Threads
     * @param numOfThreads
     * @return String with all results of scan
     */
    public String scan(int numOfThreads) {
        results = new ConcurrentSkipListSet<>();//создаем при начале сканирования,
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
            return toString();
    }

    /**
     * Create LinkedBlockingQueue of ThreadFileScan for each Path in paths
     * @return created LinkedBlockingQueue
     */
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

    /**
     * Method that create file "dirScan FULL-DATE-TIME.txt" and save results of scan to it
     * Each file is located at path "./scan results/directories HASH-OF-ARGS-TO-SCAN scan results"
     * @return Path of created file
     */
    public Path saveToFile() {
        String result = toString();
        if(result.isEmpty()){
            return null;
        }
        try {
            Path rootDir = new File("./scan results/").toPath();
            //создаем разные директории для разных входных данных так как по условию предполагается
            // сравнение результатов сканирования с одними и теми же входными данными
            String parentDir = "./scan results/directories "
                    + Math.abs(paths.hashCode() ^ ignorePaths.hashCode())
                    + " scan results/";
            Path dir = new File(parentDir).toPath();
            try {
                Files.createDirectory(rootDir);
            } catch (Exception e){
            }//значит директория уже существует
            try {
                Files.createDirectory(dir);
            } catch (Exception e){
            }
            //создаем новый файл для каждого сканирования
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
            String newFileName = "dirScan " + dateFormat.format(System.currentTimeMillis()) + ".txt";
            File newFile = new File(parentDir, newFileName);
            Files.write(newFile.toPath(), List.of(result), StandardOpenOption.CREATE);

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
