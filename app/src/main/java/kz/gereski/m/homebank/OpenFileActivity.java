package kz.gereski.m.homebank;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import kz.gereski.m.homebank.util.SimpleFileDialog;


public class OpenFileActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_file);

        final EditText etFilename = (EditText) findViewById(R.id.etOFilename);
        etFilename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFile((EditText) v);
            }
        });

        Button btOpen = (Button) findViewById(R.id.btOpen);
        btOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    openFile(etFilename.getText().toString());
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_open_file, menu);
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

    private void openFile(String file) throws IOException, ParseException {

        DBHelper dbHelper = new DBHelper(this);
        dbHelper.getExportingData();

        StringBuilder fileContent = new StringBuilder("");
        File f = new File(file);
        InputStream inStream = new FileInputStream(f);
        byte[] buffer = new byte[1024];
        int n;
        while ((n = inStream.read(buffer)) != -1) {
            fileContent.append(new String(buffer, 0, n));
        }
        inStream.close();

        boolean res = dbHelper.setImportedData(fileContent.toString());
        if (res == true)
            Toast.makeText(this, getResources().getString(R.string.loaded), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
    }

    private void chooseFile(final EditText etFilename) {

        SimpleFileDialog fileOpenDialog = new SimpleFileDialog(
                OpenFileActivity.this,
                "FileSave..",
                new SimpleFileDialog.SimpleFileDialogListener() {
                    @Override
                    public void onChosenDir(String chosenDir) {
                        // The code in this function will be executed when the dialog OK button is pushed
                        etFilename.setText(chosenDir);
                    }
                }
        );
        fileOpenDialog.default_file_name = etFilename.getText().toString();
        fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);
    }
}
