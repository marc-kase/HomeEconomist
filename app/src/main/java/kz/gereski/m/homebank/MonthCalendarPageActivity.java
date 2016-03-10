package kz.gereski.m.homebank;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import kz.gereski.m.homebank.domain.NavItem;
import kz.gereski.m.homebank.domain.NavProcessor;
import kz.gereski.m.homebank.util.CalendarHelper;
import kz.gereski.m.homebank.util.DBHelper;
import kz.gereski.m.homebank.domain.Day;
import kz.gereski.m.homebank.util.DrawerListAdapter;
import kz.gereski.m.homebank.util.Formatter;
import kz.gereski.m.homebank.views.CalendarTableLayout;
import kz.gereski.m.homebank.views.DateButton;

public class MonthCalendarPageActivity extends Activity implements CalendarTableLayout.OnCalendarSwiped {
    public static final int COLOR_SELECTED = Color.parseColor("#7F3D3D3C");
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private final ArrayList<NavItem> mNavItems = new ArrayList<>();
    private CalendarTableLayout tb;
    private DBHelper dbHelper;
    private Calendar calendar;
    private Locale locale;

    private static Calendar today = Calendar.getInstance();
    private static SimpleDateFormat df;
    private View prevView = null;
    public enum RequestCode {ProductActivity, ChartCalPage, DayListPage}


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_calendar_page);

        getLocale();
