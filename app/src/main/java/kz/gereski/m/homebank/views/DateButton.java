package kz.gereski.m.homebank.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.Date;

import kz.gereski.m.homebank.R;

public class DateButton extends LinearLayout {
    public static final int DISABLED_COLOR = Color.DKGRAY;
    public static final int PERCENTAGE_THRESHOLD = 110;
    private TextView txt;
    private TextView txtProgress;
    private ProgressBar pbar;
    private Context context;
    public long date;

    public DateButton(Context context) {
        super(context);
        this.context = context;
        setOrientation(LinearLayout.VERTICAL);

        txt = new TextView(context);
        TableLayout.LayoutParams txtLayoutParam
                = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0.2f);
        txt.setLayoutParams(txtLayoutParam);

        txtProgress = new TextView(context);
        txtProgress.setGravity(Gravity.CENTER);
        txtProgress.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        pbar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        pbar.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        pbar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        pbar.setMax(100);

        TableLayout.LayoutParams imgLayoutParam
                = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT, 0.8f);
        FrameLayout flayout = new FrameLayout(context) {
            @Override
            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                super.onSizeChanged(w, h, oldw, oldh);
                txtProgress.setTextSize(TypedValue.COMPLEX_UNIT_PX, h*2/3);
            }
        };
        flayout.setPadding(0,0,0,0);
        flayout.setLayoutParams(imgLayoutParam);
        flayout.addView(pbar);
        flayout.addView(txtProgress);

        setPadding(3, 3, 3, 0);
        addView(txt);
        addView(flayout);
    }

    public void setParams(String text, int percent, int color, boolean isEnabled) {
        txt.setGravity(Gravity.CENTER);
        txt.setTextAppearance(context, android.R.style.TextAppearance_Large);
        txt.setText(text);
        txt.setTextColor(isEnabled ? color : DISABLED_COLOR);

        int drw = R.drawable.pbar_disabled;
        txtProgress.setText("");

        if (isEnabled) {
            txtProgress.setText(percent + "%");
            drw = R.drawable.pbar_redgreen;
            if (percent > PERCENTAGE_THRESHOLD) {
                percent = 10000/percent;
                drw = R.drawable.pbar_greenred;
            }
        }

        Drawable drawable = getResources().getDrawable(drw);
        pbar.setProgressDrawable(drawable);
        pbar.setProgress(percent);
    }

    public void isToday (boolean isToday) {
        if (isToday) txt.setBackgroundColor(Color.parseColor("#56274C62"));
        else txt.setBackgroundColor(Color.TRANSPARENT);
    }

    public String getText() {
        return txt.getText().toString();
    }

    public long getDate() {
        return date;
    }
}
