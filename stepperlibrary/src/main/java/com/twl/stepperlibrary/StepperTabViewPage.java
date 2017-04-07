/*
Copyright 2016 StepStone Services

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.twl.stepperlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.view.ContextThemeWrapper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.twl.stepperlibrary.adapter.StepAdapter;
import com.twl.stepperlibrary.internal.type.AbstractStepperType;
import com.twl.stepperlibrary.internal.type.StepperTypeFactory;
import com.twl.stepperlibrary.internal.widget.TabsContainer;

/**
 * Stepper widget implemented according to the <a href="https://www.google.com/design/spec/components/steppers.html">Material documentation</a>.<br>
 * It allows for setting three types of steppers:<br>
 * - mobile dots stepper,<br>
 * - mobile progress bar stepper,<br>
 * - horizontal stepper with tabs.<br>
 * Include this stepper in the layout XML file and choose a stepper type with <code>ms_stepperType</code>.<br>
 * Check out <code>values/attrs.xml - StepperLayout</code> for a complete list of customisable properties.
 */
public class StepperTabViewPage extends LinearLayout implements TabsContainer.TabItemListener {

    /**
     * A listener for events of {@link StepperTabViewPage}.
     */
    public interface StepperListener {

        /**
         * Called when all of the steps were completed successfully
         */
        void onCompleted();

        /**
         * Called when a verification error occurs for one of the steps
         *
         * @param verificationError verification error
         */
        void onError(VerificationError verificationError);

        /**
         * Called when the current step position changes
         *
         * @param newStepPosition new step position
         */
        void onStepSelected(int newStepPosition);

        /**
         * Called when the Previous step button was pressed while on the first step
         * (the button is not present by default on first step).
         */
        void onReturn();

        StepperListener NULL = new StepperListener() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(VerificationError verificationError) {
            }

            @Override
            public void onStepSelected(int newStepPosition) {
            }

