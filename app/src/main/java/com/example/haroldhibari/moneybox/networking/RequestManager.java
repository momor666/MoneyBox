package com.example.haroldhibari.moneybox.networking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.haroldhibari.moneybox.jsonhandler.ResponseHandler;
import com.example.haroldhibari.moneybox.object.CurrentUser;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Handles all GET and POST network requests, building of JSON body and any other network necessities
 */
public class RequestManager {

    @NonNull private static final String TAG             =    RequestManager.class.getSimpleName();
    @NonNull private static final String baseUrl         =    "https://api-test00.moneyboxapp.com";
    @NonNull private static String apiVersion            =    "3.0.0";
    @NonNull private static String appVersion            =    "4.11.0";
    @NonNull private static String appId                 =    "3a97b932a9d449c981b595";
    @NonNull private static String APP_ID                =    "AppId";
    @NonNull private static String API_VERSION           =    "ApiVersion";
    @NonNull private static String APP_VERSION           =    "AppVersion";
    @NonNull private static String AUTHORIZATION         =    "Authorization";
    @Nullable private static final MediaType JSON        =    MediaType.parse("application/json; charset=utf-8");
    @NonNull private final OkHttpClient client;
    @Nullable private static RequestManager instance;

    private enum uploadKeys{ Email, Password, Amount, InvestorProductId }

    public enum requests { login, logout, account, update;

        public String getEndPoint() {
            switch (this) {
                case login      :       return "/users/login";
                case logout     :       return "/users/logout";
                case account    :       return "/investorproduct/thisweek";
                case update     :       return "/oneoffpayments";
                default         :       return "";
            }
        }
    }

    private RequestManager(){
        client = new OkHttpClient.Builder()
                .build();
        client.dispatcher().setMaxRequests(15);
    }


    @NonNull
    public synchronized static RequestManager getInstance() {
        if (instance == null) {
            instance = new RequestManager();
        }
        return instance;
    }

    /**
     * Creates an observable which handles the upload request depending on the given key
     *
     * @param context Non-null context we use to check if the device is offline
     * @param request The {@link requests} type we wish to make
     * @param firstKey The first value we'd like to send up in the JSON
     * @param secondKey The second value we'd like to send up in the JSON
     * @return An observable we can subscribe to and update the app accordingly
     */
    @NonNull
    private Observable<String> uploadObs(@NonNull Context context, requests request, @NonNull String firstKey,
                                          @NonNull String secondKey){
        if (isOffline(context)) return Observable.just("offline");

        return Observable.create(observable -> {
              try{
                  JSONObject object = new JSONObject();

                  //create the respective JSON that will be attached to the post request
                  switch(request){
                      case login:   object.put(uploadKeys.Email.name(), firstKey);
                                    object.put(uploadKeys.Password.name(), secondKey);
                          break;
                      case update:  object.put(uploadKeys.Amount.name(), firstKey);
                                    object.put(uploadKeys.InvestorProductId.name(), secondKey);
                          break;
                      default: break;
                  }

                  Response response         = client.newCall(buildPostRequest(request,object.toString())).execute();
                  ResponseBody responseBody = response.body();

                  //if there is nothing in the response, we still assume it is a valid JSON response
                  if (responseBody == null){
                      if (!observable.isDisposed()) {
                          observable.onNext("validResponse");
                          observable.onComplete();
                      }
                      return;
                  }

                  //if there is JSON, then propagate it up to any listeners
                  String responseStr = responseBody.string();
                  if (!observable.isDisposed()) {
                      observable.onNext(responseStr);
                      observable.onComplete();
                  }
              } catch (Exception e){
                    client.connectionPool().evictAll();
                    if (!observable.isDisposed())  observable.onError(e);
              }
        });
    }

    /**
     * Creates an observable which handles the downloading of user data
     *
     * @param context Non-null context we use to check if the device is online
     * @return An observable we can subscribe to and update the app accordingly
     */
    @NonNull
    private Observable<String> downloadObs(@NonNull Context context){
        if (isOffline(context)) return Observable.just("offline");

        return Observable.create(observable -> {
            try{

                Response response         = client.newCall(buildGetRequest()).execute();
                ResponseBody responseBody = response.body();

                //if there is nothing in the response, we still assume it is a valid JSON response
                if (responseBody == null){
                    if (!observable.isDisposed()) {
                        observable.onNext("validResponse");
                        observable.onComplete();
                    }
                    return;
                }

                //if there is JSON, then propagate it up to any listeners
                String responseStr = responseBody.string();
                if (!observable.isDisposed()) {
                    observable.onNext(responseStr);
                    observable.onComplete();
                }
            } catch (Exception e){
                client.connectionPool().evictAll();
                if (!observable.isDisposed())  observable.onError(e);
            }
        });
    }

