package br.com.douglas444.mltk.datastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImpurityBasedCluster {

    private double entropy;
    private int numberOfLabeledSamples;
    private Sample centroid;
    private HashMap<Integer, List<Sample>> samplesByLabel;
    private List<Sample> unlabeledSamples;

    public ImpurityBasedCluster(Sample centroid) {

        this.centroid = centroid;

        this.numberOfLabeledSamples = 0;
        this.samplesByLabel = new HashMap<>();
        this.unlabeledSamples = new ArrayList<>();
        this.entropy = 0;
    }

    public ImpurityBasedCluster(List<Sample> labeledSamples, List<Sample> unlabeledSamples) {

        if ((labeledSamples == null || labeledSamples.isEmpty()) &&
                (unlabeledSamples == null || unlabeledSamples.isEmpty())) {
            throw new IllegalArgumentException();
        }

        this.numberOfLabeledSamples = 0;
        this.samplesByLabel = new HashMap<>();

        if (labeledSamples != null) {
            labeledSamples.forEach(labeledSample -> {
                this.samplesByLabel.putIfAbsent(labeledSample.getY(), new ArrayList<>());
                this.samplesByLabel.get(labeledSample.getY()).add(labeledSample);
                this.numberOfLabeledSamples++;
            });
        }

        this.unlabeledSamples = new ArrayList<>(unlabeledSamples);
        this.calculateEntropy();
        this.calculateCentroid();
    }

    public void addUnlabeledSample(Sample sample) {
        this.unlabeledSamples.add(sample);
        this.calculateCentroid();
    }

    public void addLabeledSample(Sample sample) {
        this.samplesByLabel.putIfAbsent(sample.getY(), new ArrayList<>());
        this.samplesByLabel.get(sample.getY()).add(sample);
        this.numberOfLabeledSamples++;
        this.calculateEntropy();
        this.calculateCentroid();
    }

    private void calculateEntropy() {

        this.entropy = this.samplesByLabel.keySet()
                .stream()
                .map(this::calculateLabelProbability)
                .map(p -> -p * Math.log(p))
                .reduce(0.0, Double::sum);

    }

    public Sample getCentroid() {
        return this.centroid;
    }

    public double getEntropy() {
        return this.entropy;
    }

    public double calculateLabelProbability(Integer label) {

        return (double) this.samplesByLabel.get(label).size() / this.numberOfLabeledSamples;

    }

    private void calculateCentroid() {

        final List<Sample> samples = new ArrayList<>();
        this.samplesByLabel.values().forEach(samples::addAll);
        samples.addAll(this.unlabeledSamples);

        final Sample centroid = samples.get(0).copy();

        if (samples.size() > 1) {
            samples.subList(1, samples.size()).forEach(centroid::sum);
        }

        centroid.divide(samples.size());

        this.centroid = centroid;

    }

    public double calculateStandardDeviation() {

        final List<Sample> samples = new ArrayList<>();
        this.samplesByLabel.values().forEach(samples::addAll);
        samples.addAll(this.unlabeledSamples);

        final Sample centroid = this.getCentroid();

        final double sum = samples
                .stream()
                .mapToDouble(sample -> Math.pow(sample.distance(centroid), 2))
                .sum();

        return Math.sqrt(sum / samples.size());
    }

    public int dissimilarityCount(final Sample labeledSample) {

        if (!this.samplesByLabel.containsKey(labeledSample.getY())) {
            return this.numberOfLabeledSamples;
        }

        return this.numberOfLabeledSamples - this.samplesByLabel.get(labeledSample.getY()).size();

    }

}
