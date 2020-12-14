package com.ursic.nikola.movieticketingsystem;

import android.app.Activity;
import android.app.Application;

public interface IEventManagerActivity {
    void finishFromFragment();
    void makeToast(String toast);
    Application getApplicationFromFragment();
    void hideKeyboard();
}
