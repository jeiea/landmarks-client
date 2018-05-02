package kr.ac.kw.coms.globealbum;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import org.osmdroid.config.Configuration;

import kr.ac.kw.coms.globealbum.Map.MyMapView;


public class MapActivity extends AppCompatActivity {
    Context context = null;
    MyMapView myMapView= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //osmdroid 초기 구성
        context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        myMapView = (MyMapView)findViewById(R.id.map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myMapView != null)
            myMapView.onResume(); //osmdroid configuration refresh
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myMapView != null)
            myMapView.onPause(); //osmdroid configuration refresh
    }

}
