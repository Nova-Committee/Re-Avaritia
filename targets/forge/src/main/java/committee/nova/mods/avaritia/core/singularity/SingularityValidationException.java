package committee.nova.mods.avaritia.core.singularity;

/**
 * Identifies authored singularity input errors that script adapters may report
 * and isolate without swallowing unrelated script failures.
 */
public class SingularityValidationException extends IllegalArgumentException {
    public SingularityValidationException(String message) {
        super(message);
    }
}
