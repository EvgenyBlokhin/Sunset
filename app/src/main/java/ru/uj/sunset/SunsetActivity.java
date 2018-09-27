package ru.uj.sunset;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class SunsetActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return ;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sunset);
    }
}
