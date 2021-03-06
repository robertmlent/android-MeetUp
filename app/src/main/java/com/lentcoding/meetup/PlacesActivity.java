package com.lentcoding.meetup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlacesActivity extends AppCompatActivity {
    ListView PlacesListView;
    ArrayAdapter<String> listAdapter;
    List PlacesList, PlaceIdList;
    DBAdapter db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        try {
            String destPath = this.getDatabasePath(DBAdapter.DATABASE_NAME).getParentFile().getAbsolutePath();
            File f = new File(destPath);

            if (!f.exists()) {
                f.mkdirs();
                f.createNewFile();

                CopyDB(this.getAssets().open("databases/meetupDB.sqlite"), new FileOutputStream(destPath + "/meetupDB.sqlite"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        db = new DBAdapter(this);
        try {
            db.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Cursor c = db.getAllPlaces();
        PlacesList = new ArrayList();
        PlaceIdList = new ArrayList();

        if (c.moveToFirst()) {
            do {
                int id = c.getInt(0);
                String nm = c.getString(1);
                PlacesList.add(nm);
                PlaceIdList.add(id);
            } while (c.moveToNext());
        }

        db.close();

        PlacesListView = (ListView) findViewById(R.id.lstPlaces);
        listAdapter = new ArrayAdapter<>(PlacesActivity.this, R.layout.list, PlacesList);
        PlacesListView.setAdapter(listAdapter);
        registerForContextMenu(PlacesListView);

        PlacesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    db.open();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                int idNew = (int) PlaceIdList.get(position);
                db.close();

                Intent i = new Intent(PlacesActivity.this, PlaceDetailActivity.class);
                i.putExtra("id", idNew);
                startActivity(i);
            }
        });
    }

    public void CopyDB(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int length;

        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        inputStream.close();
        outputStream.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_places, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_add:
                Intent i0 = new Intent(PlacesActivity.this, PlaceAddActivity.class);
                startActivity(i0);
                return true;
            case R.id.action_home:
                Intent i1 = new Intent(PlacesActivity.this, HomeActivity.class);
                startActivity(i1);
                return true;
            case R.id.action_exit:
                Intent i2 = new Intent(Intent.ACTION_MAIN);
                i2.addCategory(Intent.CATEGORY_HOME);
                i2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i2.putExtra("LOGOUT", true);
                startActivity(i2);
                return true;

        }

        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        menu.add(0, 0, 0, "Update").setIcon(R.drawable.update);
        menu.add(0, 1, 1, "Delete").setIcon(R.drawable.delete);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int pos = info.position;
        int id = item.getItemId();

        switch (id) {
            case 0:
                try {
                    db.open();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                int idNew = (int) PlaceIdList.get(pos);
                db.close();

                Intent i0 = new Intent(PlacesActivity.this, PlaceUpdateActivity.class);
                i0.putExtra("id", idNew);
                startActivity(i0);
                return true;
            case 1:
                new AlertDialog.Builder(PlacesActivity.this).setTitle("Delete Place?").setMessage("Are you sure you want to delete this Place?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface d, final int i) {
                                try {
                                    db.open();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                                if (db.deletePlace((Integer) PlaceIdList.get(pos))) {
                                    Toast.makeText(PlacesActivity.this, "Place has been deleted", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(PlacesActivity.this, "Delete failed", Toast.LENGTH_LONG).show();
                                }
                                db.close();
                                startActivity(new Intent(PlacesActivity.this, PlacesActivity.class));
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
        }

        return false;
    }
}
