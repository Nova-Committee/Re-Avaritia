package committee.nova.mods.avaritia.client.model.entity;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfinityArmorWingVisibilityTest {
    private static final Path MODEL_SOURCE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/client/model/entity/InfinityArmorModel.java");

    @Test
    void wingsRequireFullInfinityArmorAndFlying() throws IOException {
        String source = Files.readString(MODEL_SOURCE, StandardCharsets.UTF_8).replaceAll("\\s+", "");

        assertAll(
                () -> assertTrue(source.contains(
                                "booleanfullInfinityArmor=headItem==ModItems.infinity_helmet.get()" +
                                        "&&chestItem==ModItems.infinity_chestplate.get()" +
                                        "&&legsItem==ModItems.infinity_pants.get()" +
                                        "&&feetItem==ModItems.infinity_boots.get();"),
                        "完整套装判定必须同时检查四个对应装备槽"),
                () -> assertTrue(source.contains(
                                "if(fullInfinityArmor&&flying){poseStack.pushPose();ModelPartleftWing="),
                        "翅膀渲染必须同时要求完整套装与飞行状态"),
                () -> assertTrue(source.contains(
                                "if(fullInfinityArmor){submitBodyParts("),
                        "完整套装星空躯干层必须复用同一套装判定")
        );
    }
}
