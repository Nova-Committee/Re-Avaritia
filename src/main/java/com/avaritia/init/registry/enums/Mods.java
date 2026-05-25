package com.avaritia.init.registry.enums;

import com.avaritia.Avaritia;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

/**
 * 外部命名空间辅助枚举，供数据生成阶段拼接资源位置。
 */
public enum Mods {
    VANILLA("minecraft"),
    AVARITIA(Avaritia.MOD_ID),
    TCON("tconstruct"),
    DE("draconicevolution"),
    RS("refinedstorage"),
    SD("storagedrawers"),
    EIO("enderio"),

    CREATE("create"),
    MEK("mekanism", Builder::reverseMetalPrefix),
    MEK_GEN("mekanismgenerators", Builder::reverseMetalPrefix),
    TH("thermal"),
    IE("immersiveengineering", Builder::reverseMetalPrefix),
    FD("farmersdelight"),
    ARS_N("ars_nouveau"),
    BSK("blue_skies"),
    BTN("botania", Builder::omitWoodSuffix),
    FA("forbidden_arcanus"),
    HEX("hexcasting"),
    ID("integrateddynamics", Builder::strippedWoodIsSuffix),
    BWG("biomeswevegone"),
    SG("silentgear"),
    TIC("tconstruct"),
    AP("architects_palette"),
    Q("quark"),
    BOP("biomesoplenty"),
    TF("twilightforest"),
    ECO("ecologics"),
    IC2("ic2", Builder::reverseMetalPrefix),
    ATM("atmospheric"),
    ATM_2("atmospheric", Builder::omitWoodSuffix),
    AUTUM("autumnity"),
    DRUIDCRAFT("druidcraft"),
    ENDER("endergetic"),
    PVJ("projectvibrantjourneys"),
    UA("upgrade_aquatic"),
    BEF("betterendforge"),
    ENV("environmental"),
    SUP("supplementaries"),
    AM("alexsmobs"),
    NEA("neapolitan"),
    AE2("ae2"),
    MC("minecraft"),
    BB("buzzier_bees"),
    SILENT_GEMS("silentgems"),
    SF("simplefarming"),
    OREGANIZED("oreganized"),
    GS("galosphere"),
    VH("the_vault"),
    IX("infernalexp"),
    GOOD("goodending"),
    BMK("biomemakeover"),
    NE("nethers_exoticism"),
    RU("regions_unexplored"),
    EO("elementaryores"),
    IF("iceandfire"),
    ENS("exnihilosequentia"),
    AET("aether"),
    HH("hauntedharvest"),
    VMP("vampirism"),
    WSP("windswept"),
    D_AET("deep_aether"),
    A_AET("ancient_aether"),
    AET_R("aether_redux"),
    GOTD("gardens_of_the_dead"),
    UUE("unusualend"),
    UG("undergarden"),
    DD("deeperdarker"),
    ARS_E("ars_elemental", Builder::omitWoodSuffix),
    JNE("netherexp");

    private final String id;

    private boolean reversedMetalPrefix;
    private boolean strippedIsSuffix;
    private boolean omitWoodSuffix;

    Mods(String id) {
        this(id, b -> {
        });
    }

    Mods(String id, Consumer<Builder> props) {
        props.accept(new Builder());
        this.id = id;
    }

    public Identifier ingotOf(String type) {
        return Identifier.fromNamespaceAndPath(id, reversedMetalPrefix ? "ingot_" + type : type + "_ingot");
    }

    public Identifier nuggetOf(String type) {
        return Identifier.fromNamespaceAndPath(id, reversedMetalPrefix ? "nugget_" + type : type + "_nugget");
    }

    public Identifier oreOf(String type) {
        return Identifier.fromNamespaceAndPath(id, reversedMetalPrefix ? "ore_" + type : type + "_ore");
    }

    public Identifier deepslateOreOf(String type) {
        return Identifier.fromNamespaceAndPath(id, reversedMetalPrefix ? "deepslate_ore_" + type : "deepslate_" + type + "_ore");
    }

    public Identifier asResource(String id) {
        return Identifier.fromNamespaceAndPath(this.id, id);
    }

    public String getId() {
        return id;
    }

    public boolean reversedMetalPrefix() {
        return reversedMetalPrefix;
    }

    public boolean strippedIsSuffix() {
        return strippedIsSuffix;
    }

    public boolean omitWoodSuffix() {
        return omitWoodSuffix;
    }

    class Builder {
        Builder reverseMetalPrefix() {
            reversedMetalPrefix = true;
            return this;
        }

        Builder strippedWoodIsSuffix() {
            strippedIsSuffix = true;
            return this;
        }

        Builder omitWoodSuffix() {
            omitWoodSuffix = true;
            return this;
        }
    }
}
