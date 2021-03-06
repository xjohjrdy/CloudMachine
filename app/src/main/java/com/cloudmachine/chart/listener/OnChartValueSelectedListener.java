package com.cloudmachine.chart.listener;

import android.view.MotionEvent;
import android.widget.SeekBar;

import com.cloudmachine.chart.data.Entry;
import com.cloudmachine.chart.highlight.Highlight;

/**
 * Listener for callbacks when selecting values inside the chart by
 * touch-gesture.
 * 
 * @author Philipp Jahoda
 */
public interface OnChartValueSelectedListener {

    /**
     * Called when a value has been selected inside the chart.
     * 
     * @param e The selected Entry.
     * @param dataSetIndex The index in the datasets array of the data object
     *            the Entrys DataSet is in.
     * @param h the corresponding highlight object that contains information
     *            about the highlighted position
     */
    void onValueSelected(Entry e, int dataSetIndex, Highlight h,MotionEvent event);

    /**
     * Called when nothing has been selected or an "un-select" has been made.
     */
    void onNothingSelected();

    void onProgressChanged(SeekBar seekBar, int progress,
                           boolean fromUser);

    void onStartTrackingTouch(SeekBar seekBar);

    void onStopTrackingTouch(SeekBar seekBar);
}
