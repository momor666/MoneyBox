package com.example.haroldhibari.moneybox.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.haroldhibari.moneybox.R;
import com.example.haroldhibari.moneybox.fragment.AccountFragment;
import com.example.haroldhibari.moneybox.fragment.LoginFragment;
import com.example.haroldhibari.moneybox.object.CurrentUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Hosts all fragments throughout the application. Also updates the {@link Toolbar} dynamically using
 * Rx
 */
public class LoginActivity extends AppCompatActivity {

    @NonNull CompositeDisposable subscriptions = new CompositeDisposable();

    @BindView(R.id.my_toolbar) Toolbar myToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        setSubscriptions();

        if(CurrentUser.getInstance().userLoggedIn()) swapFragment(new AccountFragment());
        else swapFragment(new LoginFragment());
    }

    @Override
    protected void onStop() {
        super.onStop();
        subscriptions.clear();
        CurrentUser.getInstance().logOut();

    }

    /**
     * Creates all necessary Rx subscriptions cleanly
     */
    private void setSubscriptions(){
        subscriptions.add(CurrentUser.getInstance().getToolBarTitle()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    Log.e("TITLE", s);
                    updateToolbarTitle(s);
                }, Throwable::printStackTrace));
    }

    /***
     * Dynamically update the toolbar title depending on which fragment is currently shown / is on
     * the top of the fragment stack
     * @param title The toolbar heading we wish to display
     */
    private void updateToolbarTitle(@NonNull String title){
        if(myToolbar != null) {
            myToolbar.invalidate();
            myToolbar.setTitle(title);
            myToolbar.setElevation(title.isEmpty() ? 0 : 4);
            myToolbar.setVisibility(title.isEmpty() ? View.GONE : View.VISIBLE);
            setSupportActionBar(myToolbar);
        }
    }

    /***
     * Handles the changing of fragments
     * @param newFragment The new fragment we wish to change to
     */
    public void swapFragment(Fragment newFragment){
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.fragment_container, newFragment);
        t.commit();
    }
}
