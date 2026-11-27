package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.NameAndId;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.UUID;

/**
 * Dedicated-server GameTest players that never call {@code PlayerList.placeNewPlayer}.
 * Login-based mocks try to send play payloads such as {@code avaritia:s2c_singularities}
 * to an unnegotiated embedded client.
 */
public final class GameTestPlayers {
    private GameTestPlayers() {
    }

    /**
     * No-login {@link FakePlayer} for item use, menus, and inventories.
     * Does not occupy {@link ServerLevel#players()}.
     */
    public static FakePlayer create(GameTestHelper helper, String name) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
    }

    /**
     * No-login {@link FakePlayer} that occupies the current {@link ServerLevel#players()}
     * list and is registered in the server {@code nameToIdCache} under its real profile.
     */
    public static FakePlayer occupy(GameTestHelper helper, String name) {
        ServerLevel level = helper.getLevel();
        FakePlayer player = create(helper, name);
        level.getServer().services().nameToIdCache().add(new NameAndId(player.getGameProfile()));
        if (!level.players().contains(player)) {
            level.addNewPlayer(player);
        }
        return player;
    }
}
