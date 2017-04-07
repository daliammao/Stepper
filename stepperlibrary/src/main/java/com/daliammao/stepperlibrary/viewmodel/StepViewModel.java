package com.daliammao.stepperlibrary.viewmodel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

/**
 * Contains view information about the step.
 *
 * @author Piotr Zawadzki
 */
public class StepViewModel {

    /**
     * Drawable resource ID to be used for back/next navigation button compound drawables when we do not want to show them.
     */
    public static final int NULL_DRAWABLE = -1;

    private StepViewModel() {}

    /**
     * The displayable name of the step.
     */
    @Nullable
    private CharSequence mTitle;


    @Nullable
    public CharSequence getTitle() {
        return mTitle;
    }

    public static class Builder {

        @NonNull
        private final Context mContext;

        @Nullable
        private CharSequence mTitle;

        /**
         * Creates a builder for the step info.
         *
         * @param context the parent context
         */
        public Builder(@NonNull Context context) {
            this.mContext = context;
        }

        /**
         * Set the title using the given resource id.
         *
         * @param titleId string resource ID for the title
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setTitle(@StringRes int titleId) {
            this.mTitle = mContext.getString(titleId);
            return this;
        }

        /**
         * Set the title using the given characters.
         *
         * @param title CharSequence to be used as a title
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setTitle(@Nullable CharSequence title) {
            this.mTitle = title;
            return this;
        }

        /**
         * Creates a {@link StepViewModel} with the arguments supplied to this
         * builder.
         * @return created StepViewModel
         */
        public StepViewModel create() {
            final StepViewModel viewModel = new StepViewModel();
            viewModel.mTitle = this.mTitle;
            return viewModel;
        }

    }
}
