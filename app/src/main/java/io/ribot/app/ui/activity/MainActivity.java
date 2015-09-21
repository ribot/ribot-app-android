package io.ribot.app.ui.activity;

import android.os.Bundle;

import butterknife.ButterKnife;
import io.ribot.app.R;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applicationComponent().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

}
