package com.twl.stepperlibrary.adapter;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.twl.stepperlibrary.Step;
import com.twl.stepperlibrary.viewmodel.StepViewModel;

/**
 */
public interface StepAdapter {

    /**
     * @param position The position of the {@link PagerAdapter} to be used inside the {@link ViewPager}.
     */
    Step createStep(@IntRange(from = 0) int position);

    /**
     * Finds the given step without creating it.
     * @see FragmentPagerAdapter#makeFragmentName(int, long)
     * @param position step position
     * @return step fragment
     */
    Step findStep(@IntRange(from = 0) int position);

    /**
     * Returns the view information to be used to display the step.
     * @param position step position
     * @return view information
     */
    @NonNull
    StepViewModel getViewModel(@IntRange(from = 0) int position);

    /**
     * Get the step count.
     * @return the quantity of steps
     */
    @IntRange(from = 0)
    int getCount();

    /**
     * Method for internal purpose. Should not be inherited.
     * @return the adapter to be used in the {@link ViewPager}.
     */
    PagerAdapter getPagerAdapter();
}
