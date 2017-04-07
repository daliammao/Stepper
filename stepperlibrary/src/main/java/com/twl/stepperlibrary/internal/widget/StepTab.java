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

package com.twl.stepperlibrary.internal.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.twl.stepperlibrary.Constant;
import com.twl.stepperlibrary.R;

import java.io.InputStream;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

/**
 * A widget for a single tab in the {@link TabsContainer}.
 */
@RestrictTo(LIBRARY)
public class StepTab extends FrameLayout {

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

    @ColorInt
    private int mTitleColor;

    private final View mStepDividerLeft;

    private final View mStepDividerRight;

    private final TextView mStepTitle;

    private final TextView mStepNumber;

    private final ImageView mStepDoneIndicator;

    private final ImageView mStepIconBackground;

    /**
     * Current UI state of the tab. See {@link AbstractState} for more details.
     */
    private AbstractState mCurrentState = new InactiveNumberState();

    private AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();

    public StepTab(Context context) {
        this(context, null);
    }

    public StepTab(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepTab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(getContext()).inflate(R.layout.ms_step_tab, this, true);

        mSelectedColor = ContextCompat.getColor(context, R.color.ms_selectedColor);
        mUnselectedColor = ContextCompat.getColor(context, R.color.ms_unselectedColor);
        mUnselectedNumColor = ContextCompat.getColor(context, R.color.ms_unselectedNumColor);
        mUnselectedNumColor = ContextCompat.getColor(context, R.color.ms_unselectedNumColor);
        mErrorColor = ContextCompat.getColor(context, R.color.ms_errorColor);

        mStepNumber = (TextView) findViewById(R.id.ms_stepNumber);
        mStepDoneIndicator = (ImageView) findViewById(R.id.ms_stepDoneIndicator);
        mStepIconBackground = (ImageView) findViewById(R.id.ms_stepIconBackground);
        mStepDividerLeft = findViewById(R.id.ms_stepDivider_left);
        mStepDividerRight = findViewById(R.id.ms_stepDivider_right);
        mStepTitle = ((TextView) findViewById(R.id.ms_stepTitle));

        mTitleColor = mStepTitle.getCurrentTextColor();

        Drawable avd = createCircleDrawable();
        mStepIconBackground.setImageDrawable(avd);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize;
        widthSize = mStepDividerLeft.getLayoutParams().width + mStepDividerRight.getLayoutParams().width
                + mStepNumber.getLayoutParams().width;

        setMeasuredDimension(getDefaultSize(widthSize, widthMeasureSpec),
                heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int parentLeft = getPaddingLeft();
        final int parentRight = getMeasuredWidth() - getPaddingRight();
        final int parentTop = getPaddingTop();
        final int parentBottom = getMeasuredHeight() - getPaddingBottom();
        final int parentCentterX = (parentRight - parentLeft) / 2;

        View iconContainer = findViewById(R.id.ms_iconContainer);
        iconContainer.layout(parentCentterX - iconContainer.getMeasuredWidth() / 2,
                parentTop,
                parentCentterX + iconContainer.getMeasuredWidth() / 2,
                parentTop + iconContainer.getMeasuredHeight());


        int divierTop = iconContainer.getTop() + iconContainer.getMeasuredHeight() / 2 - mStepDividerLeft.getMeasuredHeight() / 2;

        mStepDividerLeft.layout(parentLeft, divierTop, iconContainer.getLeft(), divierTop + mStepDividerLeft.getMeasuredHeight());
        mStepDividerRight.layout(iconContainer.getRight(), divierTop, parentRight, divierTop + mStepDividerRight.getMeasuredHeight());
        mStepTitle.layout(parentCentterX - mStepTitle.getMeasuredWidth() / 2,
                parentBottom - mStepTitle.getMeasuredHeight(),
                parentCentterX + mStepTitle.getMeasuredWidth() / 2,
                parentBottom);
    }

    /**
     * Changes the visibility of the horizontal line in the tab
     */
    public void toggleDivider(boolean isfirst, boolean isLast) {
        mStepDividerLeft.setVisibility(!isfirst ? VISIBLE : INVISIBLE);
        mStepDividerRight.setVisibility(!isLast ? VISIBLE : INVISIBLE);
    }

    /**
     * Updates the UI state of the tab and sets {@link #mCurrentState} based on the arguments.
     *
     * @param error   <code>true</code> if an error/warning should be shown, if <code>true</code> a warning will be shown
     * @param done    true the step was completed, if warning is not shown and this is <code>true</code> a done indicator will be shown
     * @param current true if this is the currently selected step
     */
    public void updateState(final boolean error, final boolean done, final boolean current) {
        // FIXME: 05/03/2017 stop tabs from changing positions due to changing font type (does not happen e.g. on API 16, investigate further)
        if (error) {
            mCurrentState.changeToWarning();
        } else if (done) {
            mCurrentState.changeToDone();
        } else if (current) {
            mCurrentState.changeToActiveNumber();
        } else {
            mCurrentState.changeToInactiveNumber();
        }
    }

    /**
     * Sets the name of the step
     *
     * @param title step name
     */
    public void setStepTitle(CharSequence title) {
        mStepTitle.setText(title);
    }

    /**
     * Sets the position of the step
     *
     * @param number step position
     */
    public void setStepNumber(CharSequence number) {
        mStepNumber.setText(number);
    }

    public void setUnselectedColor(int unselectedColor) {
        this.mUnselectedColor = unselectedColor;
    }

    public void setSelectedColor(int selectedColor) {
        this.mSelectedColor = selectedColor;
    }

    public void setUnselectedNumColor(int unselectedNumColor) {
        this.mUnselectedNumColor = unselectedNumColor;
    }

    public void setSelectedNumColor(int selectedNumColor) {
        this.mSelectedNumColor = selectedNumColor;
    }

    public void setErrorColor(int errorColor) {
        this.mErrorColor = errorColor;
    }

    public void setDividerWidth(int dividerWidth) {
        mStepDividerLeft.getLayoutParams().width = dividerWidth != Constant.DEFAULT_TAB_DIVIDER_WIDTH
                ? dividerWidth
                : getResources().getDimensionPixelOffset(R.dimen.ms_step_tab_divider_length);

        mStepDividerRight.getLayoutParams().width = dividerWidth != Constant.DEFAULT_TAB_DIVIDER_WIDTH
                ? dividerWidth
                : getResources().getDimensionPixelOffset(R.dimen.ms_step_tab_divider_length);
    }

    private Drawable createCircleDrawable() {
        return createAnimatedVectorDrawable(R.drawable.ms_vector_circle_24dp);
    }

    private Drawable createWarningDrawable() {
        return createAnimatedVectorDrawable(R.drawable.ms_vector_warning_24dp);
    }

    /**
     * Inflates an animated vector drawable. On Lollipop+ this uses the native {@link android.graphics.drawable.AnimatedVectorDrawable}
     * and below it inflates the drawable as a {@link AnimatedVectorDrawableCompat}.
     *
     * @param animatedVectorDrawableResId resource ID for the animated vector
     * @return animated vector drawable
     */
    public Drawable createAnimatedVectorDrawable(@DrawableRes int animatedVectorDrawableResId) {
        Context context = getContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = context.getDrawable(animatedVectorDrawableResId);
            return drawable.getConstantState().newDrawable(context.getResources());
        } else {
            Bitmap bitmap = readBitMap(context, animatedVectorDrawableResId);
            if (null == bitmap)
                return null;
            Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
            return drawable;
        }
    }

