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
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_graph, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearRegressionViewModel model = new ViewModelProvider(requireActivity()).get(LinearRegressionViewModel.class);
        TextView headerView = view.findViewById(R.id.result_header);
        LineChart costChart = (LineChart) view.findViewById(R.id.linechart_cost);
        model.beginCalculation();
        model.getTheta().observe(getViewLifecycleOwner(), item -> headerView.setText(getString(R.string.theta_result, Arrays.toString(item))));
        model.getCostHistory().observe(getViewLifecycleOwner(), item -> {
            List<Double> copy = new ArrayList<>(item);
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < copy.size(); i++) {
                entries.add(new Entry(i, copy.get(i).floatValue()));
            }
            LineDataSet dataSet = new LineDataSet(entries, "Cost over iteration");
            LineData lineData = new LineData(dataSet);
            costChart.setData(lineData);
            costChart.invalidate();
        });
    }
}