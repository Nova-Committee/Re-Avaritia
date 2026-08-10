package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModArmorMaterial;
import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

import java.util.function.BiConsumer;

public class AvaritiaEquipmentAssetProvider extends EquipmentAssetProvider {
    public AvaritiaEquipmentAssetProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void registerModels(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output) {
        output.accept(ModArmorMaterial.INFINITY_ARMOR_ASSET, EquipmentClientInfo.builder()
                .addHumanoidLayers(Const.rl("infinity_armor"), false)
                .addLayers(EquipmentClientInfo.LayerType.HORSE_BODY, new EquipmentClientInfo.Layer(Const.rl("infinity_armor")))
                .addLayers(EquipmentClientInfo.LayerType.NAUTILUS_BODY, new EquipmentClientInfo.Layer(Const.rl("neutron")))
                .build());
        output.accept(ModArmorMaterial.NEUTRON_WOLF_ARMOR_ASSET, EquipmentClientInfo.builder()
                .addLayers(EquipmentClientInfo.LayerType.WOLF_BODY, new EquipmentClientInfo.Layer(Const.rl("neutron_wolf_armor")))
                .build());

        output.accept(ModArmorMaterial.NEUTRON_HARNESS_ASSET, EquipmentClientInfo.builder()
                .addLayers(EquipmentClientInfo.LayerType.HAPPY_GHAST_BODY, new EquipmentClientInfo.Layer(Const.rl("neutron_harness")))
                .build());
    }
}
