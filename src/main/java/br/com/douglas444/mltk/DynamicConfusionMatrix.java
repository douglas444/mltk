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

        this.knownColumnLabels.add(label);
        this.knownColumnIndexByLabel.put(label, this.knownColumnsCount++);
        this.knownColumnsMatrix.forEach(line -> line.add(0));

    }

    private void addNovelColumn(Integer label) {

        this.novelColumnLabels.add(label);
        this.novelColumnIndexByLabel.put(label, this.novelColumnsCount++);
        this.novelColumnsMatrix.forEach(line -> line.add(0));

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

        int[][] matrix = new int[this.lineLabels.size() + 1][this.knownColumnsCount + this.novelColumnsCount + 1];

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
                } else if (i == 0 && j > this.knownColumnsCount) {
                    stringBuilder.append(String.format("|PN%6d", this.novelColumnIndexByLabel.get(matrix[i][j])));
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
}
