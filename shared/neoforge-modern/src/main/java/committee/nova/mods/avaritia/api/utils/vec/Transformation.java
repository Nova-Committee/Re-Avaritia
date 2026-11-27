package committee.nova.mods.avaritia.api.utils.vec;

/**
 * Abstract supertype for any 3D vector transformation
 */
public abstract class Transformation extends ITransformation<Vector3, Transformation> {

    /**
     * Applies this transformation to a normal (doesn't translate)
     *
     * @param normal The normal to transform
     */
    public abstract void applyN(Vector3 normal);

    /**
     * Applies this transformation to a matrix as a multiplication on the right hand side.
     *
     * @param mat The matrix to combine this transformation with
     */
    public abstract void apply(Matrix4 mat);

    public Transformation at(Vector3 point) {
        return new TransformationList(new Translation(-point.x, -point.y, -point.z), this, point.translation());
    }

    public TransformationList with(Transformation t) {
        return new TransformationList(this, t);
    }
}
