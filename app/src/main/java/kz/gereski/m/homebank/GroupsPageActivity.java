package kz.gereski.m.homebank;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.Map;

import kz.gereski.m.homebank.GroupButton.GroupNameProcessor;
import kz.gereski.m.homebank.util.CalendarHelper;
import kz.gereski.m.homebank.util.DBHelper;
import kz.gereski.m.homebank.util.Screen;
import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GroupsPageActivity extends Activity {

    private DBHelper dbHelper;
    private TableLayout.LayoutParams layoutParam
            = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT);
    private GroupsPageActivity context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_list);

        CalendarHelper.setScreenSize(getWindowManager());
        fillGroupsList();
    }

    private void fillGroupsList() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.groups_list);
        dbHelper = new DBHelper(this);
        Map<Long, String> groups = dbHelper.getGroups(dbHelper.getReadableDatabase());
        groups.put(GroupButton.ADD_GROUPNAME_KEY, "");
        for (final Map.Entry<Long, String> g : groups.entrySet()) {
            final int color = dbHelper.getGroupColorByKey(g.getKey(), dbHelper.getReadableDatabase());
            GroupButton gb = new GroupButton(this, g.getKey(), g.getValue(), color, dbHelper) {
                final DBHelper dbh = dbHelper;

                @Override
                public void editGroupName(DBHelper dbHelper) {
                    showInputAlert(g.getValue(), color, new GroupNameProcessor() {
                        @Override
                        public void process(String groupName, int color) {
                            dbh.sqlWriteGroupToDb(g.getKey(), groupName, String.valueOf(color), DBHelper.ACTION.UPDATE);
                            updateGroupsList();
                        }
                    });
                }

                @Override
                public void deleteGroupName(DBHelper dbHelper) {
                    dbHlpr.sqlWriteGroupToDb(id, "", "", DBHelper.ACTION.DELETE);
                    updateGroupsList();
                }

                @Override
                public void addGroupName(DBHelper dbHelper) {
                    final DBHelper dbh = dbHelper;
                    showInputAlert("", color, new GroupNameProcessor() {
                        @Override
                        public void process(String groupName, int color) {
                            dbh.sqlWriteGroupToDb(0, groupName, String.valueOf(color), DBHelper.ACTION.INSERT);
                            updateGroupsList();
                        }
                    });
                }
            };
            layout.addView(gb);
        }
    }

    private void clearGroupsList() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.groups_list);
        layout.removeAllViews();
    }

    private void showInputAlert(String oldName, final int color, final GroupNameProcessor groupNameProcessor) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.rename_group);
        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(layoutParam);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(16, 8, 16, 8);

        TableLayout.LayoutParams txtLayoutParam
                = new TableLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f);
        final EditText input = new EditText(this);
        input.setLayoutParams(txtLayoutParam);
        input.setText(oldName);

        TableLayout.LayoutParams ibLayoutParam
                = new TableLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 0.8f);
        ibLayoutParam.setMargins(16,0,0,8);
        final ImageButton ibColor = new ImageButton(context);
        ibColor.setLayoutParams(ibLayoutParam);
        ibColor.setBackgroundColor(color);
        ibColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPicker(ibColor, color);
            }
        });

        layout.addView(input);
        layout.addView(ibColor);

        b.setView(layout);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                groupNameProcessor.process(input.getText().toString(),
                        ((ColorDrawable) ibColor.getBackground()).getColor());
            }
        });
        b.setNegativeButton(R.string.cancel, null);
        b.create().show();
    }

    private void showColorPicker(final ImageButton button, int color) {
        final AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, color, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                button.setBackgroundColor(color);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // cancel was selected by the user
            }
        });
        dialog.show();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public void updateGroupsList() {
        clearGroupsList();
        fillGroupsList();
    }
}

abstract class GroupButton extends LinearLayout {
    long id;
    String name;
    int color = Color.GREEN;
    Context context;
    DBHelper dbHlpr;
    public static final long ADD_GROUPNAME_KEY = -1L;

    public GroupButton(Context context, long id, String name, int color, DBHelper dbHelper) {
        super(context);
        this.context = context;
        this.id = id;
        this.name = name;
        this.color = color;
        this.dbHlpr = dbHelper;

        init();
    }

    private void init() {
        Screen screen = CalendarHelper.getScreenSize();
        int width = screen.getWidth() / 30;

        setOrientation(LinearLayout.HORIZONTAL);
        TableLayout.LayoutParams txtLayoutParam
                = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(txtLayoutParam);
        setPadding(width, 0, width, 0);

        final TextView tv = new TextView(context);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, width);
        tv.setPadding(16, 8, 8, 8);
        tv.setText(name);
        tv.setBackgroundColor(color);
        tv.setLayoutParams(txtLayoutParam);
        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id == ADD_GROUPNAME_KEY) addGroupName(dbHlpr);
                else editGroupName(dbHlpr);
            }
        });

        ImageButton ibDel = new ImageButton(context);
        ibDel.setScaleType(ImageView.ScaleType.FIT_XY);
        ibDel.setBackgroundColor(Color.TRANSPARENT);
        ibDel.setPadding(8, 16, 8, 8);

        if (id != ADD_GROUPNAME_KEY) {
            ibDel.setImageResource(R.drawable.ic_remove);
            ibDel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteGroupName(dbHlpr);
                }
            });
        } else {
            ibDel.setImageResource(R.drawable.ic_add2);
            tv.setBackgroundColor(Color.TRANSPARENT);
            tv.setText(R.string.group_settings);
            ibDel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    addGroupName(dbHlpr);
                }
            });
        }

        addView(ibDel);
        addView(tv);
    }

    public abstract void editGroupName(DBHelper dbHelper);

    public abstract void deleteGroupName(DBHelper dbHelper);

    public abstract void addGroupName(DBHelper dbHelper);

    interface GroupNameProcessor {
        public void process(String groupName, int color);
    }

    interface OnOkPressed {
        public void updateGroupsList();
    }

}
