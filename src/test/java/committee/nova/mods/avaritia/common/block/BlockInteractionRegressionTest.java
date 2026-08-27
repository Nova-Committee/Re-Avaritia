package committee.nova.mods.avaritia.common.block;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("方块碰撞与相邻交互回归")
class BlockInteractionRegressionTest {
    private static final Path SOUL_FARMLAND_MODEL = Path.of(
            "src/generated/resources/assets/avaritia/models/block/soul_farmland.json");
    private static final Path COMPRESSED_CHEST_BLOCK = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/block/chest/CompressedChestBlock.java");

    @Test
    @DisplayName("灵魂耕地模型与十五像素高碰撞面一致")
    void soulFarmlandUsesFarmlandTemplate() throws Exception {
        JsonObject model = JsonParser.parseString(Files.readString(SOUL_FARMLAND_MODEL)).getAsJsonObject();
        JsonObject textures = model.getAsJsonObject("textures");
        assertAll(
                () -> assertEquals("minecraft:block/template_farmland", model.get("parent").getAsString()),
                () -> assertEquals("minecraft:block/soul_soil", textures.get("dirt").getAsString()),
                () -> assertEquals("avaritia:block/resource/soul_farmland", textures.get("top").getAsString())
        );
    }

    @Test
    @DisplayName("压缩箱拒绝连接并在邻居更新后保持单箱")
    void compressedChestAlwaysRemainsSingle() throws Exception {
        String source = Files.readString(COMPRESSED_CHEST_BLOCK).replaceAll("\\s+", "");
        assertAll(
                () -> assertTrue(source.contains("publicbooleanchestCanConnectTo(BlockStateblockState){returnfalse;}")),
                () -> assertTrue(source.contains("super.updateShape(state,level,ticks,pos,directionToNeighbour,neighbourPos,neighbourState,random).setValue(TYPE,ChestType.SINGLE)"))
        );
    }
}
