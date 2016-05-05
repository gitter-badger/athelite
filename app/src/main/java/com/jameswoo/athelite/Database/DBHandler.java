package com.jameswoo.athelite.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jameswoo.athelite.Model.Exercise;
import com.jameswoo.athelite.Model.ExerciseSet;
import com.jameswoo.athelite.Model.WorkoutPlan;

import java.util.ArrayList;


public class DBHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 12;

    // Database Name
    private static final String DATABASE_NAME = "athelite";

    private static final String CREATE_WORKOUTPLANS_TABLE =
            "CREATE TABLE " + DBContract.WorkoutPlanTable.TABLE_NAME + "(" +
                    DBContract.WorkoutPlanTable.COLUMN_ID + " INTEGER PRIMARY KEY," +
                    DBContract.WorkoutPlanTable.COLUMN_NAME + " TEXT," +
                    DBContract.WorkoutPlanTable.COLUMN_TEMPLATE + " INTEGER" + ")";

    private static final String CREATE_EXERCISE_TABLE =
            "CREATE TABLE " + DBContract.ExerciseTable.TABLE_NAME + "(" +
                    DBContract.ExerciseTable.COLUMN_ID + " INTEGER PRIMARY KEY," +
                    DBContract.ExerciseTable.COLUMN_NAME + " TEXT" + ")";

    private static final String CREATE_WORKOUT_EXERCISE_TABLE =
            "CREATE TABLE " + DBContract.WorkoutExerciseTable.TABLE_NAME + "(" +
                    DBContract.WorkoutExerciseTable.COLUMN_ID + " INTEGER PRIMARY KEY," +
                    DBContract.WorkoutExerciseTable.COLUMN_WORKOUT_ID + " INTEGER," +
                    DBContract.WorkoutExerciseTable.COLUMN_EXERCISE_ID + " INTEGER" + ")";

    private static final String CREATE_EXERCISESET_TABLE =
            "CREATE TABLE " + DBContract.ExerciseSetTable.TABLE_NAME + "(" +
                    DBContract.ExerciseSetTable.COLUMN_ID + " INTEGER PRIMARY KEY," +
                    DBContract.ExerciseSetTable.COLUMN_SET_NUMBER + " INTEGER," +
                    DBContract.ExerciseSetTable.COLUMN_WEIGHT + " DOUBLE," +
                    DBContract.ExerciseSetTable.COLUMN_WEIGHT_TYPE + " TEXT," +
                    DBContract.ExerciseSetTable.COLUMN_REPS + " INTEGER," +
                    DBContract.ExerciseSetTable.COLUMN_WORKOUT_EXERCISE_ID + " INTEGER" + ")";

    private static final String CREATE_CALENDAR_TABLE =
            "CREATE TABLE " + DBContract.CalendarTable.TABLE_NAME + "(" +
                    DBContract.CalendarTable.COLUMN_ID + " INTEGER PRIMARY KEY," +
                    DBContract.CalendarTable.COLUMN_DATE + " INTEGER," +
                    DBContract.CalendarTable.COLUMN_WORKOUT_ID + " TEXT" + ")";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_WORKOUTPLANS_TABLE);
        db.execSQL(CREATE_EXERCISE_TABLE);
        db.execSQL(CREATE_WORKOUT_EXERCISE_TABLE);
        db.execSQL(CREATE_EXERCISESET_TABLE);
        db.execSQL(CREATE_CALENDAR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.WorkoutPlanTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.ExerciseTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.WorkoutExerciseTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.ExerciseSetTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.CalendarTable.TABLE_NAME);
        onCreate(db);
    }

    public void deleteDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.WorkoutPlanTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.ExerciseTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.WorkoutExerciseTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.ExerciseSetTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.CalendarTable.TABLE_NAME);
        onCreate(db);
    }

    private class bool {
        public static final int FALSE = 0;
        public static final int TRUE = 1;
    }

    /*****************************************WORKOUTPLANS*****************************************/

    public WorkoutPlan createWorkoutPlan() {
        WorkoutPlan newWorkout = new WorkoutPlan.Builder("New Workout").build();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBContract.WorkoutPlanTable.COLUMN_NAME, newWorkout.getWorkoutPlanName());
        values.put(DBContract.WorkoutPlanTable.COLUMN_TEMPLATE, bool.TRUE);

        long id = db.insert(DBContract.WorkoutPlanTable.TABLE_NAME, null, values);
        newWorkout.setId(id);

        Exercise newExercise = createExerciseForWorkoutPlan(db, newWorkout);
        newWorkout.addExercise(newExercise);

        db.close();
        return newWorkout;
    }

    public ArrayList<WorkoutPlan> getWorkoutPlans() {
        SQLiteDatabase db = this.getWritableDatabase();

        ArrayList<WorkoutPlan> workoutPlans = new ArrayList<>();
        ArrayList<Exercise> exerciseList;

        String query = "SELECT * " +
                        " FROM " + DBContract.WorkoutPlanTable.TABLE_NAME +
                        " WHERE " + DBContract.WorkoutPlanTable.COLUMN_TEMPLATE + " =  \"" + bool.TRUE + "\"";;
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()) {
            do {
                long workoutPlanId = cursor.getLong(0);

                query = "SELECT * " +
                        " FROM " + DBContract.WorkoutExerciseTable.TABLE_NAME +
                        " WHERE " + DBContract.WorkoutExerciseTable.COLUMN_WORKOUT_ID + " =  \"" + workoutPlanId + "\"";

                Cursor wCursor = db.rawQuery(query, null);

                if(wCursor.moveToFirst()) {
                    exerciseList = new ArrayList<>();
                    do {
                        int exerciseId = wCursor.getInt(2);

                        Exercise exercise = readExerciseWithId(db, exerciseId);
                        try {
                            exerciseList.add(exercise);
                        } catch(NullPointerException e) {
                            Log.e("DBHandler", "getExercisesForWorkoutPlan: ", e);
                        }

                    } while(wCursor.moveToNext());

                    WorkoutPlan workoutPlan = new WorkoutPlan.Builder(cursor.getString(1))
                            .workoutPlanId(workoutPlanId)
                            .exercises(exerciseList)
                            .build();
                    workoutPlans.add(workoutPlan);
                }
                wCursor.close();
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return workoutPlans;
    }

    public void updateWorkoutPlan(WorkoutPlan workoutPlan) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String whereClauseWorkoutTable = DBContract.WorkoutPlanTable.COLUMN_ID + " =  ?";
        values.put(DBContract.WorkoutPlanTable.COLUMN_NAME, workoutPlan.getWorkoutPlanName());
        db.update(DBContract.WorkoutPlanTable.TABLE_NAME, values, whereClauseWorkoutTable,
                new String[] { String.valueOf(workoutPlan.getId()) });

        db.close();
    }

    public boolean deleteWorkoutPlan(WorkoutPlan workoutPlan) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean result = false;

        long workoutPlanId = workoutPlan.getId();

        String query = "SELECT * FROM " + DBContract.WorkoutPlanTable.TABLE_NAME +
                        " WHERE " + DBContract.WorkoutPlanTable.COLUMN_ID +
                        " =  \"" + workoutPlanId + "\"";
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()) {

            String weQuery = "SELECT * FROM " + DBContract.WorkoutExerciseTable.TABLE_NAME +
                    " WHERE " + DBContract.WorkoutExerciseTable.COLUMN_WORKOUT_ID +
                    " =  \"" + workoutPlanId + "\"";

            Cursor weCursor = db.rawQuery(weQuery, null);

            if(weCursor.moveToFirst()) {
                do {
                    int workoutExerciseId = weCursor.getInt(0);
                    int exerciseId = weCursor.getInt(2);
                    db.delete(DBContract.ExerciseSetTable.TABLE_NAME,
                            DBContract.ExerciseSetTable.COLUMN_WORKOUT_EXERCISE_ID + " = ?",
                            new String[] { String.valueOf(workoutExerciseId) });
                    db.delete(DBContract.ExerciseTable.TABLE_NAME,
                            DBContract.ExerciseTable.COLUMN_ID + " = ?",
                            new String[] { String.valueOf(exerciseId) });
                } while(weCursor.moveToNext());
            }
            weCursor.close();

            db.delete(DBContract.WorkoutExerciseTable.TABLE_NAME,
                    DBContract.WorkoutExerciseTable.COLUMN_WORKOUT_ID + " = ?",
                    new String[] { String.valueOf(workoutPlanId) });

            db.delete(DBContract.WorkoutPlanTable.TABLE_NAME,
                    DBContract.WorkoutPlanTable.COLUMN_ID + " = ?",
                    new String[] { String.valueOf(workoutPlanId) });

            cursor.close();
            result = true;
        }

        db.close();
        return result;
    }

    /****************************************WORKOUTS**********************************************/

    public WorkoutPlan CreateWorkoutFromPlan(SQLiteDatabase db, WorkoutPlan workoutPlan) {
        ContentValues values = new ContentValues();

        values.put(DBContract.WorkoutPlanTable.COLUMN_NAME, workoutPlan.getWorkoutPlanName());
        values.put(DBContract.WorkoutPlanTable.COLUMN_TEMPLATE, bool.FALSE);

        long id = db.insert(DBContract.WorkoutPlanTable.TABLE_NAME, null, values);
        workoutPlan.setId(id);

        Exercise newExercise = createExerciseForWorkoutPlan(db, workoutPlan);
        workoutPlan.addExercise(newExercise);

        return workoutPlan;
    }


    public WorkoutPlan readWorkout(long workoutId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ArrayList<Exercise> exerciseList;

        String query = "SELECT * " +
                " FROM " + DBContract.WorkoutExerciseTable.TABLE_NAME +
                " WHERE " + DBContract.WorkoutExerciseTable.COLUMN_WORKOUT_ID + " =  \"" + workoutId + "\"";
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()) {
            exerciseList = new ArrayList<>();
            do {
                int exerciseId = cursor.getInt(2);

                Exercise exercise = readExerciseWithId(db, exerciseId);
                try {
                    exerciseList.add(exercise);
                } catch(NullPointerException e) {
                    Log.e("DBHandler", "getExercisesForWorkoutPlan: ", e);
                }

            } while(cursor.moveToNext());
            cursor.close();
            String wQuery = "SELECT * " +
                    " FROM " + DBContract.WorkoutPlanTable.TABLE_NAME +
                    " WHERE " + DBContract.WorkoutPlanTable.COLUMN_ID + " =  \"" + workoutId + "\"" +
                    " AND " + DBContract.WorkoutPlanTable.COLUMN_TEMPLATE +  " =  \"" + bool.FALSE + "\"";
            Cursor wCursor = db.rawQuery(wQuery, null);
            String workoutName = "";
            if(wCursor.moveToFirst()) {
                workoutName = wCursor.getString(1);
            }

            wCursor.close();
            db.close();
            return new WorkoutPlan.Builder(workoutName)
                    .workoutPlanId(workoutId)
                    .exercises(exerciseList)
                    .build();
        }

        cursor.close();
        db.close();
        return null;
    }

    /****************************************EXERCISE**********************************************/

    public Exercise createExerciseForWorkoutPlan(SQLiteDatabase db, WorkoutPlan workoutPlan) {
        long workoutPlanId = workoutPlan.getId();
        Exercise newExercise = new Exercise.Builder("New Exercise").build();

        ContentValues values = new ContentValues();
        values.put(DBContract.ExerciseTable.COLUMN_NAME, newExercise.getExerciseName());
        long id = db.insert(DBContract.ExerciseTable.TABLE_NAME, null, values);
        newExercise.setId(id);
        values.clear();
        values.put(DBContract.WorkoutExerciseTable.COLUMN_WORKOUT_ID, workoutPlanId);
        values.put(DBContract.WorkoutExerciseTable.COLUMN_EXERCISE_ID, id);
        db.insert(DBContract.WorkoutExerciseTable.TABLE_NAME, null, values);

        ArrayList<ExerciseSet> exerciseSets = new ArrayList<>();

        exerciseSets.add(createExerciseSetForExercise(db, newExercise, 1));
        exerciseSets.add(createExerciseSetForExercise(db, newExercise, 2));
        exerciseSets.add(createExerciseSetForExercise(db, newExercise, 3));

        newExercise.setExerciseSets(exerciseSets);

        return newExercise;
    }

    public ArrayList<Exercise> getExercisesForWorkoutPlan(WorkoutPlan workoutPlan) {
        SQLiteDatabase db = this.getWritableDatabase();

        ArrayList<Exercise> exerciseList = new ArrayList<>();

        String query = "SELECT * FROM " + DBContract.WorkoutExerciseTable.TABLE_NAME +
                        " WHERE " + DBContract.WorkoutExerciseTable.COLUMN_WORKOUT_ID +
                        " =  \"" + workoutPlan.getId() + "\"";
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()) {
            do {
                int exerciseId = cursor.getInt(2);

                Exercise exercise = readExerciseWithId(db, exerciseId);
                try {
                    exerciseList.add(exercise);
                } catch(NullPointerException e) {
                    Log.e("DBHandler", "getExercisesForWorkoutPlan: ", e);
                }

            } while(cursor.moveToNext());
        }
        cursor.close();
        return exerciseList;
    }

    private Exercise readExerciseWithId(SQLiteDatabase db, int exerciseId) {
        String query;
        query = "SELECT * " +
                " FROM " + DBContract.ExerciseTable.TABLE_NAME +
                " WHERE " + DBContract.ExerciseTable.COLUMN_ID + " =  \"" + exerciseId + "\"";


        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()) {
            String exerciseName = cursor.getString(1);

            Exercise exercise = new Exercise.Builder(exerciseName)
                    .exerciseId(exerciseId)
                    .exerciseSets(readExerciseSetsWithExerciseId(db, exerciseId))
                    .build();
            cursor.close();
            return exercise;

        }
        cursor.close();
        return null;
    }

    public void updateExercise(Exercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBContract.ExerciseTable.COLUMN_NAME, exercise.getExerciseName());
        String whereClauseExerciseTable = DBContract.ExerciseTable.COLUMN_ID + " =  ?";
        db.update(DBContract.ExerciseTable.TABLE_NAME, values, whereClauseExerciseTable,
                new String[] { String.valueOf(exercise.getId()) });

        long workoutExerciseId = getWorkoutExerciseIdForExerciseSet(exercise.getId(), db);

        String query = "SELECT * FROM " + DBContract.ExerciseSetTable.TABLE_NAME +
                " WHERE " + DBContract.ExerciseSetTable.COLUMN_WORKOUT_EXERCISE_ID +
                " =  \"" + workoutExerciseId + "\"";

        Cursor cursor = db.rawQuery(query, null);

        String where = DBContract.ExerciseSetTable.COLUMN_SET_NUMBER + " =  ? AND " +
                DBContract.ExerciseSetTable.COLUMN_WORKOUT_EXERCISE_ID + " =  ? ";

        if(cursor.moveToFirst()) {
            do {
                int setNumber = cursor.getInt(1);
                ArrayList<ExerciseSet> exerciseSets = exercise.getExerciseSets();
                values.clear();
                values.put(DBContract.ExerciseSetTable.COLUMN_WEIGHT, exerciseSets.get(setNumber - 1).getSetWeight());
                values.put(DBContract.ExerciseSetTable.COLUMN_WEIGHT_TYPE, exerciseSets.get(setNumber - 1).getWeightType());
                values.put(DBContract.ExerciseSetTable.COLUMN_REPS, exerciseSets.get(setNumber - 1).getSetReps());
                db.update(DBContract.ExerciseSetTable.TABLE_NAME, values, where,
                        new String[]{String.valueOf(setNumber), String.valueOf(workoutExerciseId)});
            } while(cursor.moveToNext());
        }
        cursor.close();

        db.close();
    }

    public void deleteExercise(Exercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();

        long exerciseId = exercise.getId();

        String query = "SELECT * FROM " + DBContract.WorkoutExerciseTable.TABLE_NAME +
                " WHERE " + DBContract.WorkoutExerciseTable.COLUMN_EXERCISE_ID +
                " =  \"" + exerciseId + "\"";
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()) {
            do {
                int workoutExerciseId = cursor.getInt(0);
                db.delete(DBContract.ExerciseSetTable.TABLE_NAME,
                        DBContract.ExerciseSetTable.COLUMN_WORKOUT_EXERCISE_ID + " = ?",
                        new String[] { String.valueOf(workoutExerciseId) });
            } while(cursor.moveToNext());
        }

        db.delete(DBContract.ExerciseTable.TABLE_NAME,
                DBContract.ExerciseTable.COLUMN_ID + " = ?",
                new String[] { String.valueOf(exerciseId) });

        db.delete(DBContract.WorkoutExerciseTable.TABLE_NAME,
                DBContract.WorkoutExerciseTable.COLUMN_EXERCISE_ID + " = ?",
                new String[] { String.valueOf(exerciseId) });

        cursor.close();
        db.close();
    }

    /***************************************EXERCISESET********************************************/

    public ExerciseSet createExerciseSetForExercise(SQLiteDatabase db, Exercise exercise, int setNumber) {

        long exerciseId = exercise.getId();
        long workoutExerciseId = getWorkoutExerciseIdForExerciseSet(exerciseId, db);

        ContentValues values = new ContentValues();
        values.put(DBContract.ExerciseSetTable.COLUMN_SET_NUMBER, setNumber);
        values.put(DBContract.ExerciseSetTable.COLUMN_WEIGHT, 0.0);
        values.put(DBContract.ExerciseSetTable.COLUMN_WEIGHT_TYPE, "lb");
        values.put(DBContract.ExerciseSetTable.COLUMN_REPS, 0);
        values.put(DBContract.ExerciseSetTable.COLUMN_WORKOUT_EXERCISE_ID, workoutExerciseId);
        long id = db.insert(DBContract.ExerciseSetTable.TABLE_NAME, null, values);

        return new ExerciseSet(id, setNumber, 0.0, "lb", 0);
    }

    public ArrayList<ExerciseSet> readExerciseSetsWithExerciseId(SQLiteDatabase db, long exerciseId) {
        String query = "SELECT * FROM " + DBContract.WorkoutExerciseTable.TABLE_NAME +
                " WHERE " + DBContract.WorkoutExerciseTable.COLUMN_EXERCISE_ID +
                " =  \"" + exerciseId + "\"";
        Cursor cursor = db.rawQuery(query, null);

        ArrayList<ExerciseSet> exerciseSets = new ArrayList<>();

        long workoutExerciseId;
        if(cursor.moveToFirst()) {
            workoutExerciseId = cursor.getInt(0);
            String esQuery = "SELECT * FROM " + DBContract.ExerciseSetTable.TABLE_NAME +
                    " WHERE " + DBContract.ExerciseSetTable.COLUMN_WORKOUT_EXERCISE_ID +
                    " =  \"" + workoutExerciseId + "\"";
            Cursor esCursor = db.rawQuery(esQuery, null);
            if(esCursor.moveToFirst()) {
                do {
                    ExerciseSet es = new ExerciseSet(   esCursor.getInt(0),
                                                        esCursor.getInt(1),
                                                        esCursor.getDouble(2),
                                                        esCursor.getString(3),
                                                        esCursor.getInt(4)  );
                    exerciseSets.add(es);
                } while(esCursor.moveToNext());
            }
            esCursor.close();
        }
        cursor.close();
        return exerciseSets;
    }

    public boolean deleteExerciseSet(ExerciseSet exerciseSet) {
        SQLiteDatabase db = this.getWritableDatabase();

        long exerciseSetId = exerciseSet.getId();

        int result = db.delete(DBContract.ExerciseSetTable.TABLE_NAME,
                DBContract.WorkoutExerciseTable.COLUMN_ID + " = ?",
                new String[] { String.valueOf(exerciseSetId) });

        db.close();
        return result > 0;
    }

    private long getWorkoutExerciseIdForExerciseSet(long exerciseId, SQLiteDatabase db) {
        String query = "SELECT * FROM " + DBContract.WorkoutExerciseTable.TABLE_NAME +
                " WHERE " + DBContract.WorkoutExerciseTable.COLUMN_EXERCISE_ID +
                " =  \"" + exerciseId + "\"";
        Cursor cursor = db.rawQuery(query, null);
        long workoutExerciseId = 0;
        if(cursor.moveToFirst()) {
            workoutExerciseId = cursor.getInt(0);
        }
        cursor.close();
        return workoutExerciseId;
    }

    /*****************************************CALENDAR*********************************************/

    public void createWorkoutPlanForDateTime(WorkoutPlan workoutPlan, long dateTime) {
        SQLiteDatabase db = this.getWritableDatabase();

        WorkoutPlan copiedWorkoutPlan = CreateWorkoutFromPlan(db, workoutPlan);

        ContentValues values = new ContentValues();
        values.put(DBContract.CalendarTable.COLUMN_DATE, dateTime);
        values.put(DBContract.CalendarTable.COLUMN_WORKOUT_ID, copiedWorkoutPlan.getId());
        db.insert(DBContract.CalendarTable.TABLE_NAME, null, values);

        db.close();
    }

    public WorkoutPlan readWorkoutForDateTime(long dateTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + DBContract.CalendarTable.TABLE_NAME +
                " WHERE " + DBContract.CalendarTable.COLUMN_DATE +
                " =  \"" + dateTime + "\"";
        Cursor cursor = db.rawQuery(query, null);
        long workoutId = 0;
        if(cursor.moveToFirst()) {
            workoutId = cursor.getInt(2);
        }
        cursor.close();
        db.close();
        return readWorkout(workoutId);
    }
}