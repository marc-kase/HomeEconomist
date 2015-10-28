package kz.gereski.m.homebank;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Calendar;

import kz.gereski.m.homebank.util.CalendarHelper;

import static kz.gereski.m.homebank.util.Formatter.YYYYMMDDdashed;
import static kz.gereski.m.homebank.util.Formatter.formatMoney;
import static kz.gereski.m.homebank.util.Formatter.gap;
import static kz.gereski.m.homebank.util.Formatter.parseDate;
import static kz.gereski.m.homebank.util.Formatter.rusMonths1;


public class CalendarPageActivity extends Activity {
    private long calendarDate;
    private String currentMonth = "";
    private TextView monthName = null;
    private CalendarView cv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_page);

        cv = (CalendarView) findViewById(R.id.calendar);

        ViewGroup vg = (ViewGroup) cv.getChildAt(0);
        View child = vg.getChildAt(0);
        if (child instanceof TextView) {
            monthName = (TextView) child;
        }

        fillCredentials();

        monthName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                fillCredentials();
            }
        });

//        Class<?> cvClass = cv.getClass();
//        Field field = null;
//        try {
//            field = cvClass.getDeclaredField("mMonthName");
//            field.setAccessible(true);
//            monthName = (TextView) field.get(cv);
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            e.printStackTrace();
//        }

        calendarDate = cv.getDate();

        cv.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                onDayChanged(view);
            }
        });

        cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFileHandle();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillCredentials();
    }

    private void fillCredentials() {
        Toast.makeText(this, getResources().getString(R.string.calculating) + "...", Toast.LENGTH_SHORT).show();

        TextView tCredit = (TextView) findViewById(R.id.textCredit);
        TextView tDailyCredit = (TextView) findViewById(R.id.textDailyCredit);
        TextView tPredictCredit = (TextView) findViewById(R.id.textPredictCredit);

        String m = monthName.getText().toString();
        currentMonth = m;

        int month = Arrays.asList(rusMonths1).indexOf((currentMonth.split("\\s")[0])) + 1;
        int year = Integer.parseInt(currentMonth.split("\\s")[1]);

        int numDays = 1;
        Calendar c = Calendar.getInstance();
        if (c.get(Calendar.MONTH) == month - 1) {
            numDays = c.get(Calendar.DAY_OF_MONTH);
        } else {
            c.setTime(parseDate(year + "-" + gap(month, 2) + "-01", YYYYMMDDdashed));
            numDays = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        Double res = getTotalTransaction(month, year);
        tCredit.setText(getResources().getString(R.string.credit) + ": " + formatMoney(res));
        tDailyCredit.setText(getResources().getString(R.string.daily_credit) + ": " + formatMoney(res / numDays));
        tPredictCredit.setText(getResources().getString(R.string.predict_credit) + ": "
                + formatMoney(res / numDays * c.getActualMaximum(Calendar.DAY_OF_MONTH)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_first_page, menu);
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

    private double getTotalTransaction(int month, int year) {
        DBHelper db = new DBHelper(this);
        if (db.isDatabaseExists())
            return db.getTotalExpense(month, year);
        else return 0.0;
    }

    private void onDayChanged(CalendarView view) {
        if (view.getDate() != calendarDate) {
            calendarDate = view.getDate();

            Intent intent = new Intent(this, DayListPageActivity.class);
            intent.putExtra("Date", view.getDate());
            startActivity(intent);
        }
    }

    private boolean onFileHandle() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.import_export_question);

        alert.setPositiveButton(R.string.export_string, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                doExport();
            }
        });

        alert.setNegativeButton(R.string.import_string,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        doImport();
                    }
                });

        alert.show();

        return true;
    }

    private void doExport() {
        Intent intent = new Intent(this, SaveFileActivity.class);
        startActivity(intent);
    }

    private void doImport() {
//        Intent intent = new Intent(this, OpenFileActivity.class);
        Intent intent = new Intent(this, DaysCalendarPageActivity.class);
        startActivity(intent);
    }
}
