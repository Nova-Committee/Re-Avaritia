package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.locating.IModFile;

import java.nio.file.Path;

/**
 * @author cnlimiter
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PackResourceHandler {
    @SubscribeEvent
    public static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            IModFile modFile = ModList.get().getModFileById(Const.MOD_ID).getFile();
            Path resourcePath = modFile.findResource("resourcepacks", "avaritia");
            var pack = Pack.readMetaAndCreate(
                    "avaritia:default",
                    Component.translatable("title.avaritia.resourcepack"),
                    false,
                    (path) -> new net.minecraft.server.packs.PathPackResources("avaritia", resourcePath, false),
                    PackType.CLIENT_RESOURCES,
                    Pack.Position.TOP,
                    PackSource.BUILT_IN
            );
            if (pack != null) {
                event.addRepositorySource((infoConsumer) -> infoConsumer.accept(pack));
            }
        }
    }
}
