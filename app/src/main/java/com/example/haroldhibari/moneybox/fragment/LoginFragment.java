package com.example.haroldhibari.moneybox.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.haroldhibari.moneybox.R;
import com.example.haroldhibari.moneybox.networking.RequestManager;
import com.example.haroldhibari.moneybox.object.CurrentUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Handles user log in
 */
public class LoginFragment extends Fragment {

    @Nullable private Unbinder unbinder;
    @NonNull  private CompositeDisposable subscriptions = new CompositeDisposable();
    private boolean isLoggingIn;

    @BindView(R.id.login_email)         EditText        login_email;
    @BindView(R.id.login_password)      EditText        login_password;
    @BindView(R.id.email_text)          TextView        email_text;
    @BindView(R.id.password_text)       TextView        password_text;
    @BindView(R.id.login_button)        Button          login_button;
    @BindView(R.id.progress_bar)        ProgressBar     progress_bar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_in, container, false);
        unbinder = ButterKnife.bind(this,view);
        CurrentUser.getInstance().setToolbarTitle("");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        login_email.setText("test+env12@moneyboxapp.com");
//        login_password.setText("Money$$box@107");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(unbinder != null) unbinder.unbind();
        subscriptions.clear();
    }

    @OnClick(R.id.login_button)
    public void runLogIn() {
        logIn();
    }

    /**
     * Removes all spaces in the given editText
     *
     * @param et The editText we wish to clean
     * @return String with spaces removed
     */
    @NonNull
    private String clean(@NonNull EditText et) {
        if (et.getText() != null) return et.getText().toString().trim().replace(" ", "");
        return "";
    }

    /**
     * Handles majority of work to be done when the Login button is clicked. First it starts an upload
     * with the current email and password and if successful starts a download of user information.
     *
     * @apiNote  In the case that we are logging in, also hides the UI and prevents multiple clicks of login button.
     *  Re-shows UI when our network requests error out or are completed.
     */
    private void logIn() {
        if (isLoggingIn || getContext() == null) return;
        updateUI(true);
        subscriptions.add(RequestManager.getInstance().runUpload(getContext(), RequestManager.requests.login,
                clean(login_email), clean(login_password))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(loggedIn -> {
                    if(!loggedIn) toastError(getContext());
                    else if (getFragmentManager() != null){
                            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, new AccountFragment());
                            fragmentTransaction.commit();
                    }
                })
                .doOnError(e -> {
                    updateUI(false);
                    toastError(getContext());
                    e.printStackTrace();
                })
                .doOnComplete(()-> updateUI(false)).subscribe());
    }

    /**
     * @param isLoggingIn boolean that updates {@link #isLoggingIn} - which is used to prevent multiple
     *                   login observables - as well as hides the UI
     */
    private void updateUI(boolean isLoggingIn) {
        this.isLoggingIn = isLoggingIn;
        login_email.setVisibility(isLoggingIn    ? View.GONE    : View.VISIBLE);
        login_password.setVisibility(isLoggingIn ? View.GONE    : View.VISIBLE);
        login_button.setVisibility(isLoggingIn   ? View.GONE    : View.VISIBLE);
        email_text.setVisibility(isLoggingIn     ? View.GONE    : View.VISIBLE);
        password_text.setVisibility(isLoggingIn  ? View.GONE    : View.VISIBLE);
        progress_bar.setVisibility(isLoggingIn   ? View.VISIBLE : View.GONE);
    }

    /**
     * Toast message showing user what kind of error has been propagated up by the requests
     */
    public static void toastError(Context context){
        if (context == null) return;
        Toast.makeText(context, R.string.login_failed,Toast.LENGTH_SHORT).show();
    }
}
