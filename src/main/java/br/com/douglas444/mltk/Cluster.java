package br.com.douglas444.mltk;

import java.util.List;

public class Cluster {

    private List<Sample> samples;

    public Cluster(List<Sample> samples) {
        this.samples = samples;
    }

    /** Calculates center of the cluster.
     *
     * @return the center os the cluster.
     */
    public Sample calculateCenter() {

        Sample center = null;

        if (this.samples.size() > 0) {
            center = this.samples.get(0).copy();
            this.samples.subList(1, this.samples.size()).forEach(center::sum);
            center.divide(this.samples.size());
        }

        return center;

    }

    public double calculateStandardDeviation() {

        Sample center = this.calculateCenter();

        double sum = this
                .samples
                .stream()
                .mapToDouble(sample -> Math.pow(sample.distance(center), 2))
                .sum();

        return Math.sqrt(sum / this.samples.size());
    }

    public int getSize() {
        return samples.size();
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public void setSamples(List<Sample> samples) {
        this.samples = samples;
    }

}
