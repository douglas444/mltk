package br.com.douglas444.mltk;

import java.util.Comparator;

public class DistanceComparator implements Comparator<Sample> {

    private Sample target;

    public DistanceComparator(Sample target) {
        this.target = target;
    }

    /** Compares the distances of the two samples passed as argument to the
     * the target sample defined as a class attribute.
     *
     * @return Returns 0 if p1 and p2 have the same distance to the target
     * sample, -1 if p1 are closer to the target sample and returns 1 if p1
     * are closer to the target sample.
     */
    @Override
    public int compare(Sample p1, Sample p2) {

        double d1 = p1.distance(this.target);
        double d2 = p2.distance(this.target);

        return Double.compare(d1, d2);
    }

    public Sample getTarget() {
        return target;
    }

    public void setTarget(Sample target) {
        this.target = target;
    }

}
