package br.com.douglas444.mltk.clustering.kmeans;

import br.com.douglas444.mltk.datastructure.ImpurityBasedCluster;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.*;
import java.util.stream.Collectors;

public final class MCIKMeans {

    public static List<ImpurityBasedCluster> execute(final List<Sample> labeledSamples,
                                                     final List<Sample> unlabeledSamples,
                                                     final int k, final long seed) {

        final HashMap<Integer, List<Sample>> samplesByLabel = new HashMap<>();

        labeledSamples.forEach(labeledSample -> {
            samplesByLabel.putIfAbsent(labeledSample.getY(), new ArrayList<>());
            samplesByLabel.get(labeledSample.getY()).add(labeledSample);
        });

        final List<Sample> centroids = new ArrayList<>();

        samplesByLabel.forEach((label, samples) -> {

            int numberOfCentroids = k * (samples.size() /  labeledSamples.size());
            centroids.addAll(chooseCentroids(samples, numberOfCentroids));

            if (centroids.size() < numberOfCentroids && !unlabeledSamples.isEmpty()) {
                final List<Sample> fillingSamples = new ArrayList<>(unlabeledSamples);
                while (centroids.size() < numberOfCentroids && !fillingSamples.isEmpty()) {
                    centroids.add(fillingSamples.remove(0));
                }
            }

        });

        return execute(labeledSamples, unlabeledSamples, centroids, new Random(seed));

    }

    private static List<ImpurityBasedCluster> execute(final List<Sample> labeledSamples,
                                                      final List<Sample> unlabeledSamples,
                                                      final List<Sample> centroids, final Random random) {

        List<ImpurityBasedCluster> clusters;
        List<Sample> oldCentroids;

        do {

            clusters = group(new ArrayList<>(labeledSamples), new ArrayList<>(unlabeledSamples), centroids, random);

            oldCentroids = new ArrayList<>(centroids);
            centroids.clear();

            clusters.stream()
                    .map(ImpurityBasedCluster::getCentroid)
                    .forEach(centroids::add);

        } while(!oldCentroids.containsAll(centroids));

        return clusters;

    }

    private static List<Sample> chooseCentroids(final List<Sample> samples, final int k) {

        if (samples.size() <= k) {
            return samples;
        }

        final List<Sample> centroids = new ArrayList<>();

        for (int i = 0; i < k; ++i) {
            Sample centroid = farthestFirstTraversalHeuristic(samples, centroids);
            centroids.add(centroid);
        }

        return centroids;

    }

    private static Sample farthestFirstTraversalHeuristic(final List<Sample> samples, final List<Sample> centroids) {

        if (samples.isEmpty()) {
            throw new IllegalArgumentException();
        }

        Sample farthest = samples.get(0);

        final Comparator<Sample> comparator = Comparator
                .comparing((sample) -> KMeans.distanceToTheClosestCentroid(sample, centroids));

        for (int i = 1; i < samples.size(); i++) {
            final Sample sample = samples.get(i);
            if (comparator.compare(sample, farthest) > 0) {
                farthest = sample;
            }
        }

        return farthest;
    }

    private static List<ImpurityBasedCluster> group(final List<Sample> labeledSamples,
                                                    final List<Sample> unlabeledSamples,
                                                    final List<Sample> centroids, final Random random) {

        final List<ImpurityBasedCluster> clusters = centroids.stream()
                .map(ImpurityBasedCluster::new)
                .collect(Collectors.toCollection(ArrayList::new));

        Collections.shuffle(labeledSamples, random);
        Collections.shuffle(unlabeledSamples, random);

        int numberOfSamples = labeledSamples.size() + unlabeledSamples.size();

        for (int i = 0; i < numberOfSamples; ++i) {

            final boolean isLabeled;
            final Sample sample;

            if (random.nextBoolean()) {
                isLabeled = true;
                sample = labeledSamples.remove(0);
            } else {
                isLabeled = false;
                sample = unlabeledSamples.remove(0);
            }

            final Comparator<ImpurityBasedCluster> comparator = Comparator.comparing((cluster) -> {

                if (isLabeled) {
                    return sample.distance(cluster.getCentroid()) *
                            (1 + cluster.getEntropy() * cluster.dissimilarityCount(sample));
                } else {
                    return sample.distance(cluster.getCentroid());
                }

            });

            ImpurityBasedCluster chosenCluster = clusters.get(0);

            for (int j = 1; j < clusters.size(); j++) {
                final ImpurityBasedCluster cluster = clusters.get(j);
                if (comparator.compare(cluster, chosenCluster) < 0) {
                    chosenCluster = cluster;
                }
            }

            if (isLabeled) {
                chosenCluster.addLabeledSample(sample);
            } else {
                chosenCluster.addUnlabeledSample(sample);
            }

        }

        return clusters;

    }

}
