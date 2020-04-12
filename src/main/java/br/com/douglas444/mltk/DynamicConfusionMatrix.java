package br.com.douglas444.mltk;

import java.util.*;
import java.util.stream.Collectors;

public class DynamicConfusionMatrix {

    private Set<Integer> lineLabels;
    private Set<Integer> knownColumnLabels;
    private Set<Integer> novelColumnLabels;

    //Number of columns
    private int knownColumnsCount;
    private int novelColumnsCount;

    //Indices for matrix access
    private HashMap<Integer, Integer> knownColumnIndexByLabel;
    private HashMap<Integer, Integer> novelColumnIndexByLabel;
    private HashMap<Integer, Integer> lineIndexByLabel;

    //Matrix
    private List<List<Integer>> knownColumnsMatrix;
    private List<List<Integer>> novelColumnsMatrix;
    private List<Integer> unkownColumn;

    public DynamicConfusionMatrix(List<Integer> knownLabels) {

        this.lineLabels = new HashSet<>();
        this.knownColumnLabels = new HashSet<>();
        this.novelColumnLabels = new HashSet<>();

        this.knownColumnsCount = 0;
        this.novelColumnsCount = 0;

        this.knownColumnIndexByLabel = new HashMap<>();
        this.novelColumnIndexByLabel = new HashMap<>();
        this.lineIndexByLabel = new HashMap<>();

        this.knownColumnsMatrix = new ArrayList<>();
        this.novelColumnsMatrix = new ArrayList<>();
        this.unkownColumn = new ArrayList<>();

        knownLabels.forEach(this::addKnownColumn);
        knownLabels.forEach(this::addLine);
    }

    private void addLine(Integer label) {

        this.lineIndexByLabel.put(label, lineLabels.size());
        this.lineLabels.add(label);
        this.knownColumnsMatrix.add(new ArrayList<>(Collections.nCopies(knownColumnsCount, 0)));
        this.novelColumnsMatrix.add(new ArrayList<>(Collections.nCopies(novelColumnsCount, 0)));
        this.unkownColumn.add(0);

    }

    private void addKnownColumn(Integer label) {

        this.knownColumnLabels.add(label);
        this.knownColumnIndexByLabel.put(label, this.knownColumnsCount++);
        this.knownColumnsMatrix.forEach(line -> line.add(0));

    }

    private void addNovelColumn(Integer label) {

        this.novelColumnLabels.add(label);
        this.novelColumnIndexByLabel.put(label, this.novelColumnsCount++);
        this.novelColumnsMatrix.forEach(line -> line.add(0));

    }

    public void updatedDelayed(int realLabel, int predictedLabel, boolean isNovel) {

        if (!this.lineLabels.contains(realLabel)) {

            throw new RuntimeException("Invalid value for parameter realLabel");

        }

        int lineIndex = this.lineIndexByLabel.get(realLabel);
        int value = this.unkownColumn.get(lineIndex);
        this.unkownColumn.set(lineIndex, value - 1);

        this.addPrediction(realLabel, predictedLabel, isNovel);

    }

    public void addUnknown(int realLabel) {

        if (!this.lineLabels.contains(realLabel)) {

            this.addLine(realLabel);

        }

        int lineIndex = this.lineIndexByLabel.get(realLabel);
        int value = this.unkownColumn.get(lineIndex);
        this.unkownColumn.set(lineIndex, value + 1);

    }

    public void addPrediction(int realLabel, int predictedLabel, boolean isNovel) {


        if (!this.lineLabels.contains(realLabel)) {

            this.addLine(realLabel);

        }

        int lineIndex = this.lineIndexByLabel.get(realLabel);

        if (isNovel) {

            if (!this.novelColumnLabels.contains(predictedLabel)) {
                this.addNovelColumn(predictedLabel);
            }

            int columnIndex = this.novelColumnIndexByLabel.get(predictedLabel);
            int count = this.novelColumnsMatrix.get(lineIndex).get(columnIndex);
            this.novelColumnsMatrix.get(lineIndex).set(columnIndex, count + 1);

        } else {

            int columnIndex = this.knownColumnIndexByLabel.get(predictedLabel);
            int count = this.knownColumnsMatrix.get(lineIndex).get(columnIndex);
            this.knownColumnsMatrix.get(lineIndex).set(columnIndex, count + 1);

        }

    }

