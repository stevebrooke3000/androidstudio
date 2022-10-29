package ca.eonsound.esm;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class ScoreActivity extends AppCompatActivity {

    /*ListView listViewScore;
    AdapterScore customAdapter;
     */
    //Button btnClear;
    ArrayList<CScore> listScores;
    RecyclerView rvScores;
    CScoresAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        // back action
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        rvScores = findViewById(R.id.listviewScore);

        listScores = Settings.getInstance().getScore();
        adapter = new CScoresAdapter(listScores);
        rvScores.setAdapter(adapter);
        rvScores.setLayoutManager(new LinearLayoutManager(this));

 /*       listViewScore = findViewById(R.id.listviewScore);
        customAdapter = new AdapterScore();
        listViewScore.setAdapter(customAdapter);
        listViewScore.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CScore score = Settings.getInstance().getScore().get(position);
                final Intent intent = new Intent();
                intent.putExtra("Fname", score.strFname);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

  */

        CSwipeToDeleteCallback swipeCallback = new CSwipeToDeleteCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeCallback);
        touchHelper.attachToRecyclerView(rvScores);

        adapter.setOnItemClickListener(new CScoresAdapter.IClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                // TBD return with score file name
                CScore score = Settings.getInstance().getScore().get(position);
                final Intent intent = new Intent();
                intent.putExtra("Fname", score.strFname);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onItemLongClick(int position, View v) {
                //position = position+1;//As we are adding header
                CScore score = listScores.get(position);
                score.vToggleLock();

                CScoresAdapter.ViewHolder viewHolder = (CScoresAdapter.ViewHolder)rvScores.findViewHolderForAdapterPosition(position);
                if (viewHolder != null)
                    viewHolder.vSetLocked(v, score.bIsLocked());

                String strSnack = score.bIsLocked() ? "Score is locked" : "Score is unlocked";
                Snackbar.make(v, strSnack, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onSwipe(final int position/*, View v*/) {
                // store item list
                final CScore score = listScores.get(position);
                if (score.bIsLocked())
                    return;

                // remove item from the list
                listScores.remove(position);
                adapter.notifyItemRemoved(position);
                Snackbar snackbar = Snackbar.make(rvScores, "Score deleted from the list.", Snackbar.LENGTH_LONG);
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                            // delete the file
                            //Object obj = new Object();
                            if (score.strFname != null) {
                                try {
                                    File dir = getFilesDir();
                                    File file = new File(dir, score.strFname);
                                    file.delete();
                                } catch (Exception e) {
                                    System.out.println(e.toString());
                                    System.exit(1);
                                }
                            }
                            Settings.getInstance().vDeleteScore(score);
                        }
                    }
                });

                snackbar.setAction("UNDO",new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // add item in list
                        listScores.add(position, score);
                        adapter.notifyItemInserted(position);
                    }
                });
                snackbar.show();// display snackbar
            }
        });

/*
        btnClear = findViewById(R.id.btnClearScore);
        /* button listener */
/*        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog mAlertDialog;
                AlertDialog.Builder builder = new AlertDialog.Builder(ScoreActivity.this);
                builder.setTitle("Clear all scores");
                builder.setMessage("are you sure?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // OK, clear the scores, update the screen and return
//                        Settings.getInstance().vClearScore();
//                        adapter.notifyDataSetChanged();

                        // delete all score files
                        //File dir = getApplicationContext().getFilesDir();
                        File dir = getFilesDir();
                        if (dir.isDirectory()) {
                            for (int i=listScores.size() - 1; i>=0; i--) {
                                CScore score = listScores.get(i);
                                if ( !score.bIsLocked() ) {
                                    File file = new File(dir, score.strFname);
                                    file.delete();
                                    listScores.remove(i);
                                }
                            };
                            /*
                            String[] listFiles = dir.list();
                            for (int i=0; i<listFiles.length;  i++) {
                                String strFname = listFiles[i];
                                boolean bKeepFile = false;
                                int j;
                                for ( j = 0; j < listScores.size(); j++) {
                                    CScore score = listScores.get(j);
                                    if (score.strFname.equals(strFname)) {
                                        bKeepFile = score.bIsLocked();
                                        break;
                                    }
                                }
                                if ( !bKeepFile ) {
                                    File file = new File(dir, strFname);
                                    file.delete();
                                }
                            }
                            */
/*
                            adapter.notifyDataSetChanged();
                            // TBD clean up
                            /*
                                boolean bLocked = false;
                                for (int j = 0; j < listScores.size(); j++) {
                                    if (score.strFname.equals(listFiles[i])) {
                                        bLocked = score.bIsLocked();
                                        break;
                                    }
                                }

                                if (!bLocked) {
                                    File file = new File(dir, listFiles[i]);
                                    file.delete();
                                }


                            }

                             */
  /*                      }

                        finish();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                mAlertDialog = builder.create();
                mAlertDialog.show();

            }
        });
*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_score, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_delete_unlocked_scores) {
            AlertDialog mAlertDialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(ScoreActivity.this);
            builder.setTitle("Clear all unlocked scores");
            builder.setMessage("are you sure?");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // OK, clear the scores, update the screen and return

                    for (int i = listScores.size() - 1; i >= 0; i--) {
                        CScore score = listScores.get(i);
                        if (score.bIsLocked())
                            continue;

                        if (score.strFname != null) {
                            try {
                                File dir = getFilesDir();
                                File file = new File(dir, score.strFname);
                                file.delete();
                            } catch (Exception e) {
                                System.out.println(e.toString());
                                System.exit(1);
                            }
                        }
                        listScores.remove(i);
                        adapter.notifyItemRemoved(i);
                        Settings.getInstance().vDeleteScore(score);
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });

            mAlertDialog = builder.create();
            mAlertDialog.show();

        }
        //noinspection SimplifiableIfStatement
        else if (id == R.id.action_show_progress) {
            Intent intent = new Intent(this, ActivityProgress.class);
            startActivity(intent);
            return true;
        }
        else if (id == android.R.id.home) {
                this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();

}
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
