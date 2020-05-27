package ru.innim.flutter_login_facebook;

import android.app.Activity;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;

public class MethodCallHandler implements MethodChannel.MethodCallHandler {
    private final static String _LOGIN_METHOD = "logIn";
    private final static String _LOGOUT_METHOD = "logOut";
    private final static String _GET_ACCESS_TOKEN = "getAccessToken";
    private final static String _GET_USER_PROFILE = "getUserProfile";
    private final static String _PERMISSIONS_ARG = "permissions";
    private final static String _GET_SDK_VERSION = "getSdkVersion";
    private final static String _GET_USER_EMAIL = "getUserEmail";

    private final LoginCallback _loginCallback;
    private Activity _activity;

    public MethodCallHandler(LoginCallback loginCallback) {
        _loginCallback = loginCallback;
    }

    public void updateActivity(Activity activity) {
        _activity = activity;
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (_activity != null) {
            switch (call.method) {
                case _LOGIN_METHOD:
                    final List<String> permissions = call.argument(_PERMISSIONS_ARG);
                    logIn(permissions, result);
                    break;
                case _LOGOUT_METHOD:
                    logOut(result);
                    break;
                case _GET_ACCESS_TOKEN:
                    getAccessToken(result);
                    break;
                case _GET_USER_PROFILE:
                    getUserProfile(result);
                    break;
                case _GET_SDK_VERSION:
                    getSdkVersion(result);
                    break;
                case _GET_USER_EMAIL:
                    getUserEmail(result);
                    break;
                default:
                    result.notImplemented();
                    break;
            }
        }
    }

    private void logIn(List<String> permissions, Result result) {
        _loginCallback.addPending(result);
        LoginManager.getInstance().logIn(_activity, permissions);
    }

    private void logOut(Result result) {
        LoginManager.getInstance().logOut();
        result.success(null);
    }

    private void getAccessToken(Result result) {
        final AccessToken token = AccessToken.getCurrentAccessToken();
        result.success(Results.accessToken(token));
    }

    private void getUserProfile(Result result) {
        final Profile profile = Profile.getCurrentProfile();
        result.success(Results.userProfile(profile));
    }

    private void getUserEmail(final Result result) {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            result.success(object.getString("email"));
                        } catch (JSONException e) {
                            result.error(ErrorCode.UNKNOWN, e.getMessage(), null);
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void getSdkVersion(Result result) {
        result.success(FacebookSdk.getSdkVersion());
    }
}
