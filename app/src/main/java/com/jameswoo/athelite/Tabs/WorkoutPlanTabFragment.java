package com.jameswoo.athelite.Tabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.jameswoo.athelite.Database.DBHandler;
import com.jameswoo.athelite.Model.Exercise;
import com.jameswoo.athelite.Model.ExerciseSet;
import com.jameswoo.athelite.R;
import com.jameswoo.athelite.Model.WorkoutPlan;
import com.jameswoo.athelite.Adapter.WorkoutPlanAdapter;

import java.util.ArrayList;

public class WorkoutPlanTabFragment extends Fragment {
    private RecyclerView _workoutPlanRecyclerView;
    private RecyclerView.LayoutManager _workoutPlanLayoutManager;
    private WorkoutPlanAdapter _workoutPlanAdapter;
    private DBHandler _db;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public WorkoutPlanTabFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static WorkoutPlanTabFragment newInstance(int sectionNumber) {
        WorkoutPlanTabFragment fragment = new WorkoutPlanTabFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_workout, container, false);

        // Workouts
        _db = new DBHandler(getContext());

        _workoutPlanRecyclerView = (RecyclerView) rootView.findViewById(R.id.workout_recycler_view);
        _workoutPlanLayoutManager = new LinearLayoutManager(getContext());
        _workoutPlanRecyclerView.setLayoutManager(_workoutPlanLayoutManager);
        _workoutPlanAdapter = new WorkoutPlanAdapter(getContext(), _db.getWorkoutPlans());
        _workoutPlanRecyclerView.setAdapter(_workoutPlanAdapter);

        return rootView;
    }

    public void updateWorkoutPlanAdapter() {
        _workoutPlanAdapter.updateWorkoutPlans(_db.getWorkoutPlans());
        _workoutPlanAdapter.notifyDataSetChanged();
    }

    public void createNewWorkout() {
        _workoutPlanAdapter.addWorkoutPlan(_db.createWorkoutPlan());
        _workoutPlanAdapter.notifyDataSetChanged();
    }
}