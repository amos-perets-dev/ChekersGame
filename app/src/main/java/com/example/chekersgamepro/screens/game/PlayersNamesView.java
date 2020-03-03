package com.example.chekersgamepro.screens.game;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.example.chekersgamepro.R;
import com.example.chekersgamepro.data.pawn.total.TotalPawnsDataByPlayer;
import com.example.chekersgamepro.views.custom.PlayerNameView;
import com.example.chekersgamepro.views.custom.TotalPawnPlayerView;
import com.google.common.base.Strings;

public class PlayersNamesView {

    private Activity activity;

    private String playerOne;
    private String playerTwo;

    private PlayerNameView playerOneName;
    private PlayerNameView playerTwoName;

    private TotalPawnPlayerView totalPawnsPlayerOne;
    private TotalPawnPlayerView totalPawnsPlayerTwo;

    public PlayersNamesView(Activity activity) {
        this.activity = activity;

        playerOneName = activity.findViewById(R.id.text_player_one);
        playerTwoName = activity.findViewById(R.id.text_player_two);

        totalPawnsPlayerOne = activity.findViewById(R.id.text_player_one_total_pawn);
        totalPawnsPlayerTwo = activity.findViewById(R.id.text_player_two_total_pawn);

        Intent intent = activity.getIntent();
        playerOne = Strings.nullToEmpty(intent.getStringExtra("PLAYER_ONE"));
        playerTwo = Strings.nullToEmpty(intent.getStringExtra("PLAYER_TWO"));

        initPlayersName();
        initTotalPawnsPlayers();
    }

    private void initTotalPawnsPlayers() {

        totalPawnsPlayerOne.setIconPlayerQueen(activity.getDrawable(R.drawable.ic_king_pawn_one));
        totalPawnsPlayerOne.setIconPlayerRegular(activity.getDrawable(R.drawable.ic_pawn_one));

        totalPawnsPlayerTwo.setIconPlayerQueen(activity.getDrawable(R.drawable.ic_king_pawn_two));
        totalPawnsPlayerTwo.setIconPlayerRegular(activity.getDrawable(R.drawable.ic_pawn_two));

        totalPawnsPlayerOne.invalidate();
        totalPawnsPlayerTwo.invalidate();

    }

    private void initPlayersName() {

        setPlayersText();

//        setPlayersIcon();

        setPlayersNameTranslationX();

        setPlayerTurn(true);

        playerOneName.invalidate();
        playerTwoName.invalidate();
    }

    private void setPlayersNameTranslationX() {
        int translationX = activity.getWindowManager().getDefaultDisplay().getWidth() / 2;

        playerOneName.setTranslationX(-translationX);
        playerTwoName.setTranslationX(-translationX);

        totalPawnsPlayerOne.setTranslationX(translationX);
        totalPawnsPlayerTwo.setTranslationX(translationX);
    }

//    private void setPlayersIcon() {
//        playerOneName.setIcon(activity.getDrawable(R.drawable.ic_pawn_one));
//        playerTwoName.setIcon(activity.getDrawable(R.drawable.ic_pawn_two));
//    }

    private void setPlayersText() {
        playerOneName.setText(playerOne);
        playerTwoName.setText(playerTwo);
    }

    public void setPlayerTurn(boolean isPlayerOneTurn) {
        if (isPlayerOneTurn) {
            playerOneName.setIsTurn(true);
            playerTwoName.setIsTurn(false);
        } else {
            playerTwoName.setIsTurn(true);
            playerOneName.setIsTurn(false);
        }
    }

    public void showViewsWithAnimate() {
        animateTextView(playerOneName);
        animateTextView(playerTwoName);

        animateTextView(totalPawnsPlayerOne);
        animateTextView(totalPawnsPlayerTwo);
    }

    private void animateTextView(View view) {
        view.animate().withLayer().withStartAction(() -> view.setAlpha(1)).translationX(0).setDuration(500).start();
    }

    public void setData(TotalPawnsDataByPlayer totalPawnsDataByPlayer) {
        totalPawnsPlayerOne.setTextTotalPawns(
                totalPawnsDataByPlayer.getRegularPawnsPlayerOne(), totalPawnsDataByPlayer.getQueenPawnsPlayerOne());
        totalPawnsPlayerTwo.setTextTotalPawns(
                totalPawnsDataByPlayer.getRegularPawnsPlayerTwo(), totalPawnsDataByPlayer.getQueenPawnsPlayerTwo());
    }
}
