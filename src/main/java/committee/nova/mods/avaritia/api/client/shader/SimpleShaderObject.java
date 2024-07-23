package committee.nova.mods.avaritia.api.client.shader;

import java.util.Collection;

/**
 * Created by covers1624 on 24/5/20.
 */
public final class SimpleShaderObject extends AbstractShaderObject {
    private final String source;

    SimpleShaderObject(String name, ShaderType type, Collection<UniformPair> uniforms, String source) {
        super(name, type, uniforms);
        this.source = source;
    }

    @Override
    protected String getSource() {
        return source;
    }
}
