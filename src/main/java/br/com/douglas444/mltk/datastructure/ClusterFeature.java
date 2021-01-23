package br.com.douglas444.mltk.datastructure;

import br.com.douglas444.mltk.util.SampleDistanceComparator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClusterFeature implements ClusterSummary {

    private double[] ls;
    private double[][] ss;
    private int n;
    private Integer label;

    public ClusterFeature(double[] ls, double[][] ss, int n, Integer label) {
        this.ls = ls;
        this.ss = ss;
        this.n = n;
        this.label = label;
    }

    public ClusterFeature(double[] ls, double[][] ss, int n) {
        this.ls = ls;
        this.ss = ss;
        this.n = n;
    }

    @Override
    public Sample calculateCentroid() {

        final double[] x = this.ls.clone();
        for (int i = 0; i < x.length; ++i) {
            x[i] /= this.n;
        }

        return new Sample(x, this.label);
    }

    @Override
    public double calculateStandardDeviation() {

        double sum = 0;

        for (int i = 0; i < this.ss.length; ++i) {
            sum += (this.ss[i][i] / this.n) - Math.pow(this.ls[i] / this.n, 2);
        }

        return Math.sqrt(sum);

    }

    public static ClusterFeature calculateClosestMicroCluster(final Sample sample,
                                                              final List<ClusterFeature> clusterFeatures) {

        if (clusterFeatures.isEmpty()) {
            throw new IllegalArgumentException();
        }

        final HashMap<Sample, ClusterFeature> microClusterByCentroid = new HashMap<>();

        final List<Sample> decisionModelCentroids = clusterFeatures.stream()
                .map(microCluster -> {
                    Sample microClusterCentroid = microCluster.calculateCentroid();
                    microClusterByCentroid.put(microClusterCentroid, microCluster);
                    return microClusterCentroid;
                })
                .sorted(new SampleDistanceComparator(sample))
                .collect(Collectors.toList());

        final Sample closestCentroid = decisionModelCentroids.get(0);
        return microClusterByCentroid.get(closestCentroid);
    }

    public double[][] calculateCovarianceMatrix() {

        final double[][] covarianceMatrix = new double[ss.length][ss.length];

        for (int i = 0; i < this.ss.length; ++i) {
            for (int j = 0; j < this.ss.length; ++j) {
                covarianceMatrix[i][j] = (this.ss[i][j] / this.n) - (this.ls[i] * this.ls[j] / Math.pow(this.n, 2));
            }
        }

        return covarianceMatrix;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusterFeature that = (ClusterFeature) o;
        return n == that.n &&
                Arrays.equals(ls, that.ls) &&
                Arrays.equals(ss, that.ss) &&
                Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(n, label);
        result = 31 * result + Arrays.hashCode(ls);
        result = 31 * result + Arrays.hashCode(ss);
        return result;
    }

    public double[] getLs() {
        return ls;
    }

    public void setLs(double[] ls) {
        this.ls = ls;
    }

    public double[][] getSs() {
        return ss;
    }

    public void setSs(double[][] ss) {
        this.ss = ss;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public Integer getLabel() {
        return label;
    }

    public ClusterFeature setLabel(Integer label) {
        this.label = label;
        return this;
    }
}
