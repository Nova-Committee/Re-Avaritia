package committee.nova.mods.avaritia.api.utils.vec.uv;

import committee.nova.mods.avaritia.api.utils.math.MathUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class UVTranslation extends UVTransformation {

    public double du;
    public double dv;

    public UVTranslation(double u, double v) {
        du = u;
        dv = v;
    }

    public UVTranslation(UVTranslation other) {
        this(other.du, other.dv);
    }

    @Override
    public void apply(UV uv) {
        uv.u += du;
        uv.v += dv;
    }

    @Override
    public UVTransformation at(UV point) {
        return this;
    }

    @Override
    public UVTransformation inverse() {
        return new UVTranslation(-du, -dv);
    }

    @Override
    public UVTransformation merge(UVTransformation next) {
        if (next instanceof UVTranslation t) {
            return new UVTranslation(du + t.du, dv + t.dv);
        }

        return null;
    }

    @Override
    public boolean isRedundant() {
        return MathUtils.between(-1E-5, du, 1E-5) && MathUtils.between(-1E-5, dv, 1E-5);
    }

    @Override
    public String toString() {
        MathContext cont = new MathContext(4, RoundingMode.HALF_UP);
        return "UVTranslation(" + new BigDecimal(du, cont) + ", " + new BigDecimal(dv, cont) + ")";
    }

    @Override
    public UVTranslation copy() {
        return new UVTranslation(this);
    }
}
