import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @author: cnlimiter
 */
public class BootstrapMinecraftExtension implements Extension, BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }
}
