package com.daliammao.stepper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.twl.stepperlibrary.StepperTabViewPage;
import com.twl.stepperlibrary.VerificationError;
import com.twl.stepperlibrary.internal.widget.TabsContainer;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements StepperTabViewPage.StepperListener {
    private static final String CURRENT_STEP_POSITION_KEY = "position";

    private static final List<CharSequence> EDIT_MODE_STEP_TITLES = Arrays.<CharSequence>asList("Step 1", "Step 2");
    private TabsContainer tabs;
    private Button proceed;
    private Button onPrevious;
    private StepperTabViewPage mStepperLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Stepper sample");

        setContentView(R.layout.content_main);

        initView();

        int startingStepPosition = savedInstanceState != null ? savedInstanceState.getInt(CURRENT_STEP_POSITION_KEY) : 0;
        mStepperLayout.setAdapter(new SampleFragmentStepAdapter(getSupportFragmentManager(), this), startingStepPosition);
        mStepperLayout.setListener(this);

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStepperLayout.proceed();
            }
        });

        onPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStepperLayout.onPrevious();
            }
        });

        tabs.setSteps(EDIT_MODE_STEP_TITLES);
        tabs.updateSteps(1);
    }

    private void initView() {
        tabs = (TabsContainer) findViewById(R.id.tabs);
        proceed = (Button) findViewById(R.id.proceed);
        onPrevious = (Button) findViewById(R.id.onPrevious);
        mStepperLayout = (StepperTabViewPage) findViewById(R.id.stepperLayout);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_STEP_POSITION_KEY, mStepperLayout.getCurrentStepPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        final int currentStepPosition = mStepperLayout.getCurrentStepPosition();
        if (currentStepPosition > 0) {
            mStepperLayout.setCurrentStepPosition(currentStepPosition - 1);
        } else {
            finish();
        }
    }

    @Override
    public void onCompleted() {
        Toast.makeText(this, "onCompleted!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(VerificationError verificationError) {
        Toast.makeText(this, "onError! -> " + verificationError.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStepSelected(int newStepPosition) {
        Toast.makeText(this, "onStepSelected! -> " + newStepPosition, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReturn() {
        finish();
    }

}
