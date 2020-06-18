package br.com.douglas444.mltk.datastructure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Cluster {

    private List<Sample> samples;
    private Sample mostRecentSample;

    public Cluster(List<Sample> samples) {

        if (samples.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.mostRecentSample = null;
        this.samples = new ArrayList<>(samples);
    }

    public Sample calculateCentroid() {

        final Sample centroid = this.samples.get(0).copy();

        if (this.samples.size() > 1) {
            this.samples.subList(1, this.samples.size()).forEach(centroid::sum);
        }

        centroid.divide(this.samples.size());
        return centroid;

    }

    public double calculateStandardDeviation() {

        final Sample centroid = this.calculateCentroid();

        final double sum = this.samples
                .stream()
                .mapToDouble(sample -> Math.pow(sample.distance(centroid), 2))
                .sum();

        return Math.sqrt(sum / this.samples.size());
    }

    public Sample getMostRecentSample() {

        if (this.samples.isEmpty()) {
            throw new IllegalArgumentException();
        }

        if (this.mostRecentSample == null) {
            this.mostRecentSample = this.samples
                    .stream()
                    .min(Comparator.comparing(Sample::getT))
                    .orElse(this.getSamples().get(0));
        }

        return mostRecentSample;

    }

    public int getSize() {
        return samples.size();
    }

    public boolean isEmpty() {return samples.isEmpty();}

    public List<Sample> getSamples() {
        return samples;
    }


}
