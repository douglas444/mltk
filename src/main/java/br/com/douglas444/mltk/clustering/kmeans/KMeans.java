package br.com.douglas444.mltk.clustering.kmeans;

import br.com.douglas444.mltk.datastructure.Cluster;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.*;

public final class KMeans {

    public static List<Cluster> execute(final List<Sample> samples, final int k, final long seed) {

        List<Sample> centers = chooseCenters(samples, k, new Random(seed));
        return execute(samples, centers);

    }

    public static List<Cluster> execute(final List<Sample> samples, final List<Sample> centers) {

        List<Cluster> clusters;
        List<Sample> oldCenters;

        do {

            clusters = groupByClosestCenter(samples, centers);
            oldCenters = new ArrayList<>(centers);
            centers.clear();

            clusters.stream()
                    .filter(cluster -> !cluster.isEmpty())
                    .map(Cluster::calculateCenter)
                    .forEach(centers::add);

        } while(!oldCenters.containsAll(centers));

        return clusters;
    }

    protected static List<Sample> chooseCenters(final List<Sample> samples, final int k, final Random random) {

        final List<Sample> centers = new ArrayList<>();
        final List<Sample> candidates = new ArrayList<>(samples);

        int n = k;

        while (n > 0 && !candidates.isEmpty()) {
            final int randomIndex = random.nextInt(candidates.size());
            final Sample center = candidates.get(randomIndex);
            candidates.remove(center);
            centers.add(center);
            --n;
        }

        return centers;

    }

    protected static Optional<Sample> getClosestCenter(final Sample sample, final List<Sample> centers) {

        return centers.stream()
                .min(Comparator.comparing((center) -> center.distance(sample)));

    }

    protected static List<Cluster> groupByClosestCenter(final List<Sample> samples, final List<Sample> centers) {

        final HashMap<Sample, List<Sample>> samplesByCenter = new HashMap<>();

        centers.forEach(center -> samplesByCenter.put(center, new ArrayList<>()));

        samples.forEach(sample -> {
            getClosestCenter(sample, centers).ifPresent(closestCenter -> {
                samplesByCenter.get(closestCenter).add(sample);
            });
        });

        final List<Cluster> clusters = new ArrayList<>();

        samplesByCenter.forEach((key, value) -> {
            if (!value.isEmpty()) {
                clusters.add(new Cluster(value));
            }
        });

        return clusters;

    }


    protected static double distanceToTheClosestCenter(final Sample sample, List<Sample> centers) {

        final Optional<Sample> closestCenter = getClosestCenter(sample, centers);
        return closestCenter.map(sample::distance).orElse(0.0);

    }
}
