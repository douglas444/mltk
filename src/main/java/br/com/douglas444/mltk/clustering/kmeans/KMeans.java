package br.com.douglas444.mltk.clustering.kmeans;

import br.com.douglas444.mltk.datastructure.Cluster;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.*;

public final class KMeans {

    public static List<Cluster> execute(final List<Sample> samples, final int k, final long seed) {

        List<Sample> centroids = chooseCentroids(samples, k, new Random(seed));
        return execute(samples, centroids);

    }

    public static List<Cluster> execute(final List<Sample> samples, final List<Sample> centroids) {

        List<Cluster> clusters;
        List<Sample> oldCentroids;

        do {

            clusters = groupByClosestCentroid(samples, centroids);
            oldCentroids = new ArrayList<>(centroids);
            centroids.clear();

            clusters.stream()
                    .filter(cluster -> !cluster.isEmpty())
                    .map(Cluster::calculateCentroid)
                    .forEach(centroids::add);

        } while(!oldCentroids.containsAll(centroids));

        return clusters;
    }

    protected static List<Sample> chooseCentroids(final List<Sample> samples, final int k, final Random random) {

        final List<Sample> centroids = new ArrayList<>();
        final List<Sample> candidates = new ArrayList<>(samples);

        int n = k;

        while (n > 0 && !candidates.isEmpty()) {
            final int randomIndex = random.nextInt(candidates.size());
            final Sample centroid = candidates.get(randomIndex);
            candidates.remove(centroid);
            centroids.add(centroid);
            --n;
        }

        return centroids;

    }

    protected static Optional<Sample> getClosestCentroid(final Sample sample, final List<Sample> centroids) {

        return centroids.stream()
                .min(Comparator.comparing((centroid) -> centroid.distance(sample)));

    }

    protected static List<Cluster> groupByClosestCentroid(final List<Sample> samples, final List<Sample> centroids) {

        final HashMap<Sample, List<Sample>> samplesByCentroid = new HashMap<>();

        centroids.forEach(centroid -> samplesByCentroid.put(centroid, new ArrayList<>()));

        samples.forEach(sample -> {
            getClosestCentroid(sample, centroids).ifPresent(closestCentroid -> {
                samplesByCentroid.get(closestCentroid).add(sample);
            });
        });

        final List<Cluster> clusters = new ArrayList<>();

        samplesByCentroid.forEach((key, value) -> {
            if (!value.isEmpty()) {
                clusters.add(new Cluster(value));
            }
        });

        return clusters;

    }


    protected static double distanceToTheClosestCentroid(final Sample sample, List<Sample> centroids) {

        final Optional<Sample> closestCentroid = getClosestCentroid(sample, centroids);
        return closestCentroid.map(sample::distance).orElse(0.0);

    }
}
