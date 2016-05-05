package com.jameswoo.athelite.Activity;

import android.app.FragmentManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.jameswoo.athelite.Adapter.ExerciseListAdapter;
import com.jameswoo.athelite.Adapter.ExerciseSetListAdapter;
import com.jameswoo.athelite.Adapter.WorkoutPlanAdapter;
import com.jameswoo.athelite.Database.DBHandler;
import com.jameswoo.athelite.Model.Exercise;
import com.jameswoo.athelite.Model.ExerciseSet;
import com.jameswoo.athelite.Model.WorkoutPlan;
import com.jameswoo.athelite.R;
import com.jameswoo.athelite.Util.JsonSerializer;

import java.util.ArrayList;

public class ViewExercise extends AppCompatActivity {

    private String _workoutPlanExerciseJson;
    private Exercise _exercise;
    private EditText _exerciseName;
    private ExerciseSetListAdapter _adapter;
    private DBHandler _db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_exercise);

        initToolbar();
        initExercise();
        initInstances();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.back_to_exercises_page);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    void initInstances() {
        // Create the adapter to convert the array to views
        _db = new DBHandler(this);
        _adapter = new ExerciseSetListAdapter(this, _exercise.getExerciseSets());
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.set_list_view);
        listView.setAdapter(_adapter);

        FloatingActionButton fabAdd = (FloatingActionButton) findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Added new set", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                addExerciseSet();
            }
        });

        FloatingActionButton fabRemove = (FloatingActionButton) findViewById(R.id.fab_remove);
        fabRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Removed last set", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                _db.deleteExerciseSet(_adapter.getExerciseSets().get(_adapter.getExerciseSets().size() - 1));
                _adapter.removeLastExerciseSet();
                _adapter.notifyDataSetChanged();
            }
        });
    }

    void initExercise() {
        Intent intent = getIntent();
        _workoutPlanExerciseJson = intent.getStringExtra(ExerciseListAdapter.WORKOUT_EXERCISE);
        _exercise = JsonSerializer.getExerciseFromJson(_workoutPlanExerciseJson);

        _exerciseName = (EditText) findViewById(R.id.edit_exercise_name);
        _exerciseName.setSelectAllOnFocus(true);
        if(_exercise.getExerciseName() != null) {
            _exerciseName.setText(_exercise.getExerciseName());
        } else {
            _exerciseName.setText(R.string.new_exercise);
        }
    }

    private void addExerciseSet() {
        ExerciseSet newSet = _db.createExerciseSetForExercise(_db.getWritableDatabase(), _exercise, getNextSetNumber());
        _adapter.addExerciseSet(newSet);
        _adapter.notifyDataSetChanged();
    }

    private int getNextSetNumber() {
        return _adapter.getExerciseSets().size() + 1;
    }

    public void updateExercise() {
        _exercise.setExerciseName(_exerciseName.getText().toString());
        _exercise.setExerciseSets(_adapter.getExerciseSets());
        _db.updateExercise(_exercise);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        updateExercise();

        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        }

        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

}