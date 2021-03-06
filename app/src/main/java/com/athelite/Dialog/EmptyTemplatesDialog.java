package com.athelite.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.athelite.Database.DBHandler;
import com.athelite.R;

public class EmptyTemplatesDialog extends DialogFragment {

    private DBHandler _db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_fragment_pick_workout, container, false);
        _db = new DBHandler(getActivity());

        return rootView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom)
                .setTitle("No templates found, add a template?")
                .setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                FragmentManager fm = getFragmentManager();
                                _db.createWorkoutPlan();
                                PickWorkout dialogFragment = new PickWorkout();
                                Bundle args = new Bundle();
                                args.putLong("PickWorkout.dateTime", getArguments().getLong("PickWorkout.dateTime"));
                                dialogFragment.setArguments(args);
                                dialogFragment.show(fm, "Select A Template");
                            }
                        }
                )
                .setNegativeButton(R.string.alert_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                getActivity().onBackPressed();
                            }
                        }
                )
                .create();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }
}
