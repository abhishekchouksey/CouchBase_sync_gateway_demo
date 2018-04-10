package com.asw.couchbasegames;

/**
 * Created by Abhishek.Chouksey on 2/2/2018.
 */


public interface OAuthReceiver {

    public static final String DISMISS_ERROR = "dialog_dismissed";
    void receiveLoginAttempted(String redirectURL);
    void receiveOAuthCode(String code);
    public void receiveOAuthError(String error);

}