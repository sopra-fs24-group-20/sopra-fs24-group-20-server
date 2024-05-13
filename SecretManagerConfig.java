import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecretManagerConfig {

    @Value("${secret.manager.projectId}")
    private String projectId;

    @Value("${secret.manager.secretId}")
    private String secretId;

    public String getSecret() {
        String secretVersion = "latest"; // Can specify other versions if needed
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, secretVersion);
            AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
            return response.getPayload().getData().toStringUtf8();
        } catch (Exception e) {
            throw new RuntimeException("Unable to access secret from Secret Manager", e);
        }
    }
}