    @Override
    public String toString() {

        int[][] matrix = new int[this.lineLabels.size() + 1][this.knownColumnsCount + this.novelColumnsCount + 2];

        List<Integer> sortedKnownColumnLabels = this.knownColumnLabels.stream()
                .sorted()
                .collect(Collectors.toList());

        List<Integer> sortedNovelColumnLabels = this.novelColumnLabels.stream()
                .sorted()
                .collect(Collectors.toList());

        List<Integer> sortedLineLabels = this.lineLabels.stream()
                .sorted()
                .collect(Collectors.toList());

        for (int i = 0; i < sortedKnownColumnLabels.size(); ++i) {
            matrix[0][i + 1] = sortedKnownColumnLabels.get(i);
        }

        for (int i = 0; i < sortedNovelColumnLabels.size(); ++i) {
            matrix[0][i + sortedKnownColumnLabels.size() + 1] = sortedNovelColumnLabels.get(i);
        }

        for (int i = 0; i < sortedLineLabels.size(); ++i) {
            matrix[i + 1][0] = sortedLineLabels.get(i);
        }


        for (int i = 0; i < this.lineLabels.size(); ++i) {
            for (int j = 0; j < this.knownColumnsCount; ++j) {

                int line = sortedLineLabels.get(i);
                int column = sortedKnownColumnLabels.get(j);

                int lineIndex = this.lineIndexByLabel.get(line);
                int columnIndex = this.knownColumnIndexByLabel.get(column);

                matrix[i + 1][j + 1] = this.knownColumnsMatrix.get(lineIndex).get(columnIndex);
            }
        }

        for (int i = 0; i < this.unkownColumn.size(); ++i) {
            matrix[i][this.knownColumnsCount + this.novelColumnsCount + 1] = this.unkownColumn.get(i);
        }

        for (int i = 0; i < this.lineLabels.size(); ++i) {
            for (int j = 0; j < this.novelColumnsCount; ++j) {

                int line = sortedLineLabels.get(i);
                int column = sortedNovelColumnLabels.get(j);

                int lineIndex = this.lineIndexByLabel.get(line);
                int columnIndex = this.novelColumnIndexByLabel.get(column);

                matrix[i + 1][j + this.knownColumnsCount + 1] =
                        this.novelColumnsMatrix.get(lineIndex).get(columnIndex);
            }

        }

        StringBuilder stringBuilder = new StringBuilder();
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

        HashMap<Integer, List<Integer>> association = new HashMap<>();

        for (Integer novelColumnLabel: this.novelColumnLabels) {

            int max = 0;
            int label = -1;

            for (Integer lineLabel: this.lineLabels) {
                int line = this.lineIndexByLabel.get(lineLabel);
                int column = this.novelColumnIndexByLabel.get(novelColumnLabel);
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

    public int tp(int label, HashMap<Integer, List<Integer>> association) {

        int sum = 0;
        int lineIndex = this.lineIndexByLabel.get(label);

        if (this.knownColumnLabels.contains(label)) {
            int columnIndex = this.knownColumnIndexByLabel.get(label);
            sum += this.knownColumnsMatrix.get(lineIndex).get(columnIndex);
        }

        List<Integer> pns = association.get(label);
        if (pns == null) {
            return sum;
        }

        sum += pns.stream()
                .map(pn -> this.novelColumnsMatrix.get(lineIndex).get(pn))
                .reduce(0,  Integer::sum);

        return sum;

    }

    public int fp(int label, HashMap<Integer, List<Integer>> association) {

        int sum = 0;

        if (this.knownColumnLabels.contains(label)) {

            int columnIndex = this.knownColumnIndexByLabel.get(label);

            sum += this.lineLabels.stream()
                    .filter(lineLabel -> lineLabel != label)
                    .map(lineLabel -> this.lineIndexByLabel.get(lineLabel))
                    .map(lineIndex -> this.knownColumnsMatrix.get(lineIndex).get(columnIndex))
                    .reduce(0, Integer::sum);

        }

        List<Integer> pns = association.get(label);
        if (pns == null) {
            return sum;
        }

        sum += pns.stream()
                .map(pn ->

                    this.lineLabels.stream()
                            .filter(lineLabel -> lineLabel != label)
                            .map(lineLabel -> this.lineIndexByLabel.get(lineLabel))
                            .map(lineIndex -> this.novelColumnsMatrix.get(lineIndex).get(pn))
                            .reduce(0, Integer::sum)

                )
                .reduce(0,  Integer::sum);

        return sum;

    }

    public int fn(int label, HashMap<Integer, List<Integer>> association) {

        int sum = 0;

        int lineIndex = this.lineIndexByLabel.get(label);

        if (this.knownColumnLabels.contains(label)) {


            sum += this.knownColumnLabels.stream()
                    .filter(columnLabel -> columnLabel != label)
                    .map(columnLabel -> this.lineIndexByLabel.get(columnLabel))
                    .map(columnIndex -> this.knownColumnsMatrix.get(lineIndex).get(columnIndex))
                    .reduce(0, Integer::sum);

        }

        List<Integer> pns = association.get(label);
        if (pns == null) {
            return sum;
        }

        sum += this.novelColumnLabels.stream()
                .filter(columnLabel -> !pns.contains(columnLabel))
                .map(columnLabel -> this.novelColumnIndexByLabel.get(columnLabel))
                .map(columnIndex -> this.novelColumnsMatrix.get(lineIndex).get(columnIndex))
                .reduce(0, Integer::sum);

        return sum;
    }


    public int tn(int label, HashMap<Integer, List<Integer>> association) {

        return this.lineLabels.stream()
                .filter(lineLabel -> lineLabel != label)
                .map(lineLabel -> this.tp(lineLabel, association))
                .reduce(0, Integer::sum);

    }

    public int numberOfExplainedSamplesPerLabel(int label) {

        int lineIndex = lineIndexByLabel.get(label);

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

        int totalExplainedSamples = numberOfExplainedSamples();
        HashMap<Integer, List<Integer>> association = pnAssociation();

        sum += this.lineLabels.stream().map(lineLabel -> {

            int fp = this.fp(lineLabel, association);

            return ((double) this.numberOfExplainedSamplesPerLabel(lineLabel) / totalExplainedSamples) *
                    ((double) fp / (fp + tn(lineLabel, association)));

        }).reduce(0.0, Double::sum);


        sum += this.lineLabels.stream().map(lineLabel -> {

            int fn = this.fn(lineLabel, association);

            return ((double) this.numberOfExplainedSamplesPerLabel(lineLabel) / totalExplainedSamples) *
                    ((double) fn / (fn + tp(lineLabel, association)));

        }).reduce(0.0, Double::sum);

        return sum / 2;
    }

    public double unkR() {

        return this.lineLabels.stream()
                .map(lineLabel -> {
                    int lineIndex = this.lineIndexByLabel.get(lineLabel);
                    return (double) this.unkownColumn.get(lineIndex) / this.numberOfExplainedSamplesPerLabel(lineLabel);
                })
                .reduce(0.0, Double::sum) / this.lineLabels.size();

    }
}
