package com.example.nicho.light_app_9;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.lang.StrictMath.log10;

public class MainActivity extends AppCompatActivity {

    private static Button mapButton;
    private LatLng sydney = new LatLng(-33.852, 151.2111);

    private LineGraphSeries<DataPoint> series;
    private static final Random RANDOM = new Random();
    private int lastX = 0;
    TextView textLIGHT_available, textLIGHT_reading, textLIGHT_absorbance;
    EditText lowerBound;
    String myLowerBoundString;
    EditText upperBound;
    String myUpperBoundString;
    int myLowerBoundInt;
    int myUpperBoundInt;
    double luxValue;

    double[] xArrayRun = new double[27610];
    double[] luxArrayRun = new double[27610];
    double[] xArrayCalibrate = new double[10];
    double[] luxArrayCalibrate = new double[10];

    double averageLuxArrayCalibrate;

    List<Double> xArrayList = new ArrayList<>();
    List<Double> yArrayList = new ArrayList<>();
    List<Double> absorbance = new ArrayList<>();

    int iArray = 0;

    double regressionB;
    double regressionA;
    String regressionEquation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);

        Viewport viewport = graph.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(28000);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(1000);
        viewport.setScrollable(false);

        lowerBound = (EditText) findViewById(R.id.plain_text_inputLower);
        upperBound = (EditText) findViewById(R.id.plain_text_inputUpper);


        GridLabelRenderer glr = graph.getGridLabelRenderer();
        glr.setPadding(64);

        textLIGHT_available
                = (TextView) findViewById(R.id.LIGHT_available);
        textLIGHT_reading
                = (TextView) findViewById(R.id.LIGHT_reading);
        textLIGHT_absorbance
                = (TextView) findViewById(R.id.LIGHT_absorbance);

        SensorManager mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Sensor LightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (LightSensor != null) {
            textLIGHT_available.setText("OK");
            mySensorManager.registerListener(
                    LightSensorListener,
                    LightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);

        } else {
            textLIGHT_available.setText("Sensor.TYPE_LIGHT NOT Available");
        }

        final Button runButton = findViewById(R.id.run_id);
        runButton.setOnClickListener(new View.OnClickListener() {


            public void onClick(View v) {
                new Thread(new Runnable() {

                    @Override

                    public void run() {
                        for (int i = 0; i < 27600; i++) {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    addEntryRun();
                                }
                            });

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {

                            }
                        }
                    }

                }).start();
            }
        });

        final Button calibrateButton = findViewById(R.id.calibrate_id);
        calibrateButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                new Thread(new Runnable() {

                    @Override

                    public void run() {
                        for (int i = 0; i < 10; i++) {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    addEntryCalibrate();
                                }
                            });

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {

                            }
                        }

                        double sum = 0;
                        for (int i = 0; i < luxArrayCalibrate.length; i++) {
                            sum = sum + luxArrayCalibrate[i];
                        }
                        averageLuxArrayCalibrate = sum / luxArrayCalibrate.length;
                        Log.d("average", String.valueOf(averageLuxArrayCalibrate));

                    }

                }).start();
            }

        });

        final Button analyzeButton = findViewById(R.id.analyze_id);
        analyzeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dataFilter();
                getAbsorbance();
                analyzeData();
            }
        });

        final Button b1 = findViewById(R.id.map_id);
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(myIntent);        }
        });
    }





    private final SensorEventListener LightSensorListener
            = new SensorEventListener(){

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_LIGHT){
                textLIGHT_reading.setText("LIGHT: " + event.values[0]);
                luxValue = event.values[0];
            }
        }
    };

    public void addEntryRun() {
        series.appendData(new DataPoint(lastX++,luxValue), false, 27600);
        xArrayRun[iArray]=lastX;
        luxArrayRun[iArray]=luxValue;
        Log.d("runTag", String.valueOf(luxArrayRun[iArray]));
        textLIGHT_absorbance.setText("A: " + (2-Math.log10(luxValue*100/averageLuxArrayCalibrate)));
        iArray++;
    }

    public void addEntryCalibrate() {
        series.appendData(new DataPoint(lastX++,luxValue), false, 10);
        xArrayCalibrate[iArray]=lastX;
        luxArrayCalibrate[iArray]=luxValue;
        Log.d("calibrateTag", String.valueOf(luxArrayCalibrate[iArray]));
        iArray++;
    }

    public void dataFilter() {

        myLowerBoundString = lowerBound.getText().toString();
        myLowerBoundInt = Integer.parseInt(myLowerBoundString);
        myUpperBoundString = upperBound.getText().toString();
        myUpperBoundInt = Integer.parseInt(myUpperBoundString);

        for (int i=myLowerBoundInt; i<myUpperBoundInt; i++){
            xArrayList.add(xArrayRun[i]);
            yArrayList.add(luxArrayRun[i]);
        }

    }

    public void analyzeData() {
        Double[] xArrayRunNew = new Double[xArrayList.size()];
        xArrayRunNew = xArrayList.toArray(xArrayRunNew);
        Double[] yArrayRunNew = new Double[yArrayList.size()];
        yArrayRunNew = yArrayList.toArray(yArrayRunNew);

        double sumX=0;
        double sumY=0;
        double sumXX=0;
        double sumXY=0;
        int n=0;

        for (int i=0; i<xArrayRunNew.length; i++){
            sumX=sumX+i;
            sumY=sumY+Math.log(absorbance.get(i));
            sumXX=sumXX+i*i;
            sumXY=sumXY+(Math.log(absorbance.get(i)))*i;
            n++;
        }

        regressionB = ((n)*(sumXY)-(sumX)*(sumY))/((n)*(sumXX)-((sumX)*(sumX)));
        regressionA = (sumY-regressionB*sumX)/n;
        regressionEquation = String.valueOf(regressionA)+"+"+String.valueOf(regressionB)+"x";
        Log.d("equation", regressionEquation);
    }

    public void getAbsorbance(){
        for (int i=myLowerBoundInt; i<myUpperBoundInt; i++){
            absorbance.add(2-log10(100*luxArrayRun[i]/averageLuxArrayCalibrate));
            Log.d("absorbance", Arrays.toString(absorbance.toArray()));
        }

    }


    @Override
    protected void onResume() {
        super.onResume();


    }

}