package committee.nova.mods.avaritia.common.net.channel;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 12:44
 * @Description:
 */
public enum ChannelAction {
    ADD(0, "add"),
    REMOVE(1, "remove"),
    SET(2, "set");
    private final String name;
    private final int id;

    ChannelAction(int id, String name) {
        this.name = name;
        this.id = id;
    }
}
