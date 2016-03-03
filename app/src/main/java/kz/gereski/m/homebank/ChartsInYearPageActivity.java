package kz.gereski.m.homebank;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import kz.gereski.m.homebank.util.DBHelper;
import kz.gereski.m.homebank.util.Formatter;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;

import static kz.gereski.m.homebank.util.Formatter.formatMoney;

public class ChartsInYearPageActivity extends Activity {
    private int[] mths = {
            R.string.label_jan, R.string.label_feb, R.string.label_mar, R.string.label_apr,
            R.string.label_may, R.string.label_jun, R.string.label_jul, R.string.label_aug,
            R.string.label_sep, R.string.label_oct, R.string.label_nov, R.string.label_dec,};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts_in_year_page);

        TextView tvExpenses = (TextView) findViewById(R.id.textCreditChartYearVal);
        RelativeLayout linkToGroup = (RelativeLayout) findViewById(R.id.linkToGropsChartYear);
        linkToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGroupPage();
            }
        });

        LinearLayout row = (LinearLayout)findViewById(R.id.layMonths);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

        DBHelper dbHelper = new DBHelper(this);

        Calendar calendar = Calendar.getInstance();
        Long d = getIntent().getExtras().getLong("Date");
        calendar.setTime(new Date(d));

        calendar.add(Calendar.MONTH, -11);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int year = calendar.get(Calendar.YEAR);
        Double total = 0.0;
        Map<Long, Double> perGroup;
        int currMonth = calendar.get(Calendar.MONTH);

        Map<Long, String> groups = dbHelper.getGroups(dbHelper.getReadableDatabase());
        String label;

        List<Column> cols = new ArrayList<>();
        for (int i = 1; i < 13; i++) {
            TextView tv = new TextView(this);
            tv.setText(mths[currMonth]);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setLayoutParams(params);
            row.addView(tv);

            total += dbHelper.getTotalExpense(i, year);
            List values = new ArrayList();

            perGroup = dbHelper.getExpensesByGroup(calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.YEAR));
            for (Map.Entry<Long, Double> g : perGroup.entrySet()) {
                int color = dbHelper.getGroupColorByKey(g.getKey(), dbHelper.getReadableDatabase());
                SubcolumnValue cval = new SubcolumnValue(g.getValue().floatValue(), color);
                label = groups.get(g.getKey()) != null ? groups.get(g.getKey()) : getString(R.string.nogroup);
                cval.setLabel(label + ": " + Formatter.formatMoney(g.getValue()));
                values.add(cval);
            }

            calendar.add(Calendar.MONTH, 1);

            Column column = new Column(values);
            column.setHasLabelsOnlyForSelected(true);
            cols.add(column);
            ColumnChartData data = new ColumnChartData();
            data.setColumns(cols);

            Axis axisY = new Axis();
            axisY.setHasSeparationLine(true);
            axisY.setHasTiltedLabels(false);
            axisY.setHasLines(true);
            axisY.setInside(true);
            data.setAxisXBottom(null);
            data.setAxisYLeft(axisY);
            data.setStacked(true);

            ColumnChartView chartView = (ColumnChartView) findViewById(R.id.columnchart);
            chartView.setColumnChartData(data);

            if (++currMonth > 11) currMonth=0;
        }

        tvExpenses.setText(formatMoney(total));

        addOptions();
    }

    private void showGroupPage() {
        Intent intent = new Intent(this, GroupsPageActivity.class);
        startActivity(intent);
    }

    private void addOptions() {
        ImageView opt = (ImageView) findViewById(R.id.btMenuList);
        opt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGroupPage();
            }
        });
    }
}
