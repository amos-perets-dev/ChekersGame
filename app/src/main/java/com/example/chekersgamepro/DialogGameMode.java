package com.example.chekersgamepro;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatDialog;

import com.example.chekersgamepro.data_game.DataGame;
import com.jakewharton.rxbinding2.view.RxView;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function3;
import io.reactivex.subjects.PublishSubject;

public class DialogGameMode extends AppCompatDialog {

    private Button buttonPlayerVsPlayerOffline;
    private Button buttonPlayerVsPlayerOnline;
    private Button buttonPlayerVsComputer;

    private PublishSubject<Integer> gameMode = PublishSubject.create();

    public DialogGameMode(Context context) {
        super(context);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        setContentView(R.layout.dialog_choose_players);
        setCanceledOnTouchOutside(false);

        decreaseWindowSize();

        buttonPlayerVsPlayerOffline = findViewById(R.id.one_vs_one_offline);
        buttonPlayerVsPlayerOnline = findViewById(R.id.one_vs_one_online);
        buttonPlayerVsComputer = findViewById(R.id.one_vs_computer);

        setOnClickListener(buttonPlayerVsPlayerOffline);
        setOnClickListener(buttonPlayerVsPlayerOnline);
        setOnClickListener(buttonPlayerVsComputer);
    }

    private void setOnClickListener(View view){
        view.setOnClickListener(v -> gameMode.onNext(v.getId()));
    }

    public Observable<Integer> getGameMode(){
        return gameMode.hide()
                .doOnSubscribe(ignored -> show())
                .map(id -> {

                    switch (id){
                        case R.id.one_vs_one_offline:
                            return DataGame.Mode.OFFLINE_GAME_MODE;

                        case R.id.one_vs_one_online:
                            return DataGame.Mode.ONLINE_GAME_MODE;

                        case R.id.one_vs_computer:
                            return DataGame.Mode.COMPUTER_GAME_MODE;
                    }

                    return -1;
                });
    }

    private Observable<Integer> getClickOnComputerGameMode(){
        return RxView.clicks(buttonPlayerVsComputer)
                .map(new Function<Object, Integer>() {
                    @Override
                    public Integer apply(Object o) throws Exception {
                        return buttonPlayerVsComputer.getId();
                    }
                });
    }


    private Observable<Integer> getClickOnOnlineGameMode(){
        return RxView.clicks(buttonPlayerVsPlayerOnline)
                .map(new Function<Object, Integer>() {
                    @Override
                    public Integer apply(Object o) throws Exception {
                        return buttonPlayerVsPlayerOnline.getId();
                    }
                });
    }

    private Observable<Integer> getClickOnOfflineGameMode(){
        return RxView.clicks(buttonPlayerVsPlayerOffline)
                .map(new Function<Object, Integer>() {
                    @Override
                    public Integer apply(Object o) throws Exception {
                        return buttonPlayerVsPlayerOffline.getId();
                    }
                });
    }

    private void decreaseWindowSize() {
        Window window = getWindow();
        if (window == null) {
            return;
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        DisplayMetrics metrics = window.getContext().getResources().getDisplayMetrics();

        int screenWidth = (int) (metrics.widthPixels * 0.9);

        window.setLayout(screenWidth, WindowManager.LayoutParams.WRAP_CONTENT);
    }
}
