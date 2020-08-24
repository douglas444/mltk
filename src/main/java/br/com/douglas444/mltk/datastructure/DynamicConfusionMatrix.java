package br.com.douglas444.mltk.datastructure;

import java.util.*;
import java.util.stream.Collectors;

public class DynamicConfusionMatrix {

    private final List<Integer> lineLabels;
    private final List<Integer> knownColumnLabels;
    private final List<Integer> novelColumnLabels;

    //Number of columns
    private int knownColumnsCount;
    private int novelColumnsCount;

    //Indices for matrix access
    private final HashMap<Integer, Integer> knownColumnIndexByLabel;
    private final HashMap<Integer, Integer> novelColumnIndexByLabel;
    private final HashMap<Integer, Integer> lineIndexByLabel;

    //Matrix
    private final List<List<Integer>> knownColumnsMatrix;
    private final List<List<Integer>> novelColumnsMatrix;
    private final List<Integer> unknownColumn;

    public DynamicConfusionMatrix() {

        this.lineLabels = new ArrayList<>();
        this.knownColumnLabels = new ArrayList<>();
        this.novelColumnLabels = new ArrayList<>();

        this.knownColumnsCount = 0;
        this.novelColumnsCount = 0;

        this.knownColumnIndexByLabel = new HashMap<>();
        this.novelColumnIndexByLabel = new HashMap<>();
        this.lineIndexByLabel = new HashMap<>();

        this.knownColumnsMatrix = new ArrayList<>();
        this.novelColumnsMatrix = new ArrayList<>();
        this.unknownColumn = new ArrayList<>();

    }

    public DynamicConfusionMatrix(List<Integer> knownLabels) {

        this.lineLabels = new ArrayList<>();
        this.knownColumnLabels = new ArrayList<>();
        this.novelColumnLabels = new ArrayList<>();

        this.knownColumnsCount = 0;
        this.novelColumnsCount = 0;

        this.knownColumnIndexByLabel = new HashMap<>();
        this.novelColumnIndexByLabel = new HashMap<>();
        this.lineIndexByLabel = new HashMap<>();

        this.knownColumnsMatrix = new ArrayList<>();
        this.novelColumnsMatrix = new ArrayList<>();
        this.unknownColumn = new ArrayList<>();

        knownLabels.forEach(this::addKnownLabel);
    }

    public boolean isLabelKnown(final Integer label) {
        return this.knownColumnLabels.contains(label);
    }

    public void addKnownLabel(final Integer label) {
        this.addKnownColumn(label);
        if (!this.lineLabels.contains(label)) {
            this.addLine(label);
        }
    }

    private void addLine(final Integer label) {
        this.lineIndexByLabel.put(label, lineLabels.size());
        this.lineLabels.add(label);
        this.knownColumnsMatrix.add(new ArrayList<>(Collections.nCopies(knownColumnsCount, 0)));
        this.novelColumnsMatrix.add(new ArrayList<>(Collections.nCopies(novelColumnsCount, 0)));
        this.unknownColumn.add(0);

    }

    private void addKnownColumn(final Integer label) {

        this.knownColumnLabels.add(label);
        this.knownColumnIndexByLabel.put(label, this.knownColumnsCount++);
        this.knownColumnsMatrix.forEach(line -> line.add(0));

    }

    private void addNovelColumn(final Integer label) {

        this.novelColumnLabels.add(label);
        this.novelColumnIndexByLabel.put(label, this.novelColumnsCount++);
        this.novelColumnsMatrix.forEach(line -> line.add(0));

    }

    public void updatedDelayed(final int realLabel, final int predictedLabel, final boolean isNovel) {

        if (!this.lineLabels.contains(realLabel)) {

            throw new RuntimeException("Invalid value for parameter realLabel");

        }

        final int lineIndex = this.lineIndexByLabel.get(realLabel);
        final int value = this.unknownColumn.get(lineIndex);
        this.unknownColumn.set(lineIndex, value - 1);

        this.addPrediction(realLabel, predictedLabel, isNovel);

    }

    public void addUnknown(final int realLabel) {

        if (!this.lineLabels.contains(realLabel)) {

            this.addLine(realLabel);

        }

        final int lineIndex = this.lineIndexByLabel.get(realLabel);
        final int value = this.unknownColumn.get(lineIndex);
        this.unknownColumn.set(lineIndex, value + 1);

    }

