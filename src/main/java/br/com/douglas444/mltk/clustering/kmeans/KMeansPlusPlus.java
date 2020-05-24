package br.com.douglas444.mltk.clustering.kmeans;

import br.com.douglas444.mltk.datastructure.Cluster;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.*;

public final class KMeansPlusPlus {

    public static List<Cluster> execute(final List<Sample> samples, final int k, final long seed) {

        List<Sample> centroids = chooseCentroids(samples, k, new Random(seed));
        return KMeans.execute(samples, centroids);

    }

    private static List<Sample> chooseCentroids(final List<Sample> samples, final int k, final Random random) {

        List<Sample> centroids = new ArrayList<>();

        for (int i = 0; i < k; ++i) {
            Sample centroid = selectNextCentroid(samples, centroids, random);
            centroids.add(centroid);
        }

        return centroids;

    }

    private static Sample selectNextCentroid(final List<Sample> samples, final List<Sample> centroids,
                                           final Random random) {

        final HashMap<Sample, Double> probabilityBySample = mapProbabilityBySample(samples, centroids);
        final List<Map.Entry<Sample, Double>> entries = new ArrayList<>(probabilityBySample.entrySet());
        final Iterator<Map.Entry<Sample, Double>> iterator = entries.iterator();

        double cumulativeProbability = 0;
        Sample selected = null;
        final double r = random.nextDouble();

        while (selected == null) {

            final Map.Entry<Sample, Double> entry = iterator.next();

            cumulativeProbability += entry.getValue();

            if (r <= cumulativeProbability || !iterator.hasNext()) {
                selected = entry.getKey();
            }

        }

        return selected;

    }

    private static HashMap<Sample, Double> mapProbabilityBySample(final List<Sample> samples,
                                                                  final List<Sample> centroids) {

        final HashMap<Sample, Double> probabilityBySample = new HashMap<>();
        samples.forEach(sample -> probabilityBySample.put(sample, KMeans.distanceToTheClosestCentroid(sample, centroids)));

        final double sum = probabilityBySample.values().stream().mapToDouble(Double::doubleValue).sum();
        probabilityBySample.values().forEach(probability -> probability /= sum);

        return probabilityBySample;

    }

}
