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

public class ThreadFileScan implements Runnable {

    final private Path path;
    final private Set<Path> ignorePaths;
    final private List<String> results;
    final private Queue<Runnable> queue;

    public ThreadFileScan(Path path, Set<Path> ignorePaths, Queue<Runnable> queue, List<String> results) {
        this.path = path;
        this.ignorePaths = ignorePaths;
        this.queue = queue;
        this.results = results;
    }

    @Override
    public void run() {
        connect(path);
    }

    public void connect(Path curRootPath) {
        //создаем фильтр, если через лямбды короче без ухудшения читаемости, то почему нет
        DirectoryStream.Filter<Path> filter = (Path currentPath) -> !ignorePaths.contains(currentPath);
        //автозакрываемый поток всего что есть в дериктории с фильтрацией - идеально соответствует задаче
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(curRootPath, filter)) {
            for (Path curPath : stream) {
                //если это директория - ставим в очередь на сканирование ее содержимого
                if (Files.isDirectory(curPath)) {
                    queue.add(new ThreadFileScan(curPath, ignorePaths, queue, results));
                }
                //если это не директория - закидываем в лист результатов
                // урезаем информацию до необходимой нам
                // так сильно меньше памяти необходимо при большом количестве объектов
                else synchronized (results){
                    results.add(fileInfo(curPath));
                }
                //System.out.println(curPath);
            }
        } catch (IOException e) {
            System.out.println("no such directory: " + e.getMessage());
        }
    }

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
