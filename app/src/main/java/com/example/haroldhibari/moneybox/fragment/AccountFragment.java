package com.example.haroldhibari.moneybox.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.haroldhibari.moneybox.R;
import com.example.haroldhibari.moneybox.object.CurrentUser;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;


public class AccountFragment extends Fragment {

    @NonNull  private static String TAG                       =    AccountFragment.class.getSimpleName();
    @NonNull  private CompositeDisposable subscriptions       =    new CompositeDisposable();
    @Nullable private Unbinder unbinder;

    @BindView(R.id.welcome_text)                    TextView    welcome_text;
    @BindView(R.id.isa_button)                      Button      isa_button;
    @BindView(R.id.gia_button)                      Button      gia_button;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        unbinder = ButterKnife.bind(this, view);
        setTextForScreen();
        CurrentUser.getInstance().setToolbarTitle("Your Accounts");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setSubscriptions();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(unbinder != null) unbinder.unbind();
        subscriptions.clear();
    }

    private void setSubscriptions(){
        subscriptions.add(CurrentUser.getInstance().getSessionEnded().observeOn(AndroidSchedulers.mainThread())
                .doOnNext(bool ->{
                    if(getFragmentManager() != null){
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, new LoginFragment());
                        fragmentTransaction.commit();
                    }
                }).subscribe());
    }

    @OnClick(R.id.isa_button)
    public void publishIsa(){
        if (CurrentUser.getInstance().getIsaProduct() != null)
            //publishes the isa product as the selected product to all listeners in following fragments
            CurrentUser.getInstance().getSelectedProduct().onNext(
                    Objects.requireNonNull(CurrentUser.getInstance().getIsaProduct()));
        moveToProductDetail();
    }

    @OnClick(R.id.gia_button)
    public void publishGia(){
        if (CurrentUser.getInstance().getGiaProduct() != null)
            //publishes the gia product as the selected product to all listeners in following fragments
            CurrentUser.getInstance().getSelectedProduct().onNext(
                    Objects.requireNonNull(CurrentUser.getInstance().getGiaProduct()));
        moveToProductDetail();
    }

    /**
     * Transitions the app to the {@link ProductDetailFragment} adding a tag to the backStack to
     * keep track of this the transition
     */
    private void moveToProductDetail(){
        if (getFragmentManager() != null){
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new ProductDetailFragment());
            fragmentTransaction.addToBackStack(TAG);
            fragmentTransaction.commit();
        }
    }

    /**
     * Sets the full name of the {@link CurrentUser} to be displayed on screen
     */
    private void setTextForScreen(){
        if (getContext() == null || CurrentUser.getInstance().getCurrentUser() == null) return;
        welcome_text.setText(getString(R.string.welcome_text, Objects.requireNonNull(CurrentUser.getInstance().getCurrentUser()).getName()));
    }
}
