package com.example.chekersgamepro.screens.game.model;

import android.content.Intent;
import android.util.Log;

import com.google.common.base.Optional;

import javax.annotation.Nullable;

public class GameFinishData {

    private Optional<Boolean> isYourWin;
    private boolean isNeedUpdateUserProfile;

    private Intent intentBackToHomePage;

    public GameFinishData(Optional<Boolean> isYourWin, boolean isNeedUpdateUserProfile) {
        this.isYourWin = isYourWin;
        this.isNeedUpdateUserProfile = isNeedUpdateUserProfile;
        setIntent(isYourWin);
    }

    public Optional<Boolean> isYourWin() {
        return isYourWin;
    }

    public String getWinOrLoose() {
        return isYourWin.isPresent() && isYourWin.get() ? "WINNER" : "LOOSER";
    }

    public boolean isNeedUpdateUserProfile() {
        return isNeedUpdateUserProfile;
    }

    public Intent getIntentBackToHomePage() {
        return intentBackToHomePage;
    }

    private void setIntent(Optional<Boolean> isYourWin) {
        intentBackToHomePage = new Intent();
        intentBackToHomePage.putExtra("IS_YOUR_WIN", isYourWin.isPresent() && isYourWin.get());
        intentBackToHomePage.putExtra("IS_NEED_UPDATE_USER_PROFILE", isNeedUpdateUserProfile);
    }
}
