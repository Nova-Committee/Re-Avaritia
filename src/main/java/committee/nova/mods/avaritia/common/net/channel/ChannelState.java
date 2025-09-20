package committee.nova.mods.avaritia.common.net.channel;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 12:44
 * @Description:
 */
public enum ChannelState {
    COMMON(0, "common"),
    FULL(1, "full"),
    NAME(2, "name");
    private final String name;
    private final int id;

    ChannelState(int id, String name) {
        this.name = name;
        this.id = id;
    }
}
