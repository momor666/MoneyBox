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
import com.example.haroldhibari.moneybox.networking.RequestManager;
import com.example.haroldhibari.moneybox.object.CurrentUser;
import com.example.haroldhibari.moneybox.object.Product;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Shows information about the currently selected product and allows the user to add a fixed amount to it
 */
public class ProductDetailFragment extends Fragment {

    @NonNull CompositeDisposable subscriptions = new CompositeDisposable();
    @Nullable private Unbinder unbinder;
    private boolean updating;
    private int invProdId                           =   -1;

    @BindView(R.id.moneyBox_text)                   TextView    money_box_text;
    @BindView(R.id.add_money_button)                Button      add_money_button;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_detail, container, false);
        unbinder = ButterKnife.bind(this,view);
        CurrentUser.getInstance().setToolbarTitle(CurrentUser.getInstance().getSelectedProduct().getValue().getProductType().toString());
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
        subscriptions.add(CurrentUser.getInstance().getSelectedProduct().observeOn(AndroidSchedulers.mainThread())
        .doOnNext(this::setMoney).subscribe());

        subscriptions.add(CurrentUser.getInstance().getSessionEnded().observeOn(AndroidSchedulers.mainThread())
                .doOnNext(bool ->{
                    if(getFragmentManager() != null){
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, new LoginFragment());
                        fragmentTransaction.commit();
                    }
                }).subscribe());
    }

    @OnClick(R.id.add_money_button)
    public void addMoney(){
        makePaymentToAccount();
    }

    /**
     * Makes a payment with the fixed amount of £10 to the server using a post request. Then calls for an
     * update to the UI if successful
     */
    private void makePaymentToAccount() {
        if(updating || getContext() == null) return;
        updateUI(true);
        int updateAmount = 10;
        subscriptions.add(RequestManager.getInstance().runUpload(getContext(), RequestManager.requests.update,
                String.valueOf(updateAmount), String.valueOf(invProdId))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(success -> {
                    if(!success) LoginFragment.toastError(getContext());
                    else CurrentUser.getInstance().updateSelectedItem();
                })
                .doOnError(e -> {
                    updateUI(false);
                    LoginFragment.toastError(getContext());
                    e.printStackTrace();
                })
                .doOnComplete(()-> updateUI(false)).subscribe());
    }

    /**
     * Updates the {@link #add_money_button}'s visibility depending on if we're currently processing a payment
     * @param processing Boolean representing if we're currently making a payment
     */
    private void updateUI(boolean processing) {
        updating = processing;
        add_money_button.setVisibility(processing ? View.GONE : View.VISIBLE);
    }

    /**
     * Updates the {@link #money_box_text} to display how much is currently stored in the moneybox
     * @param product The product we wish to display its moneybox
     */
    private void setMoney(@NonNull Product product){
        if(getContext() == null) return;
        money_box_text.setText(getString(R.string.your_money, "£" + String.valueOf(product.getMoneyBox())));
        invProdId = product.getInvestorId();
    }
}
