package scanner;

import java.util.Optional;

public class main {

    public static void main(String[] args) {
        CommandLineArgParser parser = new CommandLineArgParser(args);
        parser.parse();
        FileScanner scanner = new FileScanner(parser.getPaths(), parser.getIgnorePaths());
        scanner.scan(parser.getNumOfThreads());
        System.out.println("Scanned " + scanner.getNumOfScannedFiles() + " files");
        System.out.println("Save result of scan to file " + Optional.ofNullable(scanner.saveToFile()).orElseThrow());
    }
}