    /**
     * Combining the observables {@link #uploadObs(Context, requests, String, String)} and {@link #downloadObs(Context)}
     * gives us a full app refresh. This allows for the upload then download of important data. This will sync the app
     * with the API
     *
     * @param context Non-null context we use to check if the device is online
     * @param request The {@link requests}request type we wish to make
     * @param firstKey The first value we'd like to send up in the JSON
     * @param secondKey The second value we'd like to send up in the JSON
     * @return A combined observable which carries out the full app refresh and updates current data in memory
     *
     * @apiNote The reason we use another observable to run the combined observables is due to the networkOnMainThread exception
     * caused when running a network request on a newly created observable
     */
    public Observable<Boolean> runUpload(@NonNull Context context, requests request, @NonNull String firstKey,
                                         @NonNull String secondKey){
        return Observable.create(observable -> uploadObs(context, request, firstKey, secondKey)
                .concatWith(downloadObs(context)
                        .doOnComplete(() ->{    if(CurrentUser.getInstance().getSelectedProduct().getValue() != null){
                            CurrentUser.getInstance().getSelectedProduct().onNext(CurrentUser.getInstance().getSelectedProduct().getValue());
                            Log.e("WORK", String.valueOf(CurrentUser.getInstance().getSelectedProduct().getValue().getMoneyBox()));
                        }
                            Log.e(TAG,CurrentUser.getInstance().toString());
                            if(!observable.isDisposed()) observable.onComplete();
                }))
                .doOnNext(str -> {
                    Log.e(TAG, str);
                    Log.e(TAG, String.valueOf(success(str)));
                    ResponseHandler.saveUserInfo(str);
                    observable.onNext(success(str));
                })
                .doOnError(e -> {
                    if(!observable.isDisposed()) observable.onError(e);
                    e.printStackTrace();
                })
                .subscribeOn(Schedulers.computation()).subscribe());
    }

    /**
     *
     * @param response  This is the response in the form of a JSONObject thanks to {@link #optJSONObject(String)}
     * @return a boolean dictating whether the request we made was successful
     * */
    private static boolean success(@NonNull String response) {
        JSONObject obj = optJSONObject(response);
        if(obj != null && obj.has(ResponseHandler.responseKeys.ValidationErrors.name()) && CurrentUser.getInstance().userLoggedIn())
            CurrentUser.getInstance().logOut();
        return obj != null && !obj.has(ResponseHandler.responseKeys.ValidationErrors.name());
    }

    /**
     *
     * @param response  This is the response body that we hope is valid json
     * @return a valid JSONObject containing the JSON or null
     * */
    @Nullable
    public static JSONObject optJSONObject(@NonNull String response){
        try{
            return new JSONObject(response);
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return  null;
    }


    /**
     * @return A Okhttp3 GET request
     * */
    private Request buildGetRequest(){
        return new Request.Builder().url(baseUrl + requests.account.getEndPoint())
                .addHeader(APP_ID,appId)
                .addHeader(APP_VERSION,appVersion)
                .addHeader(API_VERSION,apiVersion)
                .addHeader(AUTHORIZATION, CurrentUser.getInstance().getSessionToken()).get().build();
    }

    /**
     * @param request The type of request we wish to make
     *
     * @return A Okhttp3 POST request
     * */
    private Request buildPostRequest(requests request, @NonNull String payload){
        RequestBody body = RequestBody.create(JSON, payload);
        String url       = baseUrl + request.getEndPoint();
        Request req;
        req = request == requests.login ?
                new Request.Builder().url(url)
                        .addHeader(APP_ID,appId)
                        .addHeader(APP_VERSION,appVersion)
                        .addHeader(API_VERSION,apiVersion)
                        .post(body).build()
        :       new Request.Builder().url(url)
                        .addHeader(APP_ID,appId)
                        .addHeader(APP_VERSION,appVersion)
                        .addHeader(API_VERSION,apiVersion)
                        .addHeader(AUTHORIZATION, CurrentUser.getInstance().getSessionToken())
                        .post(body).build();
        return req;
    }

    /**
     *
     * @param context   The context so that we can access system services
     * @return          This returns true if the device is offline (no internet access)
     */
    private boolean isOffline(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = null;
        if (cm != null) netInfo = cm.getActiveNetworkInfo();
        return !(netInfo != null && netInfo.isConnectedOrConnecting());
    }
}