    /**
     * 以最省内存的方式读取本地资源的图片
     *
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap readBitMap(@NonNull Context context, @NonNull int resId) {
        if (null == context)
            return null;
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    /**
     * <p>Base state class of the tab.
     * It is used to specify what should happen from the UI perspective
     * when transitioning to other states, e.g. which views to hide or tint.</p>
     * <p>Subclasses include:</p>
     * <ul>
     * <li>{@link InactiveNumberState} - for when we show the step number, but this step still hasn't been reached</li>
     * <li>{@link ActiveNumberState} - for when we show the step number of the currently active tab</li>
     * <li>{@link DoneState} - for when the step has already been completed and the user has moved to the next step</li>
     * <li>{@link WarningState} - for when there has been an error on this step (if  <code>ms_showErrorStateEnabled</code> was set to <i>true</i>)</li>
     * </ul>
     */
    private abstract class AbstractState {

        @CallSuper
        protected void changeToInactiveNumber() {
            mStepDividerLeft.setBackgroundColor(mUnselectedColor);
            mStepDividerRight.setBackgroundColor(mUnselectedColor);
            mStepNumber.setTextColor(mUnselectedNumColor);
            StepTab.this.mCurrentState = new InactiveNumberState();
        }

        @CallSuper
        protected void changeToActiveNumber() {
            mStepDividerLeft.setBackgroundColor(mSelectedColor);
            mStepDividerRight.setBackgroundColor(mUnselectedColor);
            mStepNumber.setTextColor(mSelectedNumColor);
            StepTab.this.mCurrentState = new ActiveNumberState();
        }

