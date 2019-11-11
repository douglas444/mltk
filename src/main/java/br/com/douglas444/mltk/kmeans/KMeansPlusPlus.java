package br.com.douglas444.mltk.kmeans;

import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.Sample;

import java.util.*;

public class KMeansPlusPlus {

    private List<Sample> samples;
    private List<Cluster> clusters;

    public KMeansPlusPlus(List<Sample> samples, int k) {

        this.samples = samples;

        Set<Sample> centers = chooseCenters(samples, k);
        this.clusters = groupByClosestCenter(samples, centers);

    }

    /** Chooses the initials centers.
     *
     * @param samples samples set.
     * @param k number of centers.
     * @return a list of centers.
     */
    private static Set<Sample> chooseCenters(List<Sample> samples, int k) {

        Set<Sample> centers = new HashSet<>();

        for (int i = 0; i < k; ++i) {
            Sample center = randomSelectNextCenter(samples, centers);
            //samples.remove(center);
            centers.add(center);
        }

        return centers;

    }

    /** Selects the next center in a set of samples.
     *
     * @param samples list with the candidates samples.
     * @param centers the list containing the current centers.
     * @return the next center.
     */
    private static Sample randomSelectNextCenter(List<Sample> samples, Set<Sample> centers) {

        Random generator = new Random();

        double roulette = 1;
        HashMap<Sample, Double> probabilityBySample = mapProbabilityBySample(samples, centers);

        List<Map.Entry<Sample, Double>> entries = new ArrayList<>(probabilityBySample.entrySet());
        Iterator<Map.Entry<Sample, Double>> iterator = entries.iterator();
        Sample selectedCenter = iterator.next().getKey();

        while (iterator.hasNext()) {

            Map.Entry<Sample, Double> entry = iterator.next();
            double r = generator.nextDouble() * roulette;
            if (r <= entry.getValue()) {
                selectedCenter = entry.getKey();
            } else {
                roulette -= entry.getValue();
            }

        }

        return selectedCenter;

    }

    /** Calculates the probability of each sample of be select as the next center.
     *
     * @param samples list with the candidates samples.
     * @param centers the list containing the current centers.
     * @return a map of probability by sample.
     */
    private static HashMap<Sample, Double> mapProbabilityBySample(List<Sample> samples, Set<Sample> centers) {

        HashMap<Sample, Double> probabilityBySample = new HashMap<>();
        samples.forEach(sample -> {
            probabilityBySample.put(sample, distanceToTheClosestCenter(sample, centers));
        });

        double sum = probabilityBySample.values().stream().mapToDouble(Double::doubleValue).sum();
        probabilityBySample.values().forEach(probability -> probability /= sum);

        return probabilityBySample;

    }

    /** Calculates de distance of a sample to the closest center.
     *
     * @param sample the target sample.
     * @param centers the list containing the centers.
     * @return the distance of the sample to the closest center.
     */
    private static double distanceToTheClosestCenter(Sample sample, Set<Sample> centers) {

        Sample closestCenter = getClosestCenter(sample, centers);

        if (closestCenter != null) {
            return sample.distance(closestCenter);
        } else {
            return 0;
        }

    }

    /** Calculates the closest center.
     *
     * @param sample the target sample.
     * @param centers the list containing the centers.
     * @return the closest center.
     */
    private static Sample getClosestCenter(Sample sample, Set<Sample> centers) {

        return centers
                .stream()
                .min(Comparator.comparing(center -> center.distance(sample)))
                .orElse(null);

    }

    /** Generates a list of clusters, grouping a list of sample by the closest center.
     *
     * @param samples samples to be grouped.
     * @param centers the list containing the centers.
     * @return a list of clusters.
     */
    private static List<Cluster> groupByClosestCenter(List<Sample> samples, Set<Sample> centers) {

        HashMap<Sample, List<Sample>> samplesByCenter = new HashMap<>();

        centers.forEach(center -> {
            samplesByCenter.put(center, new ArrayList<>());
        });

        samples.forEach(sample -> {
            Sample closestCenter = getClosestCenter(sample, centers);
            if (closestCenter != null) {
                samplesByCenter.get(closestCenter).add(sample);
            }
        });

        List<Cluster> clusters = new ArrayList<>();
        samplesByCenter.forEach((key, value) -> {
            if (value.size() > 0) {
                clusters.add(new Cluster(value));
            }
        });
        return clusters;

    }

    /** Calculates de center of the current clusters and regroup the samples
     * using the new centers. Repeat the process until the centers dont change.
     *
     * @return a list of clusters.
     */
    public List<Cluster> fit() {

        Set<Sample> newCenters = new HashSet<>();
        Set<Sample> oldCenters;

        do {

            oldCenters = new HashSet<>(newCenters);
            newCenters = new HashSet<>();

            for (Cluster cluster : clusters) {
                newCenters.add(cluster.calculateCenter());
            }

            clusters = groupByClosestCenter(samples, newCenters);

        } while(!oldCenters.containsAll(newCenters));

        return clusters;
    }

}
