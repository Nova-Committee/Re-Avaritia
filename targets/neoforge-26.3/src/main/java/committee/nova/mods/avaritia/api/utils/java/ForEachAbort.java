package committee.nova.mods.avaritia.api.utils.java;

/**
 * ForEachAbort
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/7 上午3:11
 */
public class ForEachAbort extends RuntimeException {

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
