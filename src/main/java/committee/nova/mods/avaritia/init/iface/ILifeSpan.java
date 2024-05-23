package committee.nova.mods.avaritia.init.iface;

/**
 * ILifeSpan
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/24 上午3:30
 */
public interface ILifeSpan {
    default int getLifeSpan(){
        return 6000;
    }

    void setLifeSpan(int lifeSpan);
}
