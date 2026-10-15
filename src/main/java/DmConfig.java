import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public final class DmConfig {
    private DmConfig() { }

    public static Optional<String> loadPassword(Path file) {
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) {
            props.load(reader);
        } catch (IOException e) {
            return Optional.empty();
        }
        String password = props.getProperty("dm.password", "").trim();
        return password.isEmpty() ? Optional.empty() : Optional.of(password);
    }
}
