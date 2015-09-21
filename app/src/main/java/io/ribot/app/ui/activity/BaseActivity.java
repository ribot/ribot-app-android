package io.ribot.app.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.ribot.app.RibotApplication;
import io.ribot.app.injection.component.ApplicationComponent;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected ApplicationComponent applicationComponent() {
        return RibotApplication.get(this).getComponent();
    }

}
