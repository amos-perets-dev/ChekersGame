package com.example.chekersgamepro.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chekersgamepro.R;

import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposables;

public class SpinnerView extends Spinner {


    public SpinnerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Create an ArrayAdapter using the string array and a default spinner layout
        int languagesArray = R.array.languages_array;

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), languagesArray, R.layout.language_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        setAdapter(adapter);
        setDefault();
    }

    public void setDefault(){
        String displayLanguage = Locale.getDefault().getDisplayLanguage();

        if (displayLanguage.equals("עברית")){
            setSelection(0);
        } else if (displayLanguage.equals("English")){
            setSelection(1);
        }
    }

    public Observable<String> getSelectedItem() {

        return Observable.create(emitter -> {
            OnItemSelectedListener listener = new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    emitter.onNext(getItemAtPosition(position).toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            };
            setOnItemSelectedListener(listener);
            emitter.setDisposable(Disposables.fromAction(() -> setOnItemSelectedListener(null)));
        });
    }

}
