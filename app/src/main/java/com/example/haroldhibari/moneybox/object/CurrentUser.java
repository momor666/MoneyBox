package com.example.haroldhibari.moneybox.object;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

/**
 * Holds all current user information downloaded from the API. Being a singleton means the information
 * is accessible from all parts of the application
 */
public class CurrentUser {

    @NonNull private ReplaySubject<String> toolBarTitle         =   ReplaySubject.create();
    @NonNull private ReplaySubject<Product> selectedProduct     =   ReplaySubject.create();
    @NonNull private PublishSubject<Boolean> sessionEnded       =   PublishSubject.create();
    @Nullable private static CurrentUser instance               =   null;
    @Nullable private String sessionToken                       =   null;
    @Nullable private User   currentUser                        =   null;
    @NonNull private Product[] currentProducts                  =   new Product[0];

    @NonNull
    public static CurrentUser getInstance(){
        if (instance == null) instance = new CurrentUser();
        return instance;
    }

    /**
     * @return The String Bearer token we make all user specific requests with or an empty string if null
     */
    @NonNull
    public String getSessionToken() {
        return sessionToken != null ? "Bearer " + sessionToken : "";
    }

    /**
     * @return The currently logged in {@link User}, to which we use to display information across the app
     */
    @Nullable
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * @return A boolean representing if a user is currently logged in
     */
    public boolean userLoggedIn(){
        return currentUser != null;
    }

    /**
     * @return The android toolbar we wish to dynamically update its title
     */
    @NonNull
    public ReplaySubject<String> getToolBarTitle() {
        return toolBarTitle;
    }

    /**
     * @return ReplaySubject which when subscribed to, will publish the most recently selected product
     * on the {@link com.example.haroldhibari.moneybox.fragment.AccountFragment} screen
     */
    @NonNull
    public ReplaySubject<Product> getSelectedProduct() {
        return selectedProduct;
    }

    /**
     * @return PublishSubject which will notify the fragment that the session has ended for this user.
     */
    @NonNull
    public PublishSubject<Boolean> getSessionEnded() {
        return sessionEnded;
    }

    /**
     * @return Product of type isa which we publish to listeners when its respective button is selected
     */
    @Nullable
    public Product getIsaProduct() {
        for(Product product : currentProducts) if (product.getProductType() == ProductType.isa) return product;
        return null;
    }

    /**
     * @return Product of type gia which we publish to listeners when its respective button is selected
     */
    @Nullable
    public Product getGiaProduct() {
        for(Product product : currentProducts) if (product.getProductType() == ProductType.gia) return product;
        return null;
    }

    public void setSessionToken(@NonNull String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public void setCurrentUser(@NonNull User currentUser) {
        this.currentUser = currentUser;
    }

    public void setCurrentProducts(@NonNull Product[] products){
       currentProducts = products;
    }

    /**
     * Logs out of app. Will be used when a validation error occurs
     */
    public void logOut(){
        currentUser          =   null;
        sessionToken         =   null;
        currentProducts      =   new Product[0];
        sessionEnded.onNext(true);
    }

    /**
     * Updates the currently selected product with newly downloaded data {@link #selectedProduct}
     * and publishes it to any listeners
     */
    public void updateSelectedItem() {
        Product previousSelected = CurrentUser.getInstance().getSelectedProduct().getValue();

        if (previousSelected != null) {
            for(Product product : currentProducts)
                if (product.getInvestorId() == previousSelected.getInvestorId()) getSelectedProduct().onNext(product);
        }
    }

    /**
     * Flag any listeners that we wish to change the current {@link android.support.v7.widget.Toolbar}'s title
     * @param title The new title we wish to display
     */
    public void setToolbarTitle(@NonNull String title){
        toolBarTitle.onNext(title);
    }

    @Override
    public String toString(){
        return currentUser != null ? currentUser.getName() + ", products: " + currentProducts.length : "";
    }
}
