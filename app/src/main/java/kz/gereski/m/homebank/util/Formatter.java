package kz.gereski.m.homebank.util;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Formatter {
    public static SimpleDateFormat YYYYMMDDdashed = new SimpleDateFormat("yyyy-MM-dd");
    public static String[] rusMonths1 = {"Январь", "Февраль", "Март",
            "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
    private static String[] rusMonths2 = {"января", "февраля", "марта",
            "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"};

    public static String gap(int value, int digits) {
        String Z = "0000000000000000";
        String v = String.valueOf(value);
        int zeros = digits - v.length();
        return Z.substring(0, zeros) + v;
    }

    public static Date parseDate(String date, SimpleDateFormat df) {
        try {
            return df.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String formatDate(Date date, Locale locale) {
        SimpleDateFormat df = new SimpleDateFormat("dd MMMM yyyy", locale);
        return df.format(date);
    }

    public static String rusFormatDate(Date date, int dfType) {
        SimpleDateFormat df;
        if (dfType == 1) df = getRusDateFormatter1();
        else df = getRusDateFormatter2();
        return df.format(date);
    }

    public static SimpleDateFormat getRusDateFormatter1() {
        DateFormatSymbols russSymbol = new DateFormatSymbols();
        russSymbol.setMonths(rusMonths1);
        return new SimpleDateFormat("MMMM yyyy", russSymbol);
    }

    public static SimpleDateFormat getRusDateFormatter2() {
        DateFormatSymbols russSymbol = new DateFormatSymbols();
        russSymbol.setMonths(rusMonths2);
        return new SimpleDateFormat("dd MMMM yyyy", russSymbol);
    }

    public static Date parseDate(int year, int month, int day, String separator, SimpleDateFormat df) {
        try {
            return df.parse(year + separator + gap(month, 2) + separator + gap(day, 2));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String formatMoney(double value) {
        return String.format(Locale.ENGLISH, "%.2f", value);
    }
}
