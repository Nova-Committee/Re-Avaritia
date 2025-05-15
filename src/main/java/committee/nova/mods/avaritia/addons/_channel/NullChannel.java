package committee.nova.mods.avaritia.addons._channel;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 12:38
 * @Description:
 */
public class NullChannel extends ServerChannel {

    public static final NullChannel INSTANCE = new NullChannel();

    private NullChannel() {
        super();
        super.setName("RemovedChannel");
    }

    @Override
    public boolean isRemoved() {
        return true;
    }
}
