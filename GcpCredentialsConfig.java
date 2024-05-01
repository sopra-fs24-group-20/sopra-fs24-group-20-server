import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Configuration
public class GcpCredentialsConfig {

    @Autowired
    private Environment env;

    @PostConstruct
    public void init() throws Exception {
        // Decode and write credentials to a temp file
        String encodedCredentials = env.getProperty("google.cloud.credentials.json");
        byte[] decodedBytes = Base64.getDecoder().decode(encodedCredentials);
        Path tempPath = Files.createTempFile("gcp", ".json");
        Files.write(tempPath, decodedBytes);

        // Set the GOOGLE_APPLICATION_CREDENTIALS environment variable
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", tempPath.toString());
        tempPath.toFile().deleteOnExit(); // Ensure the file is deleted when the JVM exits
    }
}
