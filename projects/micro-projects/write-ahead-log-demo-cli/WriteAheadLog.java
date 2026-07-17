import java.io.*;
import java.nio.file.*;
import java.util.*;

public class WriteAheadLog {
    private final Path logFile;

    public WriteAheadLog(Path logFile) throws IOException {
        this.logFile = logFile;
        if (!Files.exists(logFile)) Files.createFile(logFile);
    }

    public void append(String operation) throws IOException {
        Files.writeString(logFile, operation + "\n", StandardOpenOption.APPEND);
    }

    public List<String> replay() throws IOException {
        return Files.readAllLines(logFile);
    }

    public static void main(String[] args) throws IOException {
        Path path = Files.createTempFile("wal-demo", ".log");
        WriteAheadLog wal = new WriteAheadLog(path);
        wal.append("SET balance=100");
        wal.append("SET balance=150");
        wal.append("SET balance=120");

        System.out.println("Journal (source de verite en cas de crash) :");
        wal.replay().forEach(System.out::println);
        Files.delete(path);
    }
}