            @Override
            public void onReturn() {
            }
        };
    }

    public abstract class AbstractOnButtonClickedCallback {

        public StepperTabViewPage getStepperLayout() {
            return StepperTabViewPage.this;
        }

    }

    public class OnNextClickedCallback extends AbstractOnButtonClickedCallback {

        @UiThread
        public void goToNextStep() {
            final int totalStepCount = mStepAdapter.getCount();

            if (mCurrentStepPosition >= totalStepCount - 1) {
                return;
            }

            mCurrentStepPosition++;
            onUpdate(mCurrentStepPosition, true);
        }

    }

    public class OnCompleteClickedCallback extends AbstractOnButtonClickedCallback {

        @UiThread
        public void complete() {
            invalidateCurrentPosition();
            mListener.onCompleted();
        }

    }

    public class OnBackClickedCallback extends AbstractOnButtonClickedCallback {

        @UiThread
        public void goToPrevStep() {
            if (mCurrentStepPosition <= 0) {
                mListener.onReturn();
                return;
            }
            mCurrentStepPosition--;
            onUpdate(mCurrentStepPosition, true);
        }

    }

    private ViewPager mPager;

    private TabsContainer mTabsContainer;

    @ColorInt
    private int mUnselectedColor;

    @ColorInt
    private int mSelectedColor;

    @ColorInt
    private int mUnselectedNumColor;

    @ColorInt
    private int mSelectedNumColor;

    @ColorInt
    private int mErrorColor;

    private int mTabStepDividerWidth = Constant.DEFAULT_TAB_DIVIDER_WIDTH;

    private int mTypeIdentifier = AbstractStepperType.TABS;

    private StepAdapter mStepAdapter;

    private AbstractStepperType mStepperType;

    private int mCurrentStepPosition;

    private boolean mShowErrorStateEnabled;

    private boolean mShowErrorStateOnBackEnabled;

    private boolean mTabNavigationEnabled;

    @StyleRes
    private int mStepperLayoutTheme;

    @NonNull
    private StepperListener mListener = StepperListener.NULL;

    public StepperTabViewPage(Context context) {
        this(context, null);
    }

    public StepperTabViewPage(Context context, AttributeSet attrs) {
        super(context, attrs);
        //Fix for issue #60 with AS Preview editor
        init(attrs, isInEditMode() ? 0 : R.attr.ms_stepperStyle);
    }

    public StepperTabViewPage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    public void setListener(@NonNull StepperListener listener) {
        this.mListener = listener;
    }

    /**
     * Getter for mStepAdapter
     *
     * @return mStepAdapter
     */
    public StepAdapter getAdapter() {
        return mStepAdapter;
    }

    /**
     * Sets the new step adapter and updates the stepper layout based on the new adapter.
     *
     * @param stepAdapter step adapter
     */
    public void setAdapter(@NonNull StepAdapter stepAdapter) {
        this.mStepAdapter = stepAdapter;

        mPager.setAdapter(stepAdapter.getPagerAdapter());

        mStepperType.onNewAdapter(stepAdapter);

        // this is so that the fragments in the adapter can be created BEFORE the onUpdate() method call
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                onUpdate(mCurrentStepPosition, false);
            }
        });

    }

    /**
     * Sets the new step adapter and updates the stepper layout based on the new adapter.
     *
     * @param stepAdapter         step adapter
     * @param currentStepPosition the initial step position, must be in the range of the adapter item count
     */
    public void setAdapter(@NonNull StepAdapter stepAdapter, @IntRange(from = 0) int currentStepPosition) {
        this.mCurrentStepPosition = currentStepPosition;
        setAdapter(stepAdapter);
    }

    /**
     * Overrides the default page transformer used in the underlying {@link ViewPager}
     *
     * @param pageTransformer new page transformer
     */
    public void setPageTransformer(@Nullable ViewPager.PageTransformer pageTransformer) {
        mPager.setPageTransformer(false, pageTransformer);
    }

    public int getSelectedColor() {
        return mSelectedColor;
    }

    public int getUnselectedColor() {
        return mUnselectedColor;
    }

    public int getUnselectedNumColor() {
        return mUnselectedNumColor;
    }

    public int getSelectedNumColor() {
        return mSelectedNumColor;
    }

    public int getErrorColor() {
        return mErrorColor;
    }

    public int getTabStepDividerWidth() {
        return mTabStepDividerWidth;
    }

    @Override
    @UiThread
    public void onTabClicked(int position) {
        if (mTabNavigationEnabled) {
            if (position > mCurrentStepPosition) {
                onNext();
            } else if (position < mCurrentStepPosition) {
                setCurrentStepPosition(position);
            }
        }
    }

    /**
     * Sets the current step to the one at the provided index.
     * This does not verify the current step.
     *
     * @param currentStepPosition new current step position
     */
    public void setCurrentStepPosition(int currentStepPosition) {
        int previousStepPosition = mCurrentStepPosition;
        if (currentStepPosition < previousStepPosition) {
            updateErrorFlagWhenGoingBack();
        }
        mCurrentStepPosition = currentStepPosition;

        onUpdate(currentStepPosition, true);
    }

    public int getCurrentStepPosition() {
        return mCurrentStepPosition;
    }

    /**
     * Set whether when going backwards should clear the error state from the Tab. Default is <code>false</code>.
     *
     * @param showErrorStateOnBack true if navigating backwards should keep the error state, false otherwise
     * @deprecated see {@link #setShowErrorStateOnBackEnabled(boolean)}
     */
    @Deprecated
    public void setShowErrorStateOnBack(boolean showErrorStateOnBack) {
        this.mShowErrorStateOnBackEnabled = showErrorStateOnBack;
    }

    /**
     * Set whether when going backwards should clear the error state from the Tab. Default is <code>false</code>.
     *
     * @param showErrorStateOnBackEnabled true if navigating backwards should keep the error state, false otherwise
     */
    public void setShowErrorStateOnBackEnabled(boolean showErrorStateOnBackEnabled) {
        this.mShowErrorStateOnBackEnabled = showErrorStateOnBackEnabled;
    }

    /**
     * Set whether the errors should be displayed when they occur or not. Default is <code>false</code>.
     *
     * @param showErrorState true if the errors should be displayed when they occur, false otherwise
     * @deprecated see {@link #setShowErrorStateEnabled(boolean)}
     */
    @Deprecated
    public void setShowErrorState(boolean showErrorState) {
        setShowErrorStateEnabled(showErrorState);
    }

    /**
     * Set whether the errors should be displayed when they occur or not. Default is <code>false</code>.
     *
     * @param showErrorStateEnabled true if the errors should be displayed when they occur, false otherwise
     */
    public void setShowErrorStateEnabled(boolean showErrorStateEnabled) {
        this.mShowErrorStateEnabled = showErrorStateEnabled;
    }

    /**
     * @return true if errors should be displayed when they occur
     */
    public boolean isShowErrorStateEnabled() {
        return mShowErrorStateEnabled;
    }

    /**
     * @return true if when going backwards the error state from the Tab should be cleared
     */
    public boolean isShowErrorStateOnBackEnabled() {
        return mShowErrorStateOnBackEnabled;
    }

    /**
     * @return true if step navigation is possible by clicking on the tabs directly, false otherwise
     */
    public boolean isTabNavigationEnabled() {
        return mTabNavigationEnabled;
    }

    /**
     * Sets whether step navigation is possible by clicking on the tabs directly. Only applicable for 'tabs' type.
     *
     * @param tabNavigationEnabled true if step navigation is possible by clicking on the tabs directly, false otherwise
     */
    public void setTabNavigationEnabled(boolean tabNavigationEnabled) {
        mTabNavigationEnabled = tabNavigationEnabled;
    }

    /**
     * Updates the error state in the UI.
     * It does nothing if showing error state is disabled.
     * This is used internally to show the error on tabs.
     *
     * @param hasError true if error should be shown, false otherwise
     * @see #setShowErrorStateEnabled(boolean)
     */
    public void updateErrorState(boolean hasError) {
        updateErrorFlag(hasError);
        if (mShowErrorStateEnabled) {
            invalidateCurrentPosition();
        }
    }

    /**
     * Set the number of steps that should be retained to either side of the
     * current step in the view hierarchy in an idle state. Steps beyond this
     * limit will be recreated from the adapter when needed.
     *
     * @param limit How many steps will be kept offscreen in an idle state.
     * @see ViewPager#setOffscreenPageLimit(int)
     */
    public void setOffscreenPageLimit(int limit) {
        mPager.setOffscreenPageLimit(limit);
    }

    public void updateErrorFlag(boolean hasError) {
        mStepperType.setErrorFlag(mCurrentStepPosition, hasError);
    }

    @SuppressWarnings("RestrictedApi")
    private void init(AttributeSet attrs, @AttrRes int defStyleAttr) {
        initDefaultValues();
        extractValuesFromAttributes(attrs, defStyleAttr);

        final Context context = getContext();

        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, context.getTheme());
        contextThemeWrapper.setTheme(mStepperLayoutTheme);

        LayoutInflater.from(contextThemeWrapper).inflate(R.layout.ms_stepper_layout, this, true);
        bindViews();

        mPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        mTabsContainer.setVisibility(GONE);

        mStepperType = StepperTypeFactory.createType(mTypeIdentifier, this);
    }

    private void bindViews() {
        mPager = (ViewPager) findViewById(R.id.ms_stepPager);

        mTabsContainer = (TabsContainer) findViewById(R.id.ms_stepTabsContainer);
    }

    private void extractValuesFromAttributes(AttributeSet attrs, @AttrRes int defStyleAttr) {
        if (attrs != null) {
            final TypedArray a = getContext().obtainStyledAttributes(
                    attrs, R.styleable.StepperTabViewPage, defStyleAttr, 0);

            if (a.hasValue(R.styleable.StepperTabViewPage_ms_activeStepColor)) {
                mSelectedColor = a.getColor(R.styleable.StepperTabViewPage_ms_activeStepColor, mSelectedColor);
            }
            if (a.hasValue(R.styleable.StepperTabViewPage_ms_inactiveStepColor)) {
                mUnselectedColor = a.getColor(R.styleable.StepperTabViewPage_ms_inactiveStepColor, mUnselectedColor);
            }
            if (a.hasValue(R.styleable.StepperTabViewPage_ms_activeStepNumColor)) {
                mSelectedNumColor = a.getColor(R.styleable.StepperTabViewPage_ms_activeStepNumColor, mSelectedNumColor);
            }
            if (a.hasValue(R.styleable.StepperTabViewPage_ms_inactiveStepNumColor)) {
                mUnselectedNumColor = a.getColor(R.styleable.StepperTabViewPage_ms_inactiveStepNumColor, mUnselectedNumColor);
            }
            if (a.hasValue(R.styleable.StepperTabViewPage_ms_errorColor)) {
                mErrorColor = a.getColor(R.styleable.StepperTabViewPage_ms_errorColor, mErrorColor);
            }

            if (a.hasValue(R.styleable.StepperTabViewPage_ms_tabStepDividerWidth)) {
                mTabStepDividerWidth = a.getDimensionPixelOffset(R.styleable.StepperTabViewPage_ms_tabStepDividerWidth, -1);
            }

            mShowErrorStateEnabled = a.getBoolean(R.styleable.StepperTabViewPage_ms_showErrorState, false);
            mShowErrorStateEnabled = a.getBoolean(R.styleable.StepperTabViewPage_ms_showErrorStateEnabled, mShowErrorStateEnabled);

            if (a.hasValue(R.styleable.StepperTabViewPage_ms_stepperType)) {
                mTypeIdentifier = a.getInt(R.styleable.StepperTabViewPage_ms_stepperType, Constant.DEFAULT_TAB_DIVIDER_WIDTH);
            }

            mShowErrorStateOnBackEnabled = a.getBoolean(R.styleable.StepperTabViewPage_ms_showErrorStateOnBack, false);
            mShowErrorStateOnBackEnabled = a.getBoolean(R.styleable.StepperTabViewPage_ms_showErrorStateOnBackEnabled, mShowErrorStateOnBackEnabled);

            mTabNavigationEnabled = a.getBoolean(R.styleable.StepperTabViewPage_ms_tabNavigationEnabled, true);

            mStepperLayoutTheme = a.getResourceId(R.styleable.StepperTabViewPage_ms_stepperViewPageTheme, R.style.MSDefaultStepperLayoutTheme);

            a.recycle();
        }
    }

    private void initDefaultValues() {
        ContextCompat.getColorStateList(getContext(), R.color.ms_bottomNavigationButtonTextColor);
        mSelectedColor = ContextCompat.getColor(getContext(), R.color.ms_selectedColor);
        mUnselectedColor = ContextCompat.getColor(getContext(), R.color.ms_unselectedColor);
        mSelectedNumColor = ContextCompat.getColor(getContext(), R.color.ms_selectedNumColor);
        mUnselectedNumColor = ContextCompat.getColor(getContext(), R.color.ms_unselectedNumColor);
        mErrorColor = ContextCompat.getColor(getContext(), R.color.ms_errorColor);
    }

    private boolean isLastPosition(int position) {
        return position == mStepAdapter.getCount() - 1;
    }

    private Step findCurrentStep() {
        return mStepAdapter.findStep(mCurrentStepPosition);
    }

    /**
     * This is an equivalent of clicking the Next/Complete button from the bottom navigation.
     * Unlike {@link #setCurrentStepPosition(int)} this actually verifies the step.
     */
    public void proceed() {
        if (isLastPosition(mCurrentStepPosition)) {
            onComplete();
        } else {
            onNext();
        }
    }

    public void onPrevious() {
        Step step = findCurrentStep();

        updateErrorFlagWhenGoingBack();

        OnBackClickedCallback onBackClickedCallback = new OnBackClickedCallback();
        if (step instanceof BlockingStep) {
            ((BlockingStep) step).onBackClicked(onBackClickedCallback);
        } else {
            onBackClickedCallback.goToPrevStep();
        }
    }

    @UiThread
    public void onNext() {
        Step step = findCurrentStep();

        if (verifyCurrentStep(step)) {
            invalidateCurrentPosition();
            return;
        }

        OnNextClickedCallback onNextClickedCallback = new OnNextClickedCallback();
        if (step instanceof BlockingStep) {
            ((BlockingStep) step).onNextClicked(onNextClickedCallback);
        } else {
            onNextClickedCallback.goToNextStep();
        }
    }

    public void onComplete() {
        Step step = findCurrentStep();
        if (verifyCurrentStep(step)) {
            invalidateCurrentPosition();
            return;
        }

        OnCompleteClickedCallback onCompleteClickedCallback = new OnCompleteClickedCallback();
        if (step instanceof BlockingStep) {
            ((BlockingStep) step).onCompleteClicked(onCompleteClickedCallback);
        } else {
            onCompleteClickedCallback.complete();
        }
    }

    private void updateErrorFlagWhenGoingBack() {
        updateErrorFlag(mShowErrorStateOnBackEnabled && mStepperType.getErrorAtPosition(mCurrentStepPosition));
    }

    private void invalidateCurrentPosition() {
        mStepperType.onStepSelected(mCurrentStepPosition, false);
    }

    private boolean verifyCurrentStep(Step step) {
        final VerificationError verificationError = step.verifyStep();
        boolean result = false;
        if (verificationError != null) {
            onError(verificationError);
            result = true;
        }

        updateErrorFlag(result);
        return result;
    }

    private void onError(@NonNull VerificationError verificationError) {
        Step step = findCurrentStep();
        if (step != null) {
            step.onError(verificationError);
        }
        mListener.onError(verificationError);
    }

    private void onUpdate(int newStepPosition, boolean userTriggeredChange) {
        mPager.setCurrentItem(newStepPosition);

        mStepperType.onStepSelected(newStepPosition, userTriggeredChange);
        mListener.onStepSelected(newStepPosition);
        Step step = mStepAdapter.findStep(newStepPosition);
        if (step != null) {
            step.onSelected();
        }
    }
}
