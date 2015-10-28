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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;

import kz.gereski.m.homebank.util.SimpleFileDialog;


public class SaveFileActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_file);

        final EditText etFolder = (EditText) findViewById(R.id.etFolder);
        final EditText etFilename = (EditText) findViewById(R.id.etFilename);

        etFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDir((EditText) v);
            }
        });

//        etFolder.setText("/storage/extSdCard/MyHomeEcono");
//        etFilename.setText(new Date().getTime() + ".txt");

        Button btSave = (Button) findViewById(R.id.btSave);
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    saveFile(etFolder.getText().toString() + "/" + etFilename.getText().toString());
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_save_file, menu);
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

    private void saveFile(String file) throws IOException, ParseException {
        File f = new File(file);
        if (!f.exists()) f.createNewFile();

        DBHelper dbHelper = new DBHelper(this);
        String sql = dbHelper.getExportingData();

        byte dataToWrite[] = sql.getBytes("UTF-8");
        OutputStream outStream = new FileOutputStream(f);
        outStream.write(dataToWrite);
        outStream.flush();
        outStream.close();

        Toast.makeText(this, getResources().getString(R.string.saved), Toast.LENGTH_SHORT).show();
    }

    private void chooseDir(final EditText etFolder) {
        SimpleFileDialog fileOpenDialog = new SimpleFileDialog(
                SaveFileActivity.this,
                "FolderChoose",
                new SimpleFileDialog.SimpleFileDialogListener() {
                    @Override
                    public void onChosenDir(String chosenDir) {
                        etFolder.setText(chosenDir);
                    }
                }
        );
        fileOpenDialog.default_file_name = etFolder.getText().toString();
        fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);
    }
}
