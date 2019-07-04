package br.com.douglas444.mltk;

import java.util.*;

public class DynamicConfusionMatrix {

    private int knownPredictedLabelsCount;
    private int novelPredictedLabelsCount;
    private int actualLabelsCount;
    private int predictedLabelsCount;

    private HashMap<Integer, Integer> actualLabelEnumByActualLabel;
    private HashMap<Integer, Integer> predictedLabelEnumByPredictedLabel;

    private HashMap<Integer, List<Integer>> predictionsAsKnownByActualLabel;
    private HashMap<Integer, List<Integer>> predictionsAsNovelByActualLabel;

    public DynamicConfusionMatrix(List<Integer> knownLabels) {

        this.actualLabelsCount = 0;
        this.predictedLabelsCount = 0;

        actualLabelEnumByActualLabel = new HashMap<>();
        predictedLabelEnumByPredictedLabel = new HashMap<>();

        this.knownPredictedLabelsCount = knownLabels.size();
        this.novelPredictedLabelsCount = 0;

        this.predictionsAsKnownByActualLabel = new HashMap<>();
        this.predictionsAsNovelByActualLabel = new HashMap<>();

        knownLabels.forEach(label -> {

            actualLabelEnumByActualLabel.put(label, actualLabelsCount);
            predictedLabelEnumByPredictedLabel.put(label, predictedLabelsCount);
            ++actualLabelsCount;
            ++predictedLabelsCount;

            this.predictionsAsKnownByActualLabel.put(actualLabelEnumByActualLabel.get(label),
                    new ArrayList<>(Collections.nCopies(knownLabels.size(), 0)));

            this.predictionsAsNovelByActualLabel.put(actualLabelEnumByActualLabel.get(label), new ArrayList<>());

        });


    }

    public void add(int actualLabel, int predictedLabel, boolean isNovel) {


        if (actualLabelEnumByActualLabel.get(actualLabel) == null) {

            actualLabelEnumByActualLabel.put(actualLabel, actualLabelsCount);
            predictionsAsKnownByActualLabel.put(actualLabelsCount, new ArrayList<>(Collections.nCopies(knownPredictedLabelsCount, 0)));
            predictionsAsNovelByActualLabel.put(actualLabelsCount, new ArrayList<>(Collections.nCopies(novelPredictedLabelsCount, 0)));
            ++actualLabelsCount;
        }

        if (predictedLabelEnumByPredictedLabel.get(predictedLabel) == null) {

            predictedLabelEnumByPredictedLabel.put(predictedLabel, predictedLabelsCount);
            predictionsAsNovelByActualLabel.forEach((key, value) -> value.add(0));
            ++predictedLabelsCount;
            ++novelPredictedLabelsCount;
        }

        int actualLabelEnum = actualLabelEnumByActualLabel.get(actualLabel);
        int predictedLabelEnum = predictedLabelEnumByPredictedLabel.get(predictedLabel);

        if (isNovel) {

            int count = predictionsAsNovelByActualLabel.get(actualLabelEnum).get(predictedLabelEnum);
            predictionsAsNovelByActualLabel.get(actualLabelEnum).set(predictedLabelEnum, count + 1);

        } else {


            int count = predictionsAsKnownByActualLabel.get(actualLabelEnum).get(predictedLabelEnum);
            predictionsAsKnownByActualLabel.get(actualLabelEnum).set(predictedLabelEnum, count + 1);

        }

    }

    @Override
    public String toString() {
        int[][] matrix = new int[actualLabelsCount][predictedLabelsCount];

        int count = 0;
        for (Map.Entry<Integer, List<Integer>> entry : predictionsAsKnownByActualLabel.entrySet()) {

            List<Integer> value = entry.getValue();

            for (int i = 0; i < value.size(); ++i) {
                matrix[count][i] = value.get(i);
            }
            ++count;
        }

        count = 0;
        for (Map.Entry<Integer, List<Integer>> entry : predictionsAsNovelByActualLabel.entrySet()) {

            List<Integer> value = entry.getValue();

            for (int i = 0; i < value.size(); ++i) {
                matrix[count][knownPredictedLabelsCount + i] = value.get(i);
            }
            ++count;

        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int[] ints : matrix) {
            for (int anInt : ints) {
                stringBuilder.append(String.format("|%6d", anInt));
            }
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }
}
