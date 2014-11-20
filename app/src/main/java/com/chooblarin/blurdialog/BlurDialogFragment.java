package com.chooblarin.blurdialog;

import android.app.Dialog;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by chooblarin on 2014/11/19.
 */
public class BlurDialogFragment extends DialogFragment {

    private Bitmap backgroundBitmap;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(getActivity());
        Window window = dialog.getWindow();

        // create bitmap as background
        View view = getActivity().getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);

        Bitmap bitmap = view.getDrawingCache();
        Rect rectgle= new Rect();
        getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(rectgle);
        int StatusBarHeight = rectgle.top;
        backgroundBitmap = Bitmap.createBitmap(
                bitmap, 0, StatusBarHeight, bitmap.getWidth(), bitmap.getHeight() - StatusBarHeight, null, true);
        view.setDrawingCacheEnabled(false);

        // set up dialog
        window.requestFeature(Window.FEATURE_NO_TITLE); // タイトル非表示
        dialog.setContentView(R.layout.fragment_blur);

        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 背景透明
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // create fast blur bitmap
        new AsyncTask<Void, Void, Drawable>() {
            @Override
            protected Drawable doInBackground(Void... params) {
                Bitmap localBitmap = Blur.fastblur(getActivity(), backgroundBitmap, 8);
                return new BitmapDrawable(getActivity().getResources(), localBitmap);
            }

            @Override
            protected void onPostExecute(Drawable drawable) {
                super.onPostExecute(drawable);
                getDialog().getWindow().setBackgroundDrawable(drawable);
            }
        }.execute();

        return dialog;
    }

    @Override public void onStart() {
        super.onStart();

        // no dimmed background
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0.0f;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(windowParams);

        window.setWindowAnimations(R.style.BlurDialogTheme);
    }

    @Override
    public void onDestroy() {
        if (backgroundBitmap != null) {
            backgroundBitmap.recycle();
        }
        super.onDestroy();
    }
}
