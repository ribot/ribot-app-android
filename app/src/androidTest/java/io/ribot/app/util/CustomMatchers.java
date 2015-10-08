package io.ribot.app.util;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public final class CustomMatchers {

    public static Matcher<View> hasCompoundDrawableRelative(final boolean start,
                                                            final boolean top,
                                                            final boolean end,
                                                            final boolean bottom) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View view) {
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    Drawable[] drawables = textView.getCompoundDrawablesRelative();
                    boolean hasStart = drawables[0] != null;
                    boolean hastTop = drawables[1] != null;
                    boolean hasEnd = drawables[2] != null;
                    boolean hasBottom = drawables[3] != null;
                    return start == hasStart &&
                            top == hastTop &&
                            end == hasEnd &&
                            bottom == hasBottom;

                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("CompoundDrawables relative not matched");
            }
        };

    }
}
