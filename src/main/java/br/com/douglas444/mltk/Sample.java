package br.com.douglas444.mltk;

import java.util.Arrays;
import java.util.Objects;

public class Sample {

    private int t;
    private double[] x;
    private int y;

    public Sample(int t, double[] x, int y) {
        this.t = t;
        this.x = x;
        this.y = y;
    }

    public Sample(double[] x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(y);
        result = 31 * result + Arrays.hashCode(x);
        return result;
    }

    /** Calculates the distance of this sample to another.
     *
     * @param sample the sample to which the distance will be calculated.
     * @return the calculated distance.
     */
    public double distance(Sample sample) {
        double sum = 0;
        for (int i = 0; i < sample.getX().length; ++i) {
            sum += (sample.getX()[i] - this.getX()[i]) * (sample.getX()[i] - this.getX()[i]);
        }
        return Math.sqrt(sum);
    }

    /** Indicates whether the coordinates x and y of some other sample is
     * "equal to" this one.
     *
     * @param o the reference to the object to be compared
     * @return true if the coordinates x and y are the same as the sample
     * passed as argument; false otherwise or if the object passed isn't
     * a instance of br.com.douglas444.common.Sample class
     */
    @Override
    public boolean equals(Object o) {

        if (o.getClass() != Sample.class) {
            return false;
        }

        Sample sample = (Sample) o;

        for (int i = 0; i < sample.getX().length; ++i) {
            if (sample.getX()[i] != this.x[i]) {
                return false;
            }
        }

        return sample.getY() == this.y;

    }

    /** Sum coordinates x of another sample to this one. Coordinate y keeps the same.
     *
     * @param sample the sample to be added.
     */
    public void sum(Sample sample) {

        for (int i = 0; i < x.length; ++i) {
            x[i] += sample.getX()[i];
        }

    }

    /** Divide each coordinate of x by a scalar.
     *
     * @param scalar the scalar.
     */
    public void divide(double scalar) {

        for (int i = 0; i < x.length; ++i) {
            x[i] /= scalar;
        }

    }

    public void pow(int exp) {

        for (int i = 0; i < x.length; ++i) {
            x[i] = Math.pow(x[i], 2);
        }

    }

    /** Return a copy of the sample, with no reference between then.
     *
     * @return a copy of the sample.
     */
    public Sample copy() {

        Sample sample = new Sample(new double[x.length], y);
        for (int i = 0; i < x.length; ++i) {
            sample.getX()[i] = x[i];
        }
        return sample;

    }

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }

    public double[] getX() {
        return x;
    }

    public void setX(double[] x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

}
