package kz.gereski.m.homebank.domain;


import java.util.Calendar;
import java.util.Date;

public class Day {
    public final Date date;
    public boolean inThisMonth;
    public double expense = 0;
    private static Calendar c = Calendar.getInstance();

    public Day(Date date, boolean inThisMonth) {
        this.date = date;
        this.inThisMonth = inThisMonth;
    }

    public int getDay() {
        c.setTime(date);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    public int getPercentageExpenses(double dailyExpense) {
        return Math.round((float)(expense/dailyExpense*100));
    }

    public void setExpense(Double expense) {
        if (expense != null) this.expense = expense;
    }
}
