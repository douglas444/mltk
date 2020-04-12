package br.com.douglas444.mltk.kmeans;

import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.Sample;

import java.util.*;

public class KMeans {

    private List<Sample> samples;
    private List<Sample> centers;

    public KMeans(List<Sample> samples, int k, long seed) {
        this.samples = new ArrayList<>(samples);
        this.centers = chooseCenters(this.samples, k, new Random(seed));
    }

    private static List<Sample> chooseCenters(List<Sample> samples, int k, Random random) {

        List<Sample> centers = new ArrayList<>();
        List<Sample> candidates = new ArrayList<>(samples);

        while (k > 0 && !candidates.isEmpty()) {
            int randomIndex = random.nextInt(candidates.size());
            Sample center = candidates.get(randomIndex);
            candidates.remove(center);
            centers.add(center);
            --k;
        }

        return centers;

    }

    private static Optional<Sample> getClosestCenter(Sample sample, List<Sample> centers) {

        return centers.stream()
                .min(Comparator.comparing((center) -> center.distance(sample)));

    }

    private static List<Cluster> groupByClosestCenter(List<Sample> samples, List<Sample> centers) {

        HashMap<Sample, List<Sample>> samplesByCenter = new HashMap<>();

        centers.forEach(center -> samplesByCenter.put(center, new ArrayList<>()));

        samples.forEach(sample -> {
            getClosestCenter(sample, centers).ifPresent(closestCenter -> {
                samplesByCenter.get(closestCenter).add(sample);
            });
        });

        List<Cluster> clusters = new ArrayList<>();

        samplesByCenter.forEach((key, value) -> {
            if (!value.isEmpty()) {
                clusters.add(new Cluster(value));
            }
        });

        return clusters;

    }

    public List<Cluster> fit() {

        List<Cluster> clusters;
        List<Sample> oldCenters;

        do {

            clusters = groupByClosestCenter(samples, this.centers);

            oldCenters = new ArrayList<>(this.centers);
            centers.clear();

            for (Cluster cluster : clusters) {
                centers.add(cluster.calculateCenter());
            }

        } while(!oldCenters.containsAll(this.centers));

        return clusters;
    }

}
