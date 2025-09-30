package committee.nova.mods.avaritia.common.tile.compressor;


/**
 * 压缩机等级枚举
 */
public enum CompressorTier {
    // 基础等级（默认）
    DEFAULT("compressor", 1.0f, 1.0f, 1),
    // 致密等级 - 工作时间减半
    DENSE("dense_compressor", 0.5f, 1.0f, 1),
    // 精英等级 - 工作时间1/4，材料消耗3/4
    DENSER("denser_compressor", 0.25f, 0.75f, 1),
    // 极限等级 - 工作时间1/8，材料消耗1/2，产出翻倍
    DENSEST("densest_compressor", 0.125f, 0.5f, 2);

    public final String name;
    public final float timeMultiplier;     // 时间倍数
    public final float materialMultiplier; // 材料消耗倍数
    public final int outputMultiplier;   // 产出倍数

    CompressorTier(String name, float timeMultiplier, float materialMultiplier, int outputMultiplier) {
        this.name = name;
        this.timeMultiplier = timeMultiplier;
        this.materialMultiplier = materialMultiplier;
        this.outputMultiplier = outputMultiplier;
    }
}
