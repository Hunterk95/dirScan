package scanner;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class for parse command line arguments
 */
public class CommandLineArgParser {

    final private String[] argv;
    final private List<Path> paths = new ArrayList<>(); //просто лист путей, поиск и изменение не требуется
    final private Set<Path> ignorePaths = new HashSet<>(); // часто ищем вхождения, hashset даст О(1)
    //количество потоков может парситься из аргументов если нужно
    private int numOfThreads = 4;

    //никакой логики в конструкторе, просто копируем входные аргументы
    public CommandLineArgParser(String[] argv) {
        this.argv = argv;
    }

    public int getNumOfThreads() {
        return numOfThreads;
    }

    public List<Path> getPaths() {
        return paths;
    }

    public Set<Path> getIgnorePaths() {
        return ignorePaths;
    }

    public void parse() throws IllegalArgumentException {
        //это не просто ключ после которого одно значение,
        // а полностью меняющий смысл всего идущего за ним, так что так
        boolean beforeIgnore = true;
        for (String arg : argv) {
            if (arg.equals("-")) {
                if (beforeIgnore) {
                    beforeIgnore = false;
                    continue;
                } else {
                    throw new IllegalArgumentException("must be only one -");
                }
            }
            //ну и раскидываем сначала в пути потом в игнор
            if (beforeIgnore) {
                paths.add(new File(arg).toPath());
            } else {
                ignorePaths.add(new File(arg).toPath());
            }
        }
    }
}

