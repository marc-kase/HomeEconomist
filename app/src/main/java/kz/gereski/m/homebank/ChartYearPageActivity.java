package kz.gereski.m.homebank;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import kz.gereski.m.homebank.util.Formatter;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;

import static kz.gereski.m.homebank.util.Formatter.formatMoney;

public class ChartYearPageActivity extends Activity {
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_year_page);

        TextView tvExpenses = (TextView) findViewById(R.id.textCreditChartYearVal);
        RelativeLayout linkToGroup = (RelativeLayout) findViewById(R.id.linkToGropsChartYear);
        linkToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGroupPage();
            }
        });

        dbHelper = new DBHelper(this);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Formatter.parseDate("2015-12-01", Formatter.YYYYMMDDdashed));
//        calendar.setTime(new Date());todo temporary unavailable
        calendar.add(Calendar.MONTH, -11);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int year = calendar.get(Calendar.YEAR);
        Double total = 0.0;
        Map<Long, Double> perGroup;

        List<Column> cols = new ArrayList<>();
        for (int i = 1; i < 13; i++) {

            total += dbHelper.getTotalExpense(i, year);
            List values = new ArrayList();
//            Map<Long, String> groups = dbHelper.getGroups(dbHelper.getReadableDatabase());
//            groups.size();

            perGroup = dbHelper.getExpensesByGroup(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
            for (Map.Entry<Long, Double> g : perGroup.entrySet()) {
                int color = dbHelper.getGroupColorByKey(g.getKey(), dbHelper.getReadableDatabase());
                SubcolumnValue cval = new SubcolumnValue(g.getValue().floatValue(), color);
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
        }

        tvExpenses.setText(formatMoney(total));
    }

    private void showGroupPage() {
        Intent intent = new Intent(this, GroupsPageActivity.class);
        startActivity(intent);
    }
}
