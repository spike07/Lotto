package pl.maslanka.lottery;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.sdsmdg.tastytoast.TastyToast;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Artur on 18.10.2016.
 */

public class CashAmount extends AppCompatActivity {

    private static final String STATE_NUM_PICKER = "state_num_picker";
    private static final String TAG_TASK_FRAGMENT = "drawing_cash_amount";
    private NumberPicker numberPicker;
    private ProgressDialog dialog;
    private CashDrawingFragment fragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cash_amount);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button play = (Button) findViewById(R.id.play);
        Logic.setButtonBlueBackground(findViewById(R.id.play));

        dialog = new ProgressDialog(CashAmount.this);
        dialog.setTitle(getResources().getString(R.string.drawing_process));
        dialog.setMessage(getResources().getString(R.string.drawing_hit_and_miss));
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        numberPicker = (NumberPicker) findViewById(R.id.numberPicker);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setMinValue(Logic.BET_COST);
        numberPicker.setMaxValue(Logic.MAX_BET_AMOUNT*Logic.BET_COST);

        if(savedInstanceState == null) {
            fragment = new CashDrawingFragment();
            getSupportFragmentManager().beginTransaction().add(fragment, TAG_TASK_FRAGMENT).commit();
        } else {
            fragment = (CashDrawingFragment) getSupportFragmentManager().findFragmentByTag(TAG_TASK_FRAGMENT);
            numberPicker.setValue(savedInstanceState.getInt(STATE_NUM_PICKER));
            dialog.setMax(numberPicker.getValue()/Logic.BET_COST);
        }

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                drawingCancel();
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                drawingCancel();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fragment != null) {
                    fragment.beginTask(numberPicker.getValue());
                    if (fragment.drawingCashAmount != null && fragment.drawingCashAmount.getStatus()== AsyncTask.Status.RUNNING) {
                        dialog.setMax(numberPicker.getValue()/Logic.BET_COST);
                        dialog.show();
                    }
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (fragment != null) {
            if (fragment.drawingCashAmount != null) {
                if (fragment.drawingCashAmount.getStatus()== AsyncTask.Status.RUNNING) {
                    dialog.show();
                }
                Log.d("OnStart Draw Finished", Boolean.toString(fragment.drawingCashAmount.getStatus() == AsyncTask.Status.FINISHED));
                if (fragment.drawingCashAmount.getStatus() == AsyncTask.Status.FINISHED) {
                    Logic.PREF_ALL_NUMBERS_ENABLED = true;
                    Logic.setAllNumbersArray(null);
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (fragment != null) {
            hideProgressDialog();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(STATE_NUM_PICKER, numberPicker.getValue());
        super.onSaveInstanceState(savedInstanceState);
    }


    public void showProgressDialog() {
        if (fragment.drawingCashAmount != null) {
            if (!dialog.isShowing()) {
                dialog.show();
            }
        }
    }

    public void hideProgressDialog() {
        if (fragment.drawingCashAmount != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    public void updateProgress(Integer progress) {
        dialog.setProgress(progress);
    }

    public void startResultActivity(int betNumber, List<Integer> drawnNumbersToPass, int[] hits06) {
        Intent i = new Intent(getApplicationContext(), GameResultsUserCash.class);
        i.putExtra("bet_amount", betNumber);
        i.putExtra("drawn_numbers_to_pass", (Serializable) drawnNumbersToPass);
        i.putExtra("hits_06", hits06);
        startActivity(i);
    }



    public void showToastOverLimit(int maxAllNumbersSize) {
        if (numberPicker.getValue() > maxAllNumbersSize) {
            TastyToast.makeText(getApplicationContext(), String.format(getResources().getString(R.string.too_much_draws), Logic.numbersFormatter(maxAllNumbersSize)), Toast.LENGTH_LONG, TastyToast.ERROR);
            Logic.PREF_ALL_NUMBERS_ENABLED = false;
        }
    }

    public void drawingCancel() {
        if (fragment != null) {
            if (fragment.drawingCashAmount != null && fragment.drawingCashAmount.getStatus()== AsyncTask.Status.RUNNING) {
                fragment.drawingCashAmount.cancel(true);
            }
        }
    }
}
