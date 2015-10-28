package kz.gereski.m.homebank.util;


import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import kz.gereski.m.homebank.DBHelper;

public class CalendarHelper {
    //    public static final int NUMBER_OF_DAYS = 43;
    public static Screen screen = null;

    public Calendar current;
    private Map<Long, Double> daysExpense = new HashMap<>();

    private CalendarHelper() {
        current = Calendar.getInstance();
    }

    private final static CalendarHelper INSTANCE = new CalendarHelper();

    public static CalendarHelper getInstance() {
        return INSTANCE;
    }

    public Day[][] getDaysInMonth(final Date startDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

//        Date startDay = cal.getTime();
//        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int thisMonth = cal.get(Calendar.MONTH);
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        toBeginOfWeek(cal);
        cal.setFirstDayOfWeek(Calendar.MONDAY);

        int maxWeeks = cal.getActualMaximum(Calendar.WEEK_OF_MONTH) + 1;
        Day[][] days = new Day[maxWeeks + 1][7];

        for (int i = 0; i < maxWeeks; i++) {
//            System.out.println(cal.get(Calendar.WEEK_OF_YEAR));
            for (int j = 0; j < 7; j++) {
                final Day day = new Day(cal.getTime(), cal.get(Calendar.MONTH) == thisMonth);
                Double expense = daysExpense.get(cal.getTime().getTime());
                day.setExpense(expense == null ? 0.0 : expense);
                days[i][j] = day;
                cal.add(Calendar.DAY_OF_WEEK, 1);
//                System.out.print("\t" + cal.get(Calendar.DAY_OF_MONTH) + "\t");
            }
        }
        return days;
    }

    public double getMonthTotal() {
        double total = 0.0;
        Set<Long> keys = daysExpense.keySet();
        for (long k : keys) {
            total += daysExpense.get(k);
        }
        return total;
    }

    public static void toBeginOfWeek(Calendar cal) {
        cal.set(Calendar.DAY_OF_WEEK, 1);
    }

    public static void toBeginOfMonth(Calendar cal) {
        cal.set(Calendar.DAY_OF_MONTH, 1);
    }

    public void toEndOfMonth(Calendar cal) {
        int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, days);
    }

    public static void toBeginOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
    }

    private Map<Long, Double> getDaysExpense(Date startDate, int numberOfDays, DBHelper db) {
        return db.getDailyExpense(startDate, numberOfDays);
    }

    public static Date parseDate(String date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return df.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    public void calculateDaysExpense(Date startDate, int numberOfDays, DBHelper db) {
        daysExpense = db.getDailyExpense(startDate, numberOfDays);
    }

    public Map<Long, Double> getDaysExpense() {
        return daysExpense;
    }

    public static void main(String[] args) {
        Calendar cal = Calendar.getInstance(new Locale("ru"));
        int maxWeeks = cal.getActualMaximum(Calendar.WEEK_OF_MONTH);
//        CalendarHelper ch = CalendarHelper.getInstance();
    }

    public static Screen getScreenSize() {
        return screen;
    }

    public static void setScreenSize(WindowManager wmanager) {
        if (screen == null) {
            Display display = wmanager.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screen = new Screen(size.x, size.y);
        }
    }
}