    public void addPrediction(final int realLabel, final int predictedLabel, final boolean isNovel) {


        if (!this.lineLabels.contains(realLabel)) {

            this.addLine(realLabel);

        }

        final int lineIndex = this.lineIndexByLabel.get(realLabel);

        if (isNovel) {

            if (!this.novelColumnLabels.contains(predictedLabel)) {
                this.addNovelColumn(predictedLabel);
            }

            final int columnIndex = this.novelColumnIndexByLabel.get(predictedLabel);
            final int count = this.novelColumnsMatrix.get(lineIndex).get(columnIndex);
            this.novelColumnsMatrix.get(lineIndex).set(columnIndex, count + 1);

        } else {

            if (!this.knownColumnIndexByLabel.containsKey(predictedLabel)) {
                throw new IllegalArgumentException("Predicted label is not known");

            }

            final int columnIndex = this.knownColumnIndexByLabel.get(predictedLabel);
            final int count = this.knownColumnsMatrix.get(lineIndex).get(columnIndex);
            this.knownColumnsMatrix.get(lineIndex).set(columnIndex, count + 1);

        }

    }

    @Override
    public String toString() {

        final List<Integer> sortedKnownColumnLabels = new ArrayList<>(this.knownColumnLabels);
        sortedKnownColumnLabels.sort(Comparator.comparingInt(lineLabels::indexOf));

        final int[][] matrix = new int[this.lineLabels.size() + 1][this.knownColumnsCount + this.novelColumnsCount + 2];

        for (int i = 0; i < sortedKnownColumnLabels.size(); ++i) {
            matrix[0][i + 1] = sortedKnownColumnLabels.get(i);
        }

        for (int i = 0; i < this.novelColumnLabels.size(); ++i) {
            matrix[0][i + sortedKnownColumnLabels.size() + 1] = this.novelColumnLabels.get(i);
        }

        for (int i = 0; i < this.lineLabels.size(); ++i) {
            matrix[i + 1][0] = this.lineLabels.get(i);
        }


        for (int i = 0; i < this.lineLabels.size(); ++i) {
            for (int j = 0; j < this.knownColumnsCount; ++j) {

                final int line = this.lineLabels.get(i);
                final int column = sortedKnownColumnLabels.get(j);

                final int lineIndex = this.lineIndexByLabel.get(line);
                final int columnIndex = this.knownColumnIndexByLabel.get(column);

                matrix[i + 1][j + 1] = this.knownColumnsMatrix.get(lineIndex).get(columnIndex);
            }
        }

        for (int i = 0; i < this.unknownColumn.size(); ++i) {
            matrix[i + 1][this.knownColumnsCount + this.novelColumnsCount + 1] = this.unknownColumn.get(i);
        }

        for (int i = 0; i < this.lineLabels.size(); ++i) {
            for (int j = 0; j < this.novelColumnsCount; ++j) {

                final int line = this.lineLabels.get(i);
                final int column = this.novelColumnLabels.get(j);

                final int lineIndex = this.lineIndexByLabel.get(line);
                final int columnIndex = this.novelColumnIndexByLabel.get(column);

                matrix[i + 1][j + this.knownColumnsCount + 1] =
                        this.novelColumnsMatrix.get(lineIndex).get(columnIndex);
            }

        }

        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < matrix.length; ++i) {
            for (int j = 0; j < matrix[0].length; ++j) {
                if (i == 0 && j == 0) {
                    stringBuilder.append(String.format("   %6s", ""));
                } else if (i == 0 && j > this.knownColumnsCount && j < this.knownColumnsCount + this.novelColumnsCount + 1) {
                    stringBuilder.append(String.format("|PN%6d", this.novelColumnIndexByLabel.get(matrix[i][j])));
                } else if (i == 0 && j > this.knownColumnsCount) {
                    stringBuilder.append(String.format("|%1sUNKNOWN", ""));
                } else if (j == 0 && i > this.knownColumnsCount){
                    stringBuilder.append(String.format("|CN%6d", matrix[i][j]));
                } else if (i == 0 || j == 0){
                    stringBuilder.append(String.format("|CK%6d", matrix[i][j]));
                } else {
                    stringBuilder.append(String.format("|  %6d", matrix[i][j]));
                }
            }
            stringBuilder.append("|\n");
        }

