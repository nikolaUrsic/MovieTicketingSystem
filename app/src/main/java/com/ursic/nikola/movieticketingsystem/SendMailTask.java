package com.ursic.nikola.movieticketingsystem;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import Models.Mail;

import static android.support.constraint.Constraints.TAG;

public class SendMailTask extends AsyncTask {

    private Activity sendMailActivity;

    public SendMailTask(Activity activity) {
        sendMailActivity = activity;
    }

    protected void onPreExecute() {
    }

    @Override
    protected Object doInBackground(Object... args) {
        try {
            Mail androidEmail = new Mail(args[0].toString(),
                    args[1].toString(), (List) args[2], args[3].toString(),
                    args[4].toString());
            androidEmail.createEmailMessage();
            androidEmail.sendEmail();
            Log.d(TAG, "Mail poslan");
        } catch (Exception e) {
            Log.d(TAG, "Problem sa slanjem maila: "+e.getMessage() +"  "+e.getCause());
        }
        return null;
    }

    @Override
    public void onProgressUpdate(Object... values) {
    }

    @Override
    public void onPostExecute(Object result) {
    }

}

