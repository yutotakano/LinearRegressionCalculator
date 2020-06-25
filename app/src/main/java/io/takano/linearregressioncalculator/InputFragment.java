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

    private LinearRegressionViewModel model;

    private EditText xsInputView, ysInputView;
    private EditText learningRateInputView, polynomialInputView;
    private RadioButton gradientDescentRadio;

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

        xsInputView = view.findViewById(R.id.inputXs);
        ysInputView = view.findViewById(R.id.inputYs);
        learningRateInputView = view.findViewById(R.id.inputLearningRate);
        polynomialInputView = view.findViewById(R.id.inputPolynomial);
        gradientDescentRadio = view.findViewById(R.id.radioTypeGradientDescent);

        model = new ViewModelProvider(requireActivity()).get(LinearRegressionViewModel.class);
        view.findViewById(R.id.begin_calculation_button).setOnClickListener(view1 -> {

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
            Double learningRate;
            try {
                learningRate = Double.valueOf(learningRateInputView.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            Integer polynomial;
            try {
                polynomial = Integer.valueOf(polynomialInputView.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String calculationType = gradientDescentRadio.isChecked() ? "gd" : "ne";


            model.initialize(currentXs, currentYs, learningRate, polynomial, calculationType);
            NavHostFragment.findNavController(InputFragment.this).navigate(InputFragmentDirections.actionPassInputToGraph());
        });

    }

    private Double[] sanitizeTrainingDataX(String[] xStrings) throws InvalidInputLengthException {
        Double[] checkedX;
        checkedX = Arrays.stream(xStrings).mapToDouble(Double::valueOf).boxed().toArray(Double[]::new);
        if (checkedX.length <= 1) {
            throw new InvalidInputLengthException();
        }
        return checkedX;
    }

    private Double[] sanitizeTrainingDataY(String[] yStrings) throws InvalidInputLengthException {
        Double[] checkedY;
        checkedY = Arrays.stream(yStrings).mapToDouble(Double::valueOf).boxed().toArray(Double[]::new);
        if (checkedY.length <= 1) {
            throw new InvalidInputLengthException();
        }
        return checkedY;
    }
}