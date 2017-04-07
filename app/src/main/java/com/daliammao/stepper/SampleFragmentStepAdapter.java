package com.daliammao.stepper;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.twl.stepperlibrary.Step;
import com.twl.stepperlibrary.adapter.AbstractFragmentStepAdapter;
import com.twl.stepperlibrary.viewmodel.StepViewModel;

public class SampleFragmentStepAdapter extends AbstractFragmentStepAdapter {

    public SampleFragmentStepAdapter(@NonNull FragmentManager fm, @NonNull Context context) {
        super(fm, context);
    }

    @NonNull
    @Override
    public StepViewModel getViewModel(@IntRange(from = 0) int position) {
        return new StepViewModel.Builder(context)
                .setTitle("step " + position)
                .create();
    }

    @Override
    public Step createStep(int position) {
        switch (position) {
            case 0:
                return StepFragmentSample.newInstance(R.layout.fragment_step);
            case 1:
                return StepFragmentSample.newInstance(R.layout.fragment_step2);
            case 2:
                return StepFragmentSample.newInstance(R.layout.fragment_step3);
            default:
                throw new IllegalArgumentException("Unsupported position: " + position);
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

}