package committee.nova.mods.avaritia.core.singularity;

/** 可由脚本适配层安全隔离的奇点输入错误。 */
public class SingularityValidationException extends IllegalArgumentException {
    public SingularityValidationException(String message) {
        super(message);
    }
}
