package kz.gereski.m.homebank;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kz.gereski.m.homebank.DaysCalendarPageActivity.RequestCode;
import kz.gereski.m.homebank.util.CalendarHelper;
import kz.gereski.m.homebank.util.Formatter;

import static kz.gereski.m.homebank.DBHelper.ACTION;


public class DayListPageActivity extends Activity {
    private DBHelper dbHelper;
    private EditText et;
    private double total = 0;
    private long groupId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daylist_page);

        CalendarHelper.setScreenSize(getWindowManager());

        Locale locale = getResources().getConfiguration().locale;
        boolean isRusLocale = locale.getLanguage().equals("ru");

        Long d = getIntent().getExtras().getLong("Date");
        Date pd = new Date(d);
        String date = isRusLocale ? Formatter.rusFormatDate(pd, 2) :
                Formatter.formatDate(pd, locale);
        groupId = getIntent().getExtras().getLong("GroupId");

        et = (EditText) findViewById(R.id.edtxtDateOfList);
        et.setText(date);

        final ImageButton btAdd = (ImageButton) findViewById(R.id.btAdd);
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddShoppingClicked(et.getText().toString());
            }
        });

        fillTable(pd, groupId);
    }

    private void fillTable(Date date, final long groupId) {
        total = 0;

        TableLayout table = (TableLayout) findViewById(R.id.tbDayList);
        table.removeAllViews();

        addHeader(table);

        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT);

        dbHelper = new DBHelper(this);
        List<Product> products;
        if (groupId < 0) {
            products = dbHelper.getListOfTheDay(date);
        } else {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.set(Calendar.DAY_OF_MONTH, 1);
            long begin = c.getTime().getTime();
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            long end = c.getTime().getTime();
            products = dbHelper.getListByGroup(begin, end, groupId);
        }
        for (Product product : products) {
            TableRow trow = new TableRow(this);
            trow.setLayoutParams(tableRowParams);
            trow.setPadding(8, 3, 8, 3);

            final TextView tvId = new TextView(this);
            final TextView tvTypeId = new TextView(this);
            final TextView tvTypeName = new TextView(this);
            final TextView tvName = new TextView(this);
            final TextView tvShop = new TextView(this);
            final TextView tvPrice = new TextView(this);
            final TextView tvAmt = new TextView(this);
            final TextView tvProdPrice = new TextView(this);
            ImageView ivDel = new ImageView(this);

            tvTypeName.setGravity(Gravity.LEFT);
            tvName.setGravity(Gravity.LEFT);
            tvShop.setGravity(Gravity.LEFT);
            tvPrice.setGravity(Gravity.RIGHT);
            tvAmt.setGravity(Gravity.RIGHT);
            tvProdPrice.setGravity(Gravity.RIGHT);

            tvTypeName.setPadding(3, 3, 8, 3);
            tvName.setPadding(8, 3, 8, 3);
            tvShop.setPadding(8, 3, 8, 3);
            tvPrice.setPadding(8, 3, 8, 3);
            tvAmt.setPadding(8, 3, 8, 3);
            tvProdPrice.setPadding(8, 3, 8, 3);

            ivDel.setBaselineAlignBottom(true);

            final long id = product.id;
            tvId.setVisibility(View.GONE);
            tvId.setText(String.valueOf(id));

            tvTypeId.setVisibility(View.GONE);
            tvTypeId.setText(String.valueOf(product.group.id));

            double price = product.price;
            double amount = product.amount;
            double res = price * amount;
            total += res;

            tvTypeName.setText(product.group.name);
            tvName.setText(product.name);
            tvShop.setText(product.shop);
            tvPrice.setText(Formatter.formatMoney(price));
            tvAmt.setText(String.valueOf(amount));
            tvProdPrice.setText(Formatter.formatMoney(res));

            trow.addView(tvId);
            trow.addView(tvTypeId);
            trow.addView(tvTypeName);
            trow.addView(tvName);
            trow.addView(tvShop);
            trow.addView(tvPrice);
            trow.addView(tvAmt);
            trow.addView(tvProdPrice);
            trow.addView(ivDel);

            trow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onEditShoppingClicked(
                            et.getText().toString(),
                            Long.parseLong(tvId.getText().toString()),
                            tvName.getText().toString(),
                            tvShop.getText().toString(),
                            tvPrice.getText().toString(),
                            tvTypeName.getText().toString(),
                            tvAmt.getText().toString()
                    );
                }
            });

            ivDel.setImageResource(R.drawable.delete);
            ivDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDelShoppingClicked(id);
                    fillTable(getDateOfList(), groupId);
                }
            });

            table.addView(trow);
        }

        dbHelper.close();

        TextView tvTotal = (TextView) findViewById(R.id.tvCredit);
        tvTotal.setText(getResources().getString(R.string.credit) + ": " + Formatter.formatMoney(total));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_day_list_page, menu);
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

    public void onAddShoppingClicked(String date) {
        Intent intent = new Intent(DayListPageActivity.this, ProductActivity.class);
        intent.putExtra("Date", date);
        intent.putExtra("Id", new Date().getTime());
        intent.putExtra("Action", ACTION.INSERT.ordinal());
        startActivityForResult(intent, RequestCode.ProductActivity.ordinal());
    }

    private void onEditShoppingClicked(String date, long id,
                                       String name, String shop, String price, String group, String amount) {
        Intent intent = new Intent(this, ProductActivity.class);
        intent.putExtra("Date", date);
        intent.putExtra("Id", id);
        intent.putExtra("Name", name);
        intent.putExtra("Shop", shop);
        intent.putExtra("Price", price);
        intent.putExtra("Amount", amount);
        intent.putExtra("Group", group);
        intent.putExtra("Action", ACTION.UPDATE.ordinal());
        startActivityForResult(intent, 0);
    }

    public void onDelShoppingClicked(long id) {
        Product product = new Product();
        product.id = id;
        dbHelper = new DBHelper(this);
        dbHelper.sqlWriteProductToDb(product, ACTION.DELETE);

        Toast.makeText(this, getResources().getString(R.string.deleted), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        fillTable(getDateOfList(), groupId);
    }

    private Date getDateOfList() {
        EditText et = (EditText) findViewById(R.id.edtxtDateOfList);
        String d = et.getText().toString();

        Locale locale = getResources().getConfiguration().locale;
        boolean isRusLocale = locale.getLanguage().equals("ru");
        return Formatter.parseDate(d, isRusLocale ? Formatter.getRusDateFormatter2() :
                new SimpleDateFormat("dd MMMM yyyy", locale));
    }

    private void addHeader(TableLayout table) {
        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT);

        TableRow trow = new TableRow(this);
        trow.setLayoutParams(tableRowParams);
        trow.setPadding(3, 3, 3, 3);

        final TextView tvId = new TextView(this);
        final TextView tvTypeId = new TextView(this);
        final TextView tvTypeName = new TextView(this);
        final TextView tvName = new TextView(this);
        final TextView tvShop = new TextView(this);
        final TextView tvPrice = new TextView(this);
        final TextView tvAmt = new TextView(this);
        final TextView tvProdPrice = new TextView(this);
        final TextView ivDel = new TextView(this);

        tvTypeName.setGravity(Gravity.LEFT);
        tvName.setGravity(Gravity.LEFT);
        tvShop.setGravity(Gravity.LEFT);
        tvPrice.setGravity(Gravity.RIGHT);
        tvAmt.setGravity(Gravity.RIGHT);
        tvProdPrice.setGravity(Gravity.RIGHT);

        tvTypeName.setPadding(8, 3, 8, 3);
        tvName.setPadding(8, 3, 8, 3);
        tvShop.setPadding(8, 3, 8, 3);
        tvPrice.setPadding(8, 3, 8, 3);
        tvAmt.setPadding(8, 3, 8, 3);
        tvProdPrice.setPadding(8, 3, 8, 3);

        tvTypeName.setTextColor(Color.BLACK);
        tvName.setTextColor(Color.BLACK);
        tvShop.setTextColor(Color.BLACK);
        tvPrice.setTextColor(Color.BLACK);
        tvAmt.setTextColor(Color.BLACK);
        tvProdPrice.setTextColor(Color.BLACK);

        tvTypeName.setText(R.string.group);
        tvName.setText(R.string.name);
        tvShop.setText(R.string.shop);
        tvPrice.setText(R.string.price);
        tvAmt.setText(R.string.amount);
        tvProdPrice.setText(R.string.result);
        ivDel.setText("   ");

        trow.setBackgroundColor(Color.LTGRAY);
        tvId.setVisibility(View.GONE);
        tvTypeId.setVisibility(View.GONE);

        trow.addView(tvId);
        trow.addView(tvTypeId);
        trow.addView(tvTypeName);
        trow.addView(tvName);
        trow.addView(tvShop);
        trow.addView(tvPrice);
        trow.addView(tvAmt);
        trow.addView(tvProdPrice);
        trow.addView(ivDel);

        table.addView(trow);
    }
}
