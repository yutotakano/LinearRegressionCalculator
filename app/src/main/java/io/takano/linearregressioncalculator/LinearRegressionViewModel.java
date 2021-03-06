package io.takano.linearregressioncalculator;

import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.Primitive64Matrix;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * Populate the required values for calculation.
     * @param x Input array
     * @param y Output array
     * @param learningRate How fast should gradient descent descend? i.e. value of alpha
     * @param polynomial Max polynomial to take into account
     * @param type Gradient Descent "gd" or Normal Equation "ne"
     */
    public void initialize(Double[] x, Double[] y, Double learningRate, Integer polynomial, String type) {
        this.x.setValue(x);
        this.y.setValue(y);
        this.learningRate.setValue(learningRate);
        this.polynomial.setValue(polynomial);
        this.costHistory.setValue(new ArrayList<>());
        this.type.setValue(type);
    }

    /**
     * Begin the calculation function using the type variable as a switch.
     */
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

    /**
     * Execute the LinearRegressionGradientDescentRunnable runnable.
     */
    public void beginGradientDescent() {
        LinearRegressionGradientDescentRunnable runnable = new LinearRegressionGradientDescentRunnable();
        new Thread(runnable).start();
    }

    /**
     * Execute the LinearRegressionNormalEquationRunnable runnable.
     */
    public void beginNormalEquation() {
        LinearRegressionNormalEquationRunnable runnable = new LinearRegressionNormalEquationRunnable();
        new Thread(runnable).start();
    }

    /**
     * Retrieve the value of theta, which is either null or the calculated value.
     * @return Calculated value of theta, or null
     */
    public LiveData<Double[]> getTheta() {
        return theta;
    }

    /**
     * Retrieve the value of costHistory
     * @return ever-expanding ArrayList reference
     */
    public LiveData<ArrayList<Double>> getCostHistory() {
        return costHistory;
    }

    /**
     * Generate plot points for the calculated theta taking into account the max/min Xs
     * @return Entries to be used for MPAndroidChart
     */
    public LiveData<List<List<Entry>>> generateChartEntries() {
        // Find the domain of the chart
        Double[] staticX = x.getValue();
        Double[] staticY = y.getValue();
        Integer staticPolynomial = polynomial.getValue();
        Double[] staticTheta = theta.getValue();
        if (staticX == null || staticY == null) return new MutableLiveData<List<List<Entry>>>(){};
        if (staticPolynomial == null) return new MutableLiveData<List<List<Entry>>>(){};
        Double minX = staticX[0];
        Double maxX = staticX[0];
        List<Entry> scatterEntries = new ArrayList<>();
        for (int i = 0; i < staticX.length; i++) {
            if (staticX[i] < minX) {
                minX = staticX[i];
            } else if (staticX[i] > maxX) {
                maxX = staticX[i];
            }

            // also let's take this opportunity to build the scatter chart
            scatterEntries.add(new Entry(staticX[i].floatValue(), staticY[i].floatValue()));
        }

        // let's now generate the calculated h_theta (x) plot
        Primitive64Matrix.Factory matrixFactory = Primitive64Matrix.FACTORY;
        Primitive64Matrix thetaMatrixTransposed = matrixFactory.columns(staticTheta);
        Log.d("TEST", thetaMatrixTransposed.toString());
        double unit = (maxX - minX) / 20d;
        List<Entry> hypothesisEntries = new ArrayList<>();
        for (Double i = minX; i <= maxX; i = i + unit) {
            // h_theta (i)
            double[] arrXPolynomials = new double[staticPolynomial + 1];
            for (int j = 0; j <= staticPolynomial; j++) {
                arrXPolynomials[j] = Math.pow(i, j);
            }
            Primitive64Matrix xPolynomials = matrixFactory.rows(arrXPolynomials);
            hypothesisEntries.add(new Entry(i.floatValue(), xPolynomials.multiply(thetaMatrixTransposed).reduceRows(Aggregator.SUM).get(0, 0).floatValue()));
        }
        List<List<Entry>> returnValue = new ArrayList<>();
        returnValue.add(scatterEntries);
        returnValue.add(hypothesisEntries);
        return new MutableLiveData<>(returnValue);
    }

    /**
     * Normal Equation version.
     */
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
            // For convenience:
            Integer m = staticXs.length;

            // OjAlgo Matrix intermediate arrays have to be {{1, 2, 3}, {4,5,6}, {7, 8, 9}} format,
            // so loop through data to create that structure.
            Double[][] arrData = new Double[staticXs.length][staticPolynomial + 1];
            Double[][] arrOutput = new Double[staticXs.length][1];
            for(int i = 0; i < staticXs.length; i++) {
                arrData[i][0] = 1d; // Set first column to 1
                for(int p = 1; p <= staticPolynomial; p++) {
                    arrData[i][p] = Math.pow(staticXs[i], p); // Set subsequent columns to powers of X_1
                }
                arrOutput[i][0] = staticYs[i];
            }

            Primitive64Matrix.Factory matrixFactory = Primitive64Matrix.FACTORY;
            Primitive64Matrix designXMatrix = matrixFactory.rows(arrData);
            Primitive64Matrix outputYMatrix = matrixFactory.rows(arrOutput);

            // Use the normal equation formula to calculate theta
            Primitive64Matrix A = designXMatrix.transpose().multiply(designXMatrix);
            Primitive64Matrix B = A.invert();
            Primitive64Matrix C = B.multiply(designXMatrix.transpose());
            Primitive64Matrix ThetaMatrix = C.multiply(outputYMatrix);

            // Convert Primitive64Matrix to Double[] and put it in theta
            theta.postValue(DoubleStream.of(ThetaMatrix.toRawCopy1D()).boxed().toArray(Double[]::new));
        }
    }

    /**
     * Gradient Descent version.
     */
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
            // For convenience:
            int m = staticXs.length;

            // OjAlgo Matrix intermediate arrays have to be {{1, 2, 3}, {4,5,6}, {7, 8, 9}} format,
            // so loop through data to create that structure.
            Double[][] arrData = new Double[staticXs.length][staticPolynomial + 1];
            Double[][] arrOutput = new Double[staticXs.length][1];
            for(int i = 0; i < staticXs.length; i++) {
                arrData[i][0] = 1d; // Set first column to 1
                for(int p = 1; p <= staticPolynomial; p++) {
                    arrData[i][p] = Math.pow(staticXs[i], p); // Set subsequent columns to powers of X_1
                }
                arrOutput[i][0] = staticYs[i];
            }
            // Also restructure the theta array, which is needed for Gradient Descent
            Double[][] arrTheta = new Double[staticPolynomial + 1][1];
            for(int i = 0; i < staticPolynomial + 1; i++) {
                arrTheta[i][0] = 0d;
            }
            Primitive64Matrix.Factory matrixFactory = Primitive64Matrix.FACTORY;
            Primitive64Matrix designXMatrix = matrixFactory.rows(arrData);
            Primitive64Matrix outputYMatrix = matrixFactory.rows(arrOutput);
            Primitive64Matrix currentThetaMatrix = matrixFactory.rows(arrTheta);

            boolean repeat = true;
            // Repeat until convergence
            while(repeat) {
                // Retrieve the cost history to put the new value in
                ArrayList<Double> mutableCostHistory = costHistory.getValue();
                if (mutableCostHistory == null) {
                    mutableCostHistory = new ArrayList<>();
                }

                Primitive64Matrix A = designXMatrix.multiply(currentThetaMatrix).subtract(outputYMatrix); // A = m x 1
                Primitive64Matrix cost = designXMatrix.transpose().multiply(A).multiply(1.0 / m); // cost = n x 1
                // Mark the "cost" for this iteration as the Sum of the costs for each training example
                Double currentCost = Math.abs(cost.reduceColumns(Aggregator.SUM).get(0, 0));
                // If cost is too large, divergence is happening, so stop before history is added.
                if (currentCost.isNaN()) {
                    break;
                }
                mutableCostHistory.add(currentCost);
                // Set new cost history
                costHistory.postValue(mutableCostHistory);

                // Calculate percentage change using the last cost, or if it didn't exist, a big number
                Double lastCost = 999999d;
                if (mutableCostHistory.size() > 1) {
                    lastCost = mutableCostHistory.get(mutableCostHistory.size() - 2);
                }
                double costPercentageChange = 1 - (currentCost / lastCost);
                // Safe to say, if each iteration only changes by 0.00001% then it's pretty much converged
                if (Math.abs(costPercentageChange) < 0.0000001d) {
                    repeat = false;
                }

                // Update theta
                currentThetaMatrix = currentThetaMatrix.subtract(cost.multiply(staticLearningRate));
            }

            // Convert Primitive64Matrix to Double[] and put it in theta
            theta.postValue(DoubleStream.of(currentThetaMatrix.toRawCopy1D()).boxed().toArray(Double[]::new));
        }
    }

}
