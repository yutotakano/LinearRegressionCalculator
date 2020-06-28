package io.takano.linearregressioncalculator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class GraphFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_graph, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LineChart costChart = (LineChart) view.findViewById(R.id.linechart_cost);

        // Load the same model as that was populated in InputFragment
        LinearRegressionViewModel model = new ViewModelProvider(requireActivity()).get(LinearRegressionViewModel.class);

        // Begin calculation in the background thread using the specified input parameters and calculation type
        model.beginCalculation();

        // When theta is found (no longer null), set the text in headerView accordingly
//        model.getTheta().observe(getViewLifecycleOwner(), item -> headerView.setText(getString(R.string.theta_result, Arrays.toString(item))));

        // Update the graph of cost function J() over iteration
        model.getCostHistory().observe(getViewLifecycleOwner(), item -> {
            // Make a copy of ArrayList item because otherwise we'd edit the original
            List<Double> copy = new ArrayList<>(item);
            // Add the float version of each cost, together with the index
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < copy.size(); i++) {
                entries.add(new Entry(i, copy.get(i).floatValue()));
            }
            LineDataSet dataSet = new LineDataSet(entries, "Cost over iteration");
            LineData lineData = new LineData(dataSet);
            costChart.setData(lineData);
            // Invalidate the existing chart, i.e. refresh the chart
            costChart.invalidate();
        });
    }
}