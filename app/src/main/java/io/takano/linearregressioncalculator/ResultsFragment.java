package io.takano.linearregressioncalculator;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResultsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(ResultsFragment.this).navigate(ResultsFragmentDirections.actionBackToStart());
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_results, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CombinedChart resultChart = (CombinedChart) view.findViewById(R.id.resultChart);

        // Load the same model as that was populated in InputFragment
        LinearRegressionViewModel model = new ViewModelProvider(requireActivity()).get(LinearRegressionViewModel.class);

        model.generateChartEntries().observe(getViewLifecycleOwner(), item -> {
            ScatterDataSet trainingDataSet = new ScatterDataSet(item.get(0), "Training Data");
            trainingDataSet.setScatterShapeSize(7.5f);
            trainingDataSet.setDrawValues(false);
            LineDataSet hypothesisDataSet = new LineDataSet(item.get(1), "Hypothesis");
            hypothesisDataSet.setColor(Color.rgb(240, 238, 70));
            hypothesisDataSet.setLineWidth(2.5f);
            ScatterData trainingData = new ScatterData(trainingDataSet);
            LineData hypothesisData = new LineData(hypothesisDataSet);
            CombinedData data = new CombinedData();
            data.setData(trainingData);
            data.setData(hypothesisData);
            resultChart.setData(data);
            resultChart.invalidate();
        });

    }

}