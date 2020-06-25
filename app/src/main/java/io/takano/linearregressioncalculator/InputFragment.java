package io.takano.linearregressioncalculator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

public class InputFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_input, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Store the views in a private variable so we don't call the expensive findViewById() often
        EditText xsInputView = view.findViewById(R.id.inputXs);
        EditText ysInputView = view.findViewById(R.id.inputYs);
        EditText learningRateInputView = view.findViewById(R.id.inputLearningRate);
        EditText polynomialInputView = view.findViewById(R.id.inputPolynomial);
        RadioButton gradientDescentRadio = view.findViewById(R.id.radioTypeGradientDescent);

        // Initialise a ViewModel in the current Activity
        LinearRegressionViewModel model = new ViewModelProvider(requireActivity()).get(LinearRegressionViewModel.class);

        // Set up a click listener for the Begin Calculation button
        view.findViewById(R.id.begin_calculation_button).setOnClickListener(view1 -> {

            // Make sure that input X and inputY are numbers, and m > 1
            Double[] currentXs, currentYs;
            try {
                currentXs = sanitizeTrainingDataX(xsInputView.getText().toString().split("\n"));
                currentYs = sanitizeTrainingDataY(ysInputView.getText().toString().split("\n"));
            } catch (NumberFormatException e) {
                // some non-numbers there
                return;
            } catch (InvalidInputLengthException e) {
                // not enough length
                return;
            }

            // Make sure learning rate is a Double
            Double learningRate;
            try {
                learningRate = Double.valueOf(learningRateInputView.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }

            // Make sure polynomial is an Integer
            Integer polynomial;
            try {
                polynomial = Integer.valueOf(polynomialInputView.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }

            // Use gradient descent or normal equation?
            String calculationType = gradientDescentRadio.isChecked() ? "gd" : "ne";

            // Store all the contents in the model so that:
            // 1. it is not altered anymore unless the button is clicked again
            // 2. it is accessible from other fragments including GraphFragment
            model.initialize(currentXs, currentYs, learningRate, polynomial, calculationType);

            // Navigate to GraphFragment
            NavHostFragment.findNavController(InputFragment.this).navigate(InputFragmentDirections.actionPassInputToGraph());
        });

    }

    /**
     * Sanitises the training data X by checking for length (m > 1) and value (is Double?)
     * @param xStrings Array of strings, each containing what is supposedly a number
     * @return Sanitised X array
     * @throws InvalidInputLengthException on m <= 1
     */
    private Double[] sanitizeTrainingDataX(String[] xStrings) throws InvalidInputLengthException {
        Double[] checkedX;
        checkedX = Arrays.stream(xStrings).mapToDouble(Double::valueOf).boxed().toArray(Double[]::new);
        if (checkedX.length <= 1) {
            throw new InvalidInputLengthException();
        }
        return checkedX;
    }

    /**
     * Sanitises the training data Y by checking for length (m > 1) and value (is Double?)
     * @param yStrings Array of strings, each containing what is supposedly a number
     * @return Sanitised Y array
     * @throws InvalidInputLengthException on m <= 1
     */
    private Double[] sanitizeTrainingDataY(String[] yStrings) throws InvalidInputLengthException {
        Double[] checkedY;
        checkedY = Arrays.stream(yStrings).mapToDouble(Double::valueOf).boxed().toArray(Double[]::new);
        if (checkedY.length <= 1) {
            throw new InvalidInputLengthException();
        }
        return checkedY;
    }
}