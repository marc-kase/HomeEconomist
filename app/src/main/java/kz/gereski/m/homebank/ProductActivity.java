package kz.gereski.m.homebank;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kz.gereski.m.homebank.util.Formatter;


public class ProductActivity extends Activity {
    private DBHelper dbHelper;
    private Product product;
    private Spinner spnGroup = null;
    private Map<Long, String> groups;
    private String selectedGroup = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        dbHelper = new DBHelper(this);

        Locale locale = getResources().getConfiguration().locale;
        boolean isRusLocale = locale.getLanguage().equals("ru");

        String d = getIntent().getExtras().getString("Date");
        SimpleDateFormat df = isRusLocale ? Formatter.getRusDateFormatter2() :
                new SimpleDateFormat("dd MMMM yyyy", locale);
        final Date date = Formatter.parseDate(d, df);

        final long id = getIntent().getExtras().getLong("Id");
        final String name = getIntent().getExtras().getString("Name");
        final String shop = getIntent().getExtras().getString("Shop");
        final String price = getIntent().getExtras().getString("Price");
        final String amount = getIntent().getExtras().getString("Amount");
        final int action = getIntent().getExtras().getInt("Action");
        selectedGroup = getIntent().getExtras().getString("Group");

        EditText etDate = (EditText) findViewById(R.id.etWhen);
        EditText etName = (EditText) findViewById(R.id.etWhat);
        EditText etShop = (EditText) findViewById(R.id.etWhere);
        EditText etPrice = (EditText) findViewById(R.id.etPrice);
        EditText etAmount = (EditText) findViewById(R.id.etAmount);
        spnGroup = (Spinner) findViewById(R.id.spnGroup);

        etDate.setText(isRusLocale ? Formatter.rusFormatDate(date, 2) :
                Formatter.formatDate(date, locale));
        etName.setText(name);
        etShop.setText(shop);
        etPrice.setText(price);
        etAmount.setText(amount);
        fillSpnGroups(spnGroup);

        Button bt = (Button) findViewById(R.id.btAddProduct);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apply(date, id, action);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (spnGroup != null) {
            dropSpinner(spnGroup);
            fillSpnGroups(spnGroup);
        }
    }

    private void apply(Date date, long id, int action) {
        addOrEditShopping(date, id, action);
        setResult(0);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_product, menu);
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

    private void fillSpnGroups(Spinner spinner) {
        groups = dbHelper.getGroups(dbHelper.getReadableDatabase());
        List<String> list = new ArrayList<>(groups.values());

        list.add(getString(R.string.edit_product));
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getSelectedItemPosition() == parent.getCount() - 1) {
                    System.out.println();
                    editGroups();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner.setSelection(dataAdapter.getPosition(selectedGroup));
    }

    private void dropSpinner(Spinner spinner) {
        spinner.setAdapter(null);
    }

    private void addOrEditShopping(Date date, long id, int action) {
        EditText name = (EditText) findViewById(R.id.etWhat);
        EditText shop = (EditText) findViewById(R.id.etWhere);
        EditText price = (EditText) findViewById(R.id.etPrice);
        EditText amt = (EditText) findViewById(R.id.etAmount);

        Spinner sp = (Spinner) findViewById(R.id.spnGroup);
        int idx = sp.getSelectedItemPosition();
        Map.Entry<Long, String> t = (Map.Entry) (groups.entrySet().toArray()[idx]);

        product = new Product();
        product.id = id;
        product.date = date;
        product.name = name.getText().toString();
        product.shop = shop.getText().toString();
        product.price = price.getText().toString().equals("")
                ? 0.0 : Double.parseDouble(price.getText().toString());
        product.amount = amt.getText().toString().equals("")
                ? 0 : Double.parseDouble(amt.getText().toString());
        product.group.id = t.getKey();
        product.group.name = t.getValue();

        dbHelper.sqlWriteProductToDb(product, DBHelper.ACTION.values()[action]);
        dbHelper.close();
    }

    private void editGroups() {
        Intent intent = new Intent(this, GroupsPageActivity.class);
        startActivity(intent);
    }
}
