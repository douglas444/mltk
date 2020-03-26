package br.com.douglas444.mltk.kmeans;

import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.Sample;

import java.util.*;

public class KMeans{

    private List<Sample> samples;
    private List<Cluster> clusters;

    public KMeans(List<Sample> samples, int k) {

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
            Sample center = randomSelectNextCenter(samples);
            samples.remove(center);
            centers.add(center);
        }

        return centers;

    }

    /** Selects the next center in a set of samples.
     *
     * @param samples list with the candidates samples.
     * @return the next center.
     */
    private static Sample randomSelectNextCenter(List<Sample> samples) {

        Random generator = new Random();
        generator.setSeed(0);
        return samples.get(generator.nextInt(samples.size()));

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
