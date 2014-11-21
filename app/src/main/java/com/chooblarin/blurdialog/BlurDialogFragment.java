package com.chooblarin.blurdialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by chooblarin on 2014/11/19.
 */
public class BlurDialogFragment extends DialogFragment {

    private Drawable backgroundDrawable;

    public void fadeIn(final FragmentActivity activity, final String tag) {
        final Bitmap backgroundBitmap = createBackgroundBitmap(activity);

        // create fast blur bitmap and show dialog
        new AsyncTask<Void, Void, Drawable>() {
            @Override
            protected Drawable doInBackground(Void... params) {
                Bitmap localBitmap = Blur.fastblur(activity, backgroundBitmap, 16);
                return new BitmapDrawable(activity.getResources(), localBitmap);
            }

            @Override
            protected void onPostExecute(Drawable drawable) {
                backgroundDrawable = drawable;
                show(activity.getSupportFragmentManager(), tag);

                if (backgroundBitmap != null) {
                    backgroundBitmap.recycle();
                }
            }
        }.execute();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(getActivity());
        Window window = dialog.getWindow();

        // set up dialog
        window.requestFeature(Window.FEATURE_NO_TITLE); // タイトル非表示
        dialog.setContentView(R.layout.fragment_blur);

        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 背景透明
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawable(backgroundDrawable);

        window.setWindowAnimations(R.style.BlurDialogTheme);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        // no dimmed background
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0.0f;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(windowParams);
    }

    private Bitmap createBackgroundBitmap(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        Bitmap bmp = view.getDrawingCache();

        int orientation = getDisplayOrientation(activity);

        int statusBarHeight = getStatusBarHeight(activity);
        int navigationBarHeight = getNavigationBarHeight(activity, orientation);

        int width = orientation == Configuration.ORIENTATION_PORTRAIT ?
                bmp.getWidth() : bmp.getWidth() - navigationBarHeight;
        int height = orientation == Configuration.ORIENTATION_PORTRAIT ?
                bmp.getHeight() - statusBarHeight - navigationBarHeight
                : bmp.getHeight() - statusBarHeight;

        Bitmap bgBitmap = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height, null, true);
        view.setDrawingCacheEnabled(false);
        return bgBitmap;
    }

    private static int getDisplayOrientation(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        switch (display.getRotation()) {
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                return Configuration.ORIENTATION_LANDSCAPE;

            default:
                return Configuration.ORIENTATION_PORTRAIT;
        }
    }

    private static int getStatusBarHeight(Activity activity) {

        Rect rectangle = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
        return rectangle.top;
    }

    private static int getNavigationBarHeight(Context context, int orientation) {
        Resources resources = context.getResources();

        int id = resources.getIdentifier(
                orientation == Configuration.ORIENTATION_PORTRAIT
                        ? "navigation_bar_height" : "navigation_bar_height_landscape",
                "dimen", "android");
        if (id > 0) {
            return resources.getDimensionPixelSize(id);
        }
        return 0;
    }
}
