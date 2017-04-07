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

package com.twl.stepperlibrary.internal.type;

import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.util.SparseBooleanArray;

import com.twl.stepperlibrary.StepperTabViewPage;
import com.twl.stepperlibrary.adapter.StepAdapter;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

/**
 * A base stepper type all stepper types must extend.
 */
@RestrictTo(LIBRARY)
public abstract class AbstractStepperType {

    /**
     * Mobile step dots
     */
    public static final int DOTS = 0x01;

    /**
     * Mobile step progress bar
     */
    public static final int PROGRESS_BAR = 0x02;

    /**
     * Horizontal stepper with tabs
     */
    public static final int TABS = 0x03;

    final StepperTabViewPage stepperTabViewPage;

    final SparseBooleanArray mStepErrors = new SparseBooleanArray();

    public AbstractStepperType(StepperTabViewPage stepperTabViewPage) {
        this.stepperTabViewPage = stepperTabViewPage;
    }

    /**
     * Called when a step gets selected as the new current step.
     * @param newStepPosition new current step position
     * @param userTriggeredChange <code>true</code> if current step position changed as a direct result of user interaction
     */
    public abstract void onStepSelected(int newStepPosition, boolean userTriggeredChange);

    /**
     * Called to set whether the stepPosition has an error or not, changing it's appearance.
     * @param stepPosition the step to set the error
     * @param hasError whether it has error or not
     */
    public void setErrorFlag(int stepPosition, boolean hasError) {
        mStepErrors.put(stepPosition, hasError);
    }

    /**
     * Checks if there's an error for the step.
     *
     * @param stepPosition the step to check for error
     * @return true if there's an error for this step
     */
    public boolean getErrorAtPosition(int stepPosition) {
        return mStepErrors.get(stepPosition);
    }

    /**
     * Called when {@link StepperTabViewPage}'s adapter gets changed
     * @param stepAdapter new stepper adapter
     */
    @CallSuper
    public void onNewAdapter(@NonNull StepAdapter stepAdapter) {
        mStepErrors.clear();
    }

    @ColorInt
    protected int getSelectedColor() {
        return stepperTabViewPage.getSelectedColor();
    }

    @ColorInt
    protected int getUnselectedColor() {
        return stepperTabViewPage.getUnselectedColor();
    }

}