        @CallSuper
        protected void changeToDone() {
            mStepDividerLeft.setBackgroundColor(mSelectedColor);
            mStepDividerRight.setBackgroundColor(mSelectedColor);
            mStepNumber.setTextColor(mSelectedNumColor);
            StepTab.this.mCurrentState = new DoneState();
        }

        @CallSuper
        protected void changeToWarning() {
            mStepDoneIndicator.setVisibility(View.GONE);
            mStepNumber.setVisibility(View.GONE);
            mStepIconBackground.setColorFilter(mErrorColor);
            mStepDividerLeft.setBackgroundColor(mSelectedColor);
            mStepDividerRight.setBackgroundColor(mUnselectedColor);
            mStepTitle.setTextColor(mErrorColor);
            StepTab.this.mCurrentState = new WarningState();
        }
    }

    private abstract class AbstractNumberState extends AbstractState {

        @Override
        @CallSuper
        protected void changeToWarning() {
            Drawable avd = createWarningDrawable();
            mStepIconBackground.setImageDrawable(avd);
            super.changeToWarning();
        }

        @Override
        @CallSuper
        protected void changeToDone() {
            mStepDoneIndicator.setVisibility(VISIBLE);
            mStepNumber.setVisibility(GONE);
            super.changeToDone();
        }

    }

    private class InactiveNumberState extends AbstractNumberState {

        @Override
        protected void changeToInactiveNumber() {
            mStepIconBackground.setColorFilter(mUnselectedColor);
            mStepTitle.setTextColor(mTitleColor);
            super.changeToInactiveNumber();
        }

        @Override
        protected void changeToActiveNumber() {
            mStepIconBackground.setColorFilter(mSelectedColor);
            mStepTitle.setTextColor(mSelectedColor);
            super.changeToActiveNumber();
        }

        @Override
        protected void changeToDone() {
            mStepIconBackground.setColorFilter(mSelectedColor);
            mStepTitle.setTextColor(mSelectedColor);
            super.changeToDone();
        }
    }

    private class ActiveNumberState extends AbstractNumberState {

        @Override
        protected void changeToInactiveNumber() {
            mStepIconBackground.setColorFilter(mUnselectedColor);
            mStepTitle.setTextColor(mTitleColor);
            super.changeToInactiveNumber();
        }
    }

    private class DoneState extends AbstractState {

        @Override
        protected void changeToInactiveNumber() {
            mStepDoneIndicator.setVisibility(GONE);
            mStepNumber.setVisibility(VISIBLE);
            mStepIconBackground.setColorFilter(mUnselectedColor);
            mStepTitle.setTextColor(mTitleColor);
            super.changeToInactiveNumber();
        }

        @Override
        protected void changeToActiveNumber() {
            mStepDoneIndicator.setVisibility(GONE);
            mStepNumber.setVisibility(VISIBLE);
            super.changeToActiveNumber();
        }

        @Override
        protected void changeToWarning() {
            Drawable avd = createWarningDrawable();
            mStepIconBackground.setImageDrawable(avd);
            super.changeToWarning();
        }
    }

    private class WarningState extends AbstractState {

        @Override
        protected void changeToDone() {
            animateViewIn(mStepDoneIndicator);

            mStepIconBackground.setColorFilter(mSelectedColor);
            mStepTitle.setTextColor(mSelectedColor);

            mStepDoneIndicator.setVisibility(VISIBLE);
            mStepNumber.setVisibility(GONE);

            super.changeToDone();
        }

        @Override
        protected void changeToInactiveNumber() {
            animateViewIn(mStepNumber);

            mStepIconBackground.setColorFilter(mUnselectedColor);
            mStepTitle.setTextColor(mTitleColor);
            mStepNumber.setVisibility(VISIBLE);
            super.changeToInactiveNumber();
        }

        @Override
        protected void changeToActiveNumber() {
            animateViewIn(mStepNumber);

            mStepIconBackground.setColorFilter(mSelectedColor);
            mStepTitle.setTextColor(mSelectedColor);
            mStepNumber.setVisibility(VISIBLE);
            super.changeToActiveNumber();
        }

        private void animateViewIn(final View view) {
            Drawable avd = createCircleDrawable();
            mStepIconBackground.setImageDrawable(avd);
        }
    }

}
