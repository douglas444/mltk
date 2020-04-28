package br.com.douglas444.mltk.clustering.kmeans;

import br.com.douglas444.mltk.datastructure.Cluster;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.*;

public class KMeans {

    public static List<Cluster> execute(final List<Sample> samples, final int k, final long seed) {
        return execute(samples, k, seed, true);
    }

    public static List<Cluster> execute(final List<Sample> samples, final int k, final long seed,
                                        final boolean usePlusPlus) {

        final List<Sample> centers = usePlusPlus ? chooseCentersPlusPlus(samples, k, new Random(seed)) :
                chooseCenters(samples, k, new Random(seed));

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

    private static List<Sample> chooseCenters(final List<Sample> samples, final int k, final Random random) {

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

    private static List<Sample> chooseCentersPlusPlus(final List<Sample> samples, final int k, final Random random) {

        List<Sample> centers = new ArrayList<>();

        for (int i = 0; i < k; ++i) {
            Sample center = randomSelectNextCenter(samples, centers, random);
            centers.add(center);
        }

        return centers;

    }

    private static Sample randomSelectNextCenter(final List<Sample> samples, final List<Sample> centers,
                                                 final Random generator) {

        final HashMap<Sample, Double> probabilityBySample = mapProbabilityBySample(samples, centers);
        final List<Map.Entry<Sample, Double>> entries = new ArrayList<>(probabilityBySample.entrySet());
        final Iterator<Map.Entry<Sample, Double>> iterator = entries.iterator();

        Sample selectedCenter = iterator.next().getKey();
        double roulette = 1;

        while (iterator.hasNext()) {

            final Map.Entry<Sample, Double> entry = iterator.next();
            final double r = generator.nextDouble() * roulette;
            if (r <= entry.getValue()) {
                selectedCenter = entry.getKey();
            } else {
                roulette -= entry.getValue();
            }

        }

        return selectedCenter;

    }

    private static HashMap<Sample, Double> mapProbabilityBySample(final List<Sample> samples,
                                                                  final List<Sample> centers) {

        final HashMap<Sample, Double> probabilityBySample = new HashMap<>();
        samples.forEach(sample -> {
            probabilityBySample.put(sample, distanceToTheClosestCenter(sample, centers));
        });

        final double sum = probabilityBySample.values().stream().mapToDouble(Double::doubleValue).sum();
        probabilityBySample.values().forEach(probability -> probability /= sum);

        return probabilityBySample;

    }

    private static double distanceToTheClosestCenter(final Sample sample, List<Sample> centers) {

        final Optional<Sample> closestCenter = getClosestCenter(sample, centers);
        return closestCenter.map(sample::distance).orElse(0.0);

    }
}
