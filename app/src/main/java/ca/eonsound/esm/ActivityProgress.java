package ca.eonsound.esm;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;

public class ActivityProgress extends AppCompatActivity {
    private LineGraphSeries<DataPoint> seriesProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        GraphView graphProgress = findViewById(R.id.graphProgress);

        Viewport viewport = graphProgress.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setScrollable(true);
        viewport.setScalable(true);
        viewport.setScrollableY(false);
        viewport.setScalableY(false);
        viewport.setMaxXAxisSize(100);

        ArrayList<CScore> aScore = new ArrayList<>(); //Settings.getInstance().getScore();

        // simulate data
        NumberFormat fmt = NumberFormat.getInstance();
        fmt.setMaximumFractionDigits(1);
        Random rand = new Random();
        for (int i=0; i<30; i++) {
            CScore score = new CScore();
            score.strScore = fmt.format(i * 3 + 50 + rand.nextFloat() * 8 - 4);
            aScore.add(score);
        }
        for (int i=0; i<aScore.size(); i++) {
            DataPoint data = new DataPoint(i, Float.parseFloat(aScore.get(i).strScore));
            seriesProgress.appendData(data, true, 100, false);
        }
        graphProgress.addSeries(seriesProgress);
    }
}