//        addSliderMenu();

        addOptions();

        dbHelper = new DBHelper(this);

        calendar = Calendar.getInstance(new Locale("ru"));
        updateToday();

        tb = (CalendarTableLayout) findViewById(R.id.tbMonth);
        tb.setOnCalendarSwipedListener(this);

        CalendarHelper.setScreenSize(getWindowManager());
    }

    private void addOptions() {
        ImageView opt = (ImageView) findViewById(R.id.btMenuList);
        opt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFileHandle();
            }
        });
    }

    private void getLocale() {
        locale = getResources().getConfiguration().locale;
        if (locale.getLanguage().equals("ru")) {
            df = Formatter.getRusDateFormatter1();
        }
        else df = new SimpleDateFormat("MMMM yyyy", locale);
    }

    private void addSliderMenu() {

        mNavItems.add(new NavItem("Data", "Import/export", R.drawable.ic_settings/*, new NavProcessor() {
            @Override
            public void start() {
                onFileHandle();
            }
        }*/));
        mNavItems.add(new NavItem("Data", "Import/export", R.drawable.ic_settings));
        mDrawerList = (ListView)findViewById(R.id.navList);
        DrawerListAdapter adapter = new DrawerListAdapter(this, mNavItems);
        mDrawerList.setAdapter(adapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mNavItems.get(position).start();
            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
//                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
//                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        RelativeLayout button = (RelativeLayout)findViewById(R.id.menuDateLayout);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        RelativeLayout rl = (RelativeLayout)findViewById(R.id.drawerPane);
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("On Clicked");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dbHelper != null) showMonth(calendar, today);
    }

    private void showMonth(Calendar cal, Calendar today) {
        CalendarHelper ch = CalendarHelper.getInstance();

        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int thisDay;
        if (calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
            thisDay = today.get(Calendar.DAY_OF_MONTH);
        } else {
            thisDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        }

        CalendarHelper.toBeginOfDay(cal);
        CalendarHelper.toBeginOfMonth(cal);
        Date begin = cal.getTime();
        ch.calculateDaysExpense(begin, maxDays, dbHelper);
        Day[][] days = ch.getDaysInMonth(begin);
        double total = ch.getMonthTotal(/*begin, dbHelper*/);
        double daily = total / thisDay;
        fillMonth(days, daily);

        TextView txtTotalExps = (TextView) findViewById(R.id.txtExpensesVal);
        TextView txtDailyExp = (TextView) findViewById(R.id.txtDailyExpVal);
        TextView txtPredictExp = (TextView) findViewById(R.id.txtPredictVal);
        TextView txtMonth = (TextView) findViewById(R.id.txtMonth);

        txtTotalExps.setText(Formatter.formatMoney(total));
        txtDailyExp.setText(Formatter.formatMoney(daily));
        txtPredictExp.setText(Formatter.formatMoney(daily * maxDays));
        txtMonth.setText(df.format(cal.getTime()));

        txtMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFileHandle();
            }
        });
    }

    private void fillMonth(Day[][] days, double total) {
        DateButton ib;
        TableRow tr;
        Day day;
        long div;

        int MAX_WEEKS = 6;
        for (int w = 0; w < MAX_WEEKS; w++) {
            tr = (TableRow) tb.getChildAt(w);
            for (int dw = 0; dw < 7; dw++) {
                ib = (DateButton) tr.getChildAt(dw);
                if (ib == null) {
                    ib = new DateButton(this);
                    tr.addView(ib, (new TableRow.LayoutParams(-1, -1, 1.0f)));
                }
                day = days[w][dw];
                ib.date = day.date.getTime();

                ib.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (prevView != null) {
                                prevView.setBackgroundColor(0x00000000);
                            }
                            prevView = v;
                            v.setBackgroundColor(COLOR_SELECTED);
                            showDayListPage(v);
                        }
                        return true;
                    }
                });
                boolean isEnabled = day.inThisMonth;
                ib.setParams(
                        (day.getDay() == 0 ? " " : String.valueOf(day.getDay())),
                        isEnabled ? day.getPercentageExpenses(total) : 0, Color.WHITE, isEnabled);

                div = new Date().getTime() - day.date.getTime();
                if (0 < div && div < 86400000L) {
                    ib.isToday(true);
                } else ib.isToday(false);
            }
        }
    }

    private void showDayListPage(View view) {
        boolean isRusLocale = locale.getLanguage().equals("ru");
        long d = ((DateButton) view).getDate();
        String date = isRusLocale ? Formatter.rusFormatDate(new Date(d), 2) :
                Formatter.formatDate(new Date(d), locale);

        Intent intent = new Intent(this, DayShoppingListPageActivity.class);
        intent.putExtra("Date", d);
        intent.putExtra("DateView", date);
        intent.putExtra("GroupId", -1L);
        intent.putExtra("Editable", true);
        startActivity(intent);
    }

    private void showChartCalendarPage(Calendar cal) {
        Intent intent = new Intent(this, ChartByGroupsPageActivity.class);
        intent.putExtra("Date", cal.getTime().getTime());
        startActivityForResult(intent, RequestCode.ChartCalPage.ordinal());
    }

    private void showChartYearPage(Calendar cal) {
        Intent intent = new Intent(this, ChartsInYearPageActivity.class);
        intent.putExtra("Date", cal.getTime().getTime());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_calendar_page, menu);
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

    @Override
    public void doSwipe(CalendarTableLayout.Direction direction) {
        Toast.makeText(this, getResources().getString(R.string.calculating) + "...", Toast.LENGTH_SHORT).show();
        if (direction == CalendarTableLayout.Direction.LEFT) onSwipeLeft();
        if (direction == CalendarTableLayout.Direction.RIGHT) onSwipeRight();
        if (direction == CalendarTableLayout.Direction.UP) onSwipeUp();
        if (direction == CalendarTableLayout.Direction.DOWN) onSwipeDw();
    }

    private void onSwipeUp() {
        showChartCalendarPage(calendar);
    }

    private void onSwipeDw() {
        showChartYearPage(calendar);
    }

    public void onSwipeRight() {
        calendar.add(Calendar.MONTH, 1);
        updateToday();
        showMonth(calendar, today);
    }

    public void onSwipeLeft() {
        calendar.add(Calendar.MONTH, -1);
        updateToday();
        showMonth(calendar, today);
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
        Intent intent = new Intent(this, OpenFileActivity.class);
        startActivity(intent);
    }

    private void updateToday() {
        today.setTime(new Date());
    }
}
