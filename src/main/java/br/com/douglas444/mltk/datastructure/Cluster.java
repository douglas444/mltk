package br.com.douglas444.mltk.datastructure;

import java.util.ArrayList;
import java.util.List;

public class Cluster {

    private List<Sample> samples;

    public Cluster(List<Sample> samples) {

        if (samples.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.samples = new ArrayList<>(samples);
    }

    public Sample calculateCenter() {

        final Sample center = this.samples.get(0).copy();

        if (samples.size() > 1) {
            this.samples.subList(1, this.samples.size()).forEach(center::sum);
        }

        center.divide(this.samples.size());
        return center;

    }

    public double calculateStandardDeviation() {

        final Sample center = this.calculateCenter();

        final double sum = this.samples
                .stream()
                .mapToDouble(sample -> Math.pow(sample.distance(center), 2))
                .sum();

        return Math.sqrt(sum / this.samples.size());
    }

    public int getSize() {
        return samples.size();
    }

    public boolean isEmpty() {return samples.isEmpty();}

    public List<Sample> getSamples() {
        return samples;
    }

    public void setSamples(List<Sample> samples) {
        this.samples = samples;
    }

}
