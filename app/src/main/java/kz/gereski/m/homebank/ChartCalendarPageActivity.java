package kz.gereski.m.homebank;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import kz.gereski.m.homebank.util.Formatter;

public class ChartCalendarPageActivity extends Activity {
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_calendar_page);

        Locale locale = getResources().getConfiguration().locale;
        SimpleDateFormat df;
        if (locale.getLanguage().equals("ru")) {
            df = Formatter.getRusDateFormatter1();
        } else df = new SimpleDateFormat("MMMM yyyy", locale);

        DBHelper dbHelper = new DBHelper(this);
        calendar = Calendar.getInstance(new Locale("ru"));
        Long d = getIntent().getExtras().getLong("Date");
        calendar.setTime(new Date(d));

        TextView tvMonth = (TextView)findViewById(R.id.txtChartMonth);
        tvMonth.setText(df.format(calendar.getTime()));

        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
//        int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        double total = dbHelper.getTotalExpense(month, year);
        Map<Long, Double> expByGroup = dbHelper.getExpensesByGroup(month, year);
        Map<Long, String> gs = dbHelper.getGroups(dbHelper.getReadableDatabase());
        Map<Long, String> groups = new TreeMap<>(gs);
        Long pbarMaxVal = Math.round(getMaxExpenses(expByGroup)/total*100+1);

        TextView allExps = (TextView)findViewById(R.id.txtChartExpVal);
        allExps.setText(Formatter.formatMoney(total));

        RelativeLayout linkToGroups = (RelativeLayout)findViewById(R.id.linkToGropsChartCal);
        linkToGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGroupPage();
            }
        });

        LinearLayout lay = (LinearLayout) findViewById(R.id.layProgressGroup);
        for (Map.Entry<Long, String> g : groups.entrySet()) {

            final TextView group = new TextView(this);
            group.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
            group.setText(g.getValue() != null ? g.getValue() : getResources().getString(R.string.no_group));

            final TextView txtProgress = new TextView(this);
            txtProgress.setTextColor(Color.WHITE);
            txtProgress.setGravity(Gravity.CENTER);
            txtProgress.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

            ProgressBar pbar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            pbar.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            pbar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            pbar.setProgressDrawable(getResources().getDrawable(R.drawable.pbar_redgreen));
            pbar.setMinimumHeight(10);
            pbar.setMax(pbarMaxVal.intValue());

            Double ex = expByGroup.get(g.getKey());
            int percent = 0;
            String expense = "0.00";
            if (total > 0.01 && ex != null) {
                percent = Math.round((float) (ex * 100 / total));
                expense = Formatter.formatMoney(ex);
            }
            pbar.setProgress(percent);
            txtProgress.setText(expense);

            TableLayout.LayoutParams imgLayoutParam
                    = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            FrameLayout flayout = new FrameLayout(this) {
                @Override
                protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                    super.onSizeChanged(w, h, oldw, oldh);
                    txtProgress.setTextSize(TypedValue.COMPLEX_UNIT_PX, h * 2 / 3);
                    group.setTextSize(TypedValue.COMPLEX_UNIT_PX, h*2/3);
                }
            };
            flayout.setPadding(0, 0, 0, 0);
            flayout.setLayoutParams(imgLayoutParam);
            flayout.addView(pbar);
            flayout.addView(txtProgress);
            final long groupId = g.getKey();
            flayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showGroupListPage(v, groupId);
                }
            });

            lay.addView(group);
            lay.addView(flayout);
        }
    }

    private double getMaxExpenses(Map<Long, Double> expByGroup) {
        double max = 0;
        Collection<Double> vs = expByGroup.values();
        for (Double v : vs) {
            if (v > max) max = v;
        }
        return max;
    }

    private void showGroupPage() {
        Intent intent = new Intent(this, GroupsPageActivity.class);
        startActivity(intent);
    }

    private void showGroupListPage(View view, long groupId) {
        Intent intent = new Intent(this, DayListPageActivity.class);
        intent.putExtra("Date", calendar.getTime().getTime());
        intent.putExtra("GroupId", groupId);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chart_calendar_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
