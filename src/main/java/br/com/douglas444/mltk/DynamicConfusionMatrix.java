package br.com.douglas444.mltk;

import java.util.*;
import java.util.stream.Collectors;

public class DynamicConfusionMatrix {

    private int knownPredictedLabelsCount;
    private int novelPredictedLabelsCount;
    private int actualLabelsCount;
    private int predictedLabelsCount;

    private HashMap<Integer, Integer> actualLabelEnumByActualLabel;

    private HashMap<Integer, Integer> knownPredictedLabelEnumByPredictedLabel;
    private HashMap<Integer, Integer> novelPredictedLabelEnumByPredictedLabel;

    private HashMap<Integer, List<Integer>> predictionsAsKnownByActualLabelEnum;
    private HashMap<Integer, List<Integer>> predictionsAsNovelByActualLabelEnum;

    public DynamicConfusionMatrix(List<Integer> knownLabels) {

        this.actualLabelsCount = 0;
        this.predictedLabelsCount = 0;
        this.actualLabelEnumByActualLabel = new HashMap<>();

        this.knownPredictedLabelEnumByPredictedLabel = new HashMap<>();
        this.novelPredictedLabelEnumByPredictedLabel = new HashMap<>();

        this.knownPredictedLabelsCount = knownLabels.size();
        this.novelPredictedLabelsCount = 0;

        this.predictionsAsKnownByActualLabelEnum = new HashMap<>();
        this.predictionsAsNovelByActualLabelEnum = new HashMap<>();

        knownLabels.forEach(label -> {

            actualLabelEnumByActualLabel.put(label, actualLabelsCount);
            knownPredictedLabelEnumByPredictedLabel.put(label, predictedLabelsCount);

            ++actualLabelsCount;
            ++predictedLabelsCount;

            this.predictionsAsKnownByActualLabelEnum.put(actualLabelEnumByActualLabel.get(label),
                    new ArrayList<>(Collections.nCopies(knownLabels.size(), 0)));

            this.predictionsAsNovelByActualLabelEnum.put(actualLabelEnumByActualLabel.get(label), new ArrayList<>());

        });


    }

    public void add(int actualLabel, int predictedLabel, boolean isNovel) {


        if (actualLabelEnumByActualLabel.get(actualLabel) == null) {

            actualLabelEnumByActualLabel.put(actualLabel, actualLabelsCount);
            predictionsAsKnownByActualLabelEnum.put(actualLabelsCount,
                    new ArrayList<>(Collections.nCopies(knownPredictedLabelsCount, 0)));
            predictionsAsNovelByActualLabelEnum.put(actualLabelsCount,
                    new ArrayList<>(Collections.nCopies(novelPredictedLabelsCount, 0)));
            ++actualLabelsCount;
        }

        if (isNovel && novelPredictedLabelEnumByPredictedLabel.get(predictedLabel) == null) {

            novelPredictedLabelEnumByPredictedLabel.put(predictedLabel, novelPredictedLabelsCount);
            predictionsAsNovelByActualLabelEnum.forEach((key, value) -> value.add(0));
            ++predictedLabelsCount;
            ++novelPredictedLabelsCount;
        }

        int actualLabelEnum = actualLabelEnumByActualLabel.get(actualLabel);

        if (isNovel) {

            int predictedLabelEnum = novelPredictedLabelEnumByPredictedLabel.get(predictedLabel);
            int count = predictionsAsNovelByActualLabelEnum.get(actualLabelEnum).get(predictedLabelEnum);
            predictionsAsNovelByActualLabelEnum.get(actualLabelEnum).set(predictedLabelEnum, count + 1);

        } else {


            int predictedLabelEnum = knownPredictedLabelEnumByPredictedLabel.get(predictedLabel);
            int count = predictionsAsKnownByActualLabelEnum.get(actualLabelEnum).get(predictedLabelEnum);
            predictionsAsKnownByActualLabelEnum.get(actualLabelEnum).set(predictedLabelEnum, count + 1);

        }

    }

    @Override
    public String toString() {

        int[][] matrix = new int[actualLabelsCount + 1][predictedLabelsCount + 1];

        List<Integer> predictedLabels = knownPredictedLabelEnumByPredictedLabel
                .keySet()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        predictedLabels.addAll(novelPredictedLabelEnumByPredictedLabel
                .keySet()
                .stream()
                .sorted()
                .collect(Collectors.toList()));

        List<Integer> actualLabels = actualLabelEnumByActualLabel
                .keySet()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        for (int i = 0; i < knownPredictedLabelsCount; ++i) {
            matrix[0][i + 1] = predictedLabels.get(i);
        }

        for (int i = 0; i < actualLabels.size(); ++i) {
            matrix[i + 1][0] = actualLabels.get(i);
        }


        for (int i = knownPredictedLabelsCount; i < predictedLabels.size(); ++i) {
            matrix[0][i + 1] = predictedLabels.get(i);
        }


        for (int i = 0; i < actualLabelsCount; ++i) {
            for (int j = 0; j < knownPredictedLabelsCount; ++j) {

                int actualLabel = actualLabels.get(i);
                int predictedLabel = predictedLabels.get(j);

                int actualLabelEnum = actualLabelEnumByActualLabel.get(actualLabel);


                int predictedLabelEnum = knownPredictedLabelEnumByPredictedLabel.get(predictedLabel);

                matrix[i + 1][j + 1] = predictionsAsKnownByActualLabelEnum.get(actualLabelEnum).get(predictedLabelEnum);
            }
        }

        for (int i = 0; i < actualLabelsCount; ++i) {
            for (int j = knownPredictedLabelsCount; j < novelPredictedLabelsCount; ++j) {

                int actualLabel = actualLabels.get(i);
                int predictedLabel = predictedLabels.get(j);

                int actualLabelEnum = actualLabelEnumByActualLabel.get(actualLabel);
                int predictedLabelEnum = novelPredictedLabelEnumByPredictedLabel.get(predictedLabel);

                matrix[i + 1][j + knownPredictedLabelsCount + 1] =
                        predictionsAsNovelByActualLabelEnum.get(actualLabelEnum).get(predictedLabelEnum);
            }

        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < matrix.length; ++i) {
            for (int j = 0; j < matrix[0].length; ++j) {
                if (i == 0 && j == 0) {
                    stringBuilder.append(String.format("   %6s", ""));
                } else if (i == 0 && j > knownPredictedLabelsCount) {
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
