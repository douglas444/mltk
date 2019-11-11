package br.com.douglas444.mltk;

import java.util.*;
import java.util.stream.Collectors;

public class DynamicConfusionMatrix {

    private Set<Integer> lineLabels;
    private Set<Integer> columnLabels;

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

    public DynamicConfusionMatrix(List<Integer> knownLabels) {

        this.lineLabels = new HashSet<>();
        this.columnLabels = new HashSet<>();

        this.knownColumnsCount = 0;
        this.novelColumnsCount = 0;

        this.knownColumnIndexByLabel = new HashMap<>();
        this.novelColumnIndexByLabel = new HashMap<>();
        this.lineIndexByLabel = new HashMap<>();

        this.knownColumnsMatrix = new ArrayList<>();
        this.novelColumnsMatrix = new ArrayList<>();

        knownLabels.forEach(this::addKnownColumn);
        knownLabels.forEach(this::addLine);
    }

    private void addLine(Integer label) {

        this.lineIndexByLabel.put(label, lineLabels.size());
        this.lineLabels.add(label);
        this.knownColumnsMatrix.add(new ArrayList<>(Collections.nCopies(knownColumnsCount, 0)));
        this.novelColumnsMatrix.add(new ArrayList<>(Collections.nCopies(novelColumnsCount, 0)));

    }

    private void addKnownColumn(Integer label) {

        this.columnLabels.add(label);
        this.knownColumnIndexByLabel.put(label, this.knownColumnsCount++);
        this.knownColumnsMatrix.forEach(line -> line.add(0));

    }

    private void addNovelColumn(Integer label) {

        this.columnLabels.add(label);
        this.novelColumnIndexByLabel.put(label, this.novelColumnsCount++);
        this.novelColumnsMatrix.forEach(line -> line.add(0));

    }

    public void addPrediction(int realLabel, int predictedLabel) {


        if (!this.lineLabels.contains(realLabel)) {

            this.addLine(realLabel);

        }

        if (!this.columnLabels.contains(predictedLabel)) {
            this.addNovelColumn(predictedLabel);
        }

        int lineIndex = this.lineIndexByLabel.get(realLabel);

        if (!this.lineLabels.contains(predictedLabel)) {

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

        int[][] matrix = new int[this.lineLabels.size() + 1][this.knownColumnsCount + this.novelColumnsCount + 1];

        List<Integer> sortedColumnLabels = this.columnLabels.stream()
                .sorted()
                .collect(Collectors.toList());

        List<Integer> sortedLineLabels = this.lineLabels.stream()
                .sorted()
                .collect(Collectors.toList());

        for (int i = 0; i < this.knownColumnsCount; ++i) {
            matrix[0][i + 1] = sortedColumnLabels.get(i);
        }

        for (int i = this.knownColumnsCount; i < sortedColumnLabels.size(); ++i) {
            matrix[0][i + 1] = sortedColumnLabels.get(i);
        }

        for (int i = 0; i < sortedLineLabels.size(); ++i) {
            matrix[i + 1][0] = sortedLineLabels.get(i);
        }


        for (int i = 0; i < this.lineLabels.size(); ++i) {
            for (int j = 0; j < this.knownColumnsCount; ++j) {

                int line = sortedLineLabels.get(i);
                int column = sortedColumnLabels.get(j);

                int lineIndex = this.lineIndexByLabel.get(line);
                int columnIndex = this.knownColumnIndexByLabel.get(column);

                matrix[i + 1][j + 1] = this.knownColumnsMatrix.get(lineIndex).get(columnIndex);
            }
        }

        for (int i = 0; i < this.lineLabels.size(); ++i) {
            for (int j = this.knownColumnsCount; j < this.novelColumnsCount; ++j) {

                int actualLabel = sortedLineLabels.get(i);
                int predictedLabel = sortedColumnLabels.get(j);

                int actualLabelEnum = this.lineIndexByLabel.get(actualLabel);
                int predictedLabelEnum = this.novelColumnIndexByLabel.get(predictedLabel);

                matrix[i + 1][j + this.knownColumnsCount + 1] =
                        this.novelColumnsMatrix.get(actualLabelEnum).get(predictedLabelEnum);
            }

        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < matrix.length; ++i) {
            for (int j = 0; j < matrix[0].length; ++j) {
                if (i == 0 && j == 0) {
                    stringBuilder.append(String.format("   %6s", ""));
                } else if (i == 0 && j > this.knownColumnsCount) {
                    stringBuilder.append(String.format("|PN%6d", matrix[i][j]));
                } else if (i == 0 || j == 0){
                    stringBuilder.append(String.format("|C %6d", matrix[i][j]));
                } else {
                    stringBuilder.append(String.format("|  %6d", matrix[i][j]));
                }
            }
            stringBuilder.append("|\n");
        }

        return stringBuilder.toString();
    }
}
