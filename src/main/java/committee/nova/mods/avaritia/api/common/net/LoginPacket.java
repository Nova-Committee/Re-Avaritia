package committee.nova.mods.avaritia.api.common.net;

import java.util.function.IntSupplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 13:03
 * Version: 1.0
 */
public abstract class LoginPacket<T extends IPacket<T>> extends IPacket<T> implements IntSupplier {
    private int loginIndex;

    public LoginPacket() {
    }

    public int getAsInt() {
        return this.loginIndex;
    }

    public int getLoginIndex() {
        return this.loginIndex;
    }

    public void setLoginIndex(int loginIndex) {
        this.loginIndex = loginIndex;
    }
}
