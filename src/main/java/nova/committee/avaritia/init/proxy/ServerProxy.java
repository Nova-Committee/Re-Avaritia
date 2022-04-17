package nova.committee.avaritia.init.proxy;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 20:12
 * Version: 1.0
 */
public class ServerProxy implements IProxy {
    public static final GameProfile avaritiaFakePlayer = new GameProfile(UUID.fromString("32283731-bbef-487c-bb69-c7e32f84ed27"), "[Avaritia]");

    @Override
    public void init() {

    }
}