        return stringBuilder.toString();
    }

    public HashMap<Integer, List<Integer>> pnAssociation() {

        final HashMap<Integer, List<Integer>> association = new HashMap<>();

        for (Integer novelColumnLabel: this.novelColumnLabels) {

            int max = 0;
            int label = -1;

            for (Integer lineLabel: this.lineLabels) {
                final int line = this.lineIndexByLabel.get(lineLabel);
                final int column = this.novelColumnIndexByLabel.get(novelColumnLabel);
                if (this.novelColumnsMatrix.get(line).get(column) > max) {
                    max = this.novelColumnsMatrix.get(line).get(column);
                    label = lineLabel;
                }
            }

            if (label != -1) {
                List<Integer> pns;
                if ((pns = association.get(label)) != null) {
                    pns.add(novelColumnLabel);
                } else {
                    pns = new ArrayList<>();
                    pns.add(novelColumnLabel);
                    association.put(label, pns);
                }
            }
        }

        return association;

    }

    public int tp(final int label, final HashMap<Integer, List<Integer>> association) {

        int sum = 0;
        final int lineIndex = this.lineIndexByLabel.get(label);

        if (this.knownColumnLabels.contains(label)) {
            final int columnIndex = this.knownColumnIndexByLabel.get(label);
            sum += this.knownColumnsMatrix.get(lineIndex).get(columnIndex);
        }

        final List<Integer> pns = association.get(label);
        if (pns == null) {
            return sum;
        }

        sum += pns.stream()
                .map(pn -> this.novelColumnsMatrix.get(lineIndex).get(pn))
                .reduce(0,  Integer::sum);

        return sum;

    }

    public int fp(final int label, final HashMap<Integer, List<Integer>> association) {

        int sum = 0;

        if (this.knownColumnLabels.contains(label)) {

            final int columnIndex = this.knownColumnIndexByLabel.get(label);

            sum += this.lineLabels.stream()
                    .filter(lineLabel -> lineLabel != label)
                    .map(this.lineIndexByLabel::get)
                    .map(lineIndex -> this.knownColumnsMatrix.get(lineIndex).get(columnIndex))
                    .reduce(0, Integer::sum);

        }

        final List<Integer> pns = association.get(label);
        if (pns == null) {
            return sum;
        }

        sum += pns.stream()
                .map(pn ->

                    this.lineLabels.stream()
                            .filter(lineLabel -> lineLabel != label)
                            .map(this.lineIndexByLabel::get)
                            .map(lineIndex -> this.novelColumnsMatrix.get(lineIndex).get(pn))
                            .reduce(0, Integer::sum)

                )
                .reduce(0,  Integer::sum);

        return sum;

    }

    public int fn(final int label, final HashMap<Integer, List<Integer>> association) {

        int sum = 0;

        final int lineIndex = this.lineIndexByLabel.get(label);

        if (this.knownColumnLabels.contains(label)) {


            sum += this.knownColumnLabels.stream()
                    .filter(columnLabel -> columnLabel != label)
                    .map(this.knownColumnIndexByLabel::get)
                    .map(columnIndex -> this.knownColumnsMatrix.get(lineIndex).get(columnIndex))
                    .reduce(0, Integer::sum);

        }

        final List<Integer> pns = association.get(label);
        if (pns == null) {
            return sum;
        }

        sum += this.novelColumnLabels.stream()
                .filter(columnLabel -> !pns.contains(columnLabel))
                .map(this.novelColumnIndexByLabel::get)
                .map(columnIndex -> this.novelColumnsMatrix.get(lineIndex).get(columnIndex))
                .reduce(0, Integer::sum);

        return sum;
    }


    public int tn(final int label, final HashMap<Integer, List<Integer>> association) {

        return this.lineLabels.stream()
                .filter(lineLabel -> lineLabel != label)
                .map(lineLabel -> this.tp(lineLabel, association))
                .reduce(0, Integer::sum);

    }

    public int numberOfExplainedSamplesPerLabel(final int label) {

        final int lineIndex = lineIndexByLabel.get(label);

        int sum = knownColumnsMatrix.get(lineIndex)
                .stream()
                .reduce(0, Integer::sum);

        sum += novelColumnsMatrix.get(lineIndex)
                .stream()
                .reduce(0, Integer::sum);

        return sum;
    }

    public int numberOfExplainedSamples() {


        return this.lineLabels.stream().map(this::numberOfExplainedSamplesPerLabel).reduce(0, Integer::sum);


    }

    public double cer() {

        double sum = 0;

        final int totalExplainedSamples = numberOfExplainedSamples();
        final HashMap<Integer, List<Integer>> association = pnAssociation();

        sum += this.lineLabels.stream().map(lineLabel -> {

            final int fp = this.fp(lineLabel, association);
            final int fn = this.fn(lineLabel, association);

            final int numberOfExplainedSamples = this.numberOfExplainedSamplesPerLabel(lineLabel);

            if (numberOfExplainedSamples == 0 || (fp + tn(lineLabel, association) == 0)  || (fn + tp(lineLabel, association) == 0)) {

                return 0.0;

            } else {

                return ((double) numberOfExplainedSamples / totalExplainedSamples) *
                        ((double) fp / (fp + tn(lineLabel, association)))

                        + ((double) numberOfExplainedSamples / totalExplainedSamples) *
                        ((double) fn / (fn + tp(lineLabel, association)));

            }

        }).reduce(0.0, Double::sum);

        return sum / 2;
    }

    public double unkR() {

        return this.lineLabels.stream()
                .map(lineLabel -> {

                    final double unexplained = this.unknownColumn.get(this.lineIndexByLabel.get(lineLabel));
                    final double explained = this.numberOfExplainedSamplesPerLabel(lineLabel);

                    if (explained == 0) {
                        if (unexplained == 0) {
                            return 0.0;
                        } else {
                            return 1.0;
                        }
                    } else {
                        return unexplained / (explained + unexplained);
                    }

                })
                .reduce(0.0, Double::sum) / this.lineLabels.size();

    }

    public void print() {
        System.out.println(this.toString());
    }
}
