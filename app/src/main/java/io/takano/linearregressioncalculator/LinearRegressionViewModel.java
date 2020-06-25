package io.takano.linearregressioncalculator;

import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.Primitive64Matrix;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.DoubleStream;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LinearRegressionViewModel extends ViewModel {
    private MutableLiveData<Double[]> x = new MutableLiveData<>();
    private MutableLiveData<Double[]> y = new MutableLiveData<>();
    private MutableLiveData<Double> learningRate = new MutableLiveData<>(0.01d);
    private MutableLiveData<Double[]> theta = new MutableLiveData<>();
    private MutableLiveData<Integer> polynomial = new MutableLiveData<>(2);
    private MutableLiveData<ArrayList<Double>> costHistory = new MutableLiveData<>();
    private MutableLiveData<String> type = new MutableLiveData<>("gd");

    public void initialize(Double[] x, Double[] y, Double learningRate, Integer polynomial, String type) {
        this.x.setValue(x);
        this.y.setValue(y);
        this.learningRate.setValue(learningRate);
        this.polynomial.setValue(polynomial);
        this.costHistory.setValue(new ArrayList<>());
        this.type.setValue(type);
    }

    public void beginCalculation() {
        switch(Objects.requireNonNull(type.getValue())) {
            case "gd":
                beginGradientDescent();
                break;
            case "ne":
                beginNormalEquation();
                break;
            default:
                break;
        }
    }

    public void beginGradientDescent() {
        LinearRegressionGradientDescentRunnable runnable = new LinearRegressionGradientDescentRunnable();
        new Thread(runnable).start();
    }

    public void beginNormalEquation() {
        LinearRegressionNormalEquationRunnable runnable = new LinearRegressionNormalEquationRunnable();
        new Thread(runnable).start();
    }

    public LiveData<Double[]> getTheta() {
        return theta;
    }

    public LiveData<ArrayList<Double>> getCostHistory() {
        return costHistory;
    }

    class LinearRegressionNormalEquationRunnable implements Runnable {

        Double[] staticXs, staticYs;
        Integer staticPolynomial;

        LinearRegressionNormalEquationRunnable() {
            this.staticXs = x.getValue();
            this.staticYs = y.getValue();
            this.staticPolynomial = polynomial.getValue();
        }

        @Override
        public void run() {
            Integer m = staticXs.length;
            // Matrix intermediate arrays have to be {1, 2, 3}, {4,5,6}, {7, 8, 9} format
            Double[][] arr_data = new Double[staticXs.length][staticPolynomial + 1];
            Double[][] arr_output = new Double[staticXs.length][1];
            for(int i = 0; i < staticXs.length; i++) {
                arr_data[i][0] = 1d; // Set first column to 1
                for(int p = 1; p <= staticPolynomial; p++) {
                    arr_data[i][p] = Math.pow(staticXs[i], p); // Set subsequent columns to powers of X_1
                }
                arr_output[i][0] = staticYs[i];
            }
            Primitive64Matrix.Factory matrixFactory = Primitive64Matrix.FACTORY;
            Primitive64Matrix designXMatrix = matrixFactory.rows(arr_data);
            Primitive64Matrix outputYMatrix = matrixFactory.rows(arr_output);

            Primitive64Matrix A = designXMatrix.transpose().multiply(designXMatrix);
            Primitive64Matrix B = A.invert();
            Primitive64Matrix C = B.multiply(designXMatrix.transpose());
            Primitive64Matrix ThetaMatrix = C.multiply(outputYMatrix);
            theta.postValue(DoubleStream.of(ThetaMatrix.toRawCopy1D()).boxed().toArray(Double[]::new));
        }
    }

    class LinearRegressionGradientDescentRunnable implements Runnable {

        // manage xs, ys separately inside this runnable, don't use viewmodel data
        Double[] staticXs, staticYs;
        Double staticLearningRate;
        Integer staticPolynomial;

        LinearRegressionGradientDescentRunnable() {
            this.staticXs = x.getValue();
            this.staticYs = y.getValue();
            this.staticLearningRate = learningRate.getValue();
            this.staticPolynomial = polynomial.getValue();
        }

        @Override
        public void run() {
            int m = staticXs.length;
            // Matrix intermediate arrays have to be {1, 2, 3}, {4,5,6}, {7, 8, 9} format
            Double[][] arrData = new Double[staticXs.length][staticPolynomial + 1];
            Double[][] arrOutput = new Double[staticXs.length][1];
            for(int i = 0; i < staticXs.length; i++) {
                arrData[i][0] = 1d; // Set first column to 1
                for(int p = 1; p <= staticPolynomial; p++) {
                    arrData[i][p] = Math.pow(staticXs[i], p); // Set subsequent columns to powers of X_1
                }
                arrOutput[i][0] = staticYs[i];
            }
            Double[][] arrTheta = new Double[staticPolynomial + 1][1];
            for(int i = 0; i < staticPolynomial + 1; i++) {
                arrTheta[i][0] = 0d;
            }
            Primitive64Matrix.Factory matrixFactory = Primitive64Matrix.FACTORY;
            Primitive64Matrix designXMatrix = matrixFactory.rows(arrData);
            Primitive64Matrix outputYMatrix = matrixFactory.rows(arrOutput);
            Primitive64Matrix currentThetaMatrix = matrixFactory.rows(arrTheta);

            boolean repeat = true;
            while(repeat) {
                Primitive64Matrix A = designXMatrix.multiply(currentThetaMatrix).subtract(outputYMatrix); // m x 1
                Primitive64Matrix cost = designXMatrix.transpose().multiply(A).multiply(1.0 / m); // n x 1
                ArrayList<Double> currentCostHistory = costHistory.getValue();
                if (currentCostHistory == null) {
                    currentCostHistory = new ArrayList<>();
                }
                Double lastCost = 999999d;
                if (currentCostHistory.size() > 0) {
                    lastCost = currentCostHistory.get(currentCostHistory.size() - 1);
                }
                Double currentCost = Math.abs(cost.reduceColumns(Aggregator.SUM).get(0, 0));
                if (currentCost.isNaN()) {
                    break;
                }
                double costPercentageChange = currentCost / lastCost;
                currentCostHistory.add(currentCost);
                costHistory.postValue(currentCostHistory);

                Primitive64Matrix nextThetaMatrix = currentThetaMatrix.subtract(cost.multiply(staticLearningRate));
                if (Math.abs(1 - costPercentageChange) < 0.0000001d) {
                    repeat = false;
                }
                currentThetaMatrix = nextThetaMatrix;
            }
            theta.postValue(DoubleStream.of(currentThetaMatrix.toRawCopy1D()).boxed().toArray(Double[]::new));
        }
    }

}
