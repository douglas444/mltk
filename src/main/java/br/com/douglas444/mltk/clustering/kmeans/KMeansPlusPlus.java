package br.com.douglas444.mltk.clustering.kmeans;

import br.com.douglas444.mltk.datastructure.Cluster;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.*;

public final class KMeansPlusPlus {

    public static List<Cluster> execute(final List<Sample> samples, final int k, final long seed) {

        List<Sample> centers = chooseCenters(samples, k, new Random(seed));
        return KMeans.execute(samples, centers);

    }

    private static List<Sample> chooseCenters(final List<Sample> samples, final int k, final Random random) {

        List<Sample> centers = new ArrayList<>();

        for (int i = 0; i < k; ++i) {
            Sample center = selectNextCenter(samples, centers, random);
            centers.add(center);
        }

        return centers;

    }

    private static Sample selectNextCenter(final List<Sample> samples, final List<Sample> centers,
                                           final Random random) {

        final HashMap<Sample, Double> probabilityBySample = mapProbabilityBySample(samples, centers);
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
                                                                  final List<Sample> centers) {

        final HashMap<Sample, Double> probabilityBySample = new HashMap<>();
        samples.forEach(sample -> probabilityBySample.put(sample, KMeans.distanceToTheClosestCenter(sample, centers)));

        final double sum = probabilityBySample.values().stream().mapToDouble(Double::doubleValue).sum();
        probabilityBySample.values().forEach(probability -> probability /= sum);

        return probabilityBySample;

    }

}
