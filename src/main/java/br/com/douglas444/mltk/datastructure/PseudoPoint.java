package br.com.douglas444.mltk.datastructure;

import java.util.Objects;

public class PseudoPoint implements ClusterSummary {

    private Sample centroid;
    private Double standardDeviation;
    private int n;

    public PseudoPoint(Sample centroid) {
        this.centroid = centroid;
    }

    public PseudoPoint(Sample centroid, double standardDeviation) {
        this.centroid = centroid;
        this.standardDeviation = standardDeviation;
    }

    @Override
    public Sample calculateCentroid() {
        return centroid;
    }

    @Override
    public double calculateStandardDeviation() {
        return standardDeviation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PseudoPoint that = (PseudoPoint) o;
        return n == that.n &&
                Objects.equals(centroid, that.centroid) &&
                Objects.equals(standardDeviation, that.standardDeviation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(centroid, standardDeviation, n);
    }

    public Sample getCentroid() {
        return centroid;
    }

    public void setCentroid(Sample centroid) {
        this.centroid = centroid;
    }

    public Double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(Double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

}
