package scanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Class of one thread of scan
 */
public class ThreadFileScan implements Runnable {

    final private Path path;
    final private Set<Path> ignorePaths;
    final private Set<String> results;
    final private Queue<Runnable> queue;

    /**
     * Construct thread of scan
     *
     * @param path        Path of directory to scan
     * @param ignorePaths Set of ignored directories
     * @param queue       Queue of scan threads to add to it new threads of scan of subdirectories
     * @param results     List of results to save result on it
     */
    public ThreadFileScan(Path path, Set<Path> ignorePaths, Queue<Runnable> queue, Set<String> results) {
        this.path = path;
        this.ignorePaths = ignorePaths;
        this.queue = queue;
        this.results = results;
    }

    @Override
    public void run() {
        oneDirScan(path);
    }

    /**
     * @param curRootPath
     */
    private void oneDirScan(Path curRootPath) {
        //создаем фильтр, если через лямбды короче без ухудшения читаемости, то почему нет
        DirectoryStream.Filter<Path> filter = (Path currentPath) -> !ignorePaths.contains(currentPath);
        //автозакрываемый поток всего что есть в директории с фильтрацией - идеально соответствует задаче
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(curRootPath, filter)) {
            for (Path curPath : stream) {
                //если это директория - ставим в очередь на сканирование ее содержимого
                if (Files.isDirectory(curPath)) {
                    if (!queue.add(new ThreadFileScan(curPath, ignorePaths, queue, results))) {
                        throw new IllegalStateException("Cant add path to scan queue");
                    }
                }
                //если это не директория - закидываем в лист результатов
                // урезаем информацию до необходимой нам
                // так сильно меньше памяти необходимо при большом количестве объектов
                else {
                    results.add(fileInfo(curPath));
                }
                //System.out.println(curPath);
            }
        } catch (IOException e) {
            System.out.println("no such directory: " + e.getMessage());
        }
    }

    /**
     * Format file name, date of last modify and size to String
     *
     * @param path of file
     * @return formatted String
     */
    private String fileInfo(Path path) {
        File file = path.toFile();
        StringBuilder result = new StringBuilder();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
        result.append("[\n" + "file = ").append(file.getAbsolutePath())
                .append("\n" + "date = ").append(formater.format(file.lastModified()))
                .append("\n" + "size = ").append(file.length())
                .append("]");
        return result.toString();
    }
}
