package com.example.haroldhibari.moneybox.jsonhandler;

import android.support.annotation.NonNull;

import com.example.haroldhibari.moneybox.networking.RequestManager;
import com.example.haroldhibari.moneybox.object.CurrentUser;
import com.example.haroldhibari.moneybox.object.Product;
import com.example.haroldhibari.moneybox.object.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Handles the updating of {@link CurrentUser} singleton with the JSON response from the network requests
 */
public class ResponseHandler {

    public enum responseKeys{User, Session, Products, ValidationErrors}

    private enum responseBody{UserId, FirstName, LastName, Email, BearerToken, InvestorProductId,InvestorProductType,
        ProductId, Moneybox, Name, Message}


    /**
     * Saves all user information to the singleton {@link CurrentUser}
     *
     * @param response The JSON response we are parsing to its saving respective functions
     */
    public static void saveUserInfo(@NonNull String response){
        JSONObject data = RequestManager.optJSONObject(response);

        if (data != null){
           saveUser(data);
           saveSession(data);
           saveProducts(data);
        }
    }

    /**
     * Modifies the singleton {@link CurrentUser} with the newly downloaded JSON user data for display
     * within the app
     * @param data The JSON response we extract the user data from
     */
    private static void saveUser(@NonNull JSONObject data){
        JSONObject userData = data.optJSONObject(responseKeys.User.name());

        if(userData != null) {
            //Update the singleton with the currently logged in user
            CurrentUser.getInstance().setCurrentUser(new User(
                    userData.optString(responseBody.UserId.name()),
                    userData.optString(responseBody.FirstName.name()),
                    userData.optString(responseBody.LastName.name()),
                    userData.optString(responseBody.Email.name())));
        }
    }

    /**
     * Modifies the singleton {@link CurrentUser} with the newly downloaded JSON session data to further requests
     * within the app
     * @param data The JSON response we extract the token from
     */
    private static void saveSession(@NonNull JSONObject data){
        JSONObject sessionData = data.optJSONObject(responseKeys.Session.name());

        if(sessionData != null){
            //Update the singleton with the current session token
            CurrentUser.getInstance().setSessionToken(sessionData.optString(responseBody.BearerToken.name()));
        }
    }

    /**
     * Modifies the singleton {@link CurrentUser} with the newly downloaded JSON product data to display and
     * further requests within the app
     * @param data The JSON response we extract the products from from
     */
    private static void saveProducts(@NonNull JSONObject data) {
        JSONArray productData = data.optJSONArray(responseKeys.Products.name());
        ArrayList<Product> products = new ArrayList<>();

        if (productData != null) {
            //append current products into arrayList and then update the singleton
            for (int i = 0; i < productData.length(); i++) {
                try {
                    JSONObject product = productData.getJSONObject(i);
                    products.add(new Product(
                            product.optInt(responseBody.ProductId.name()),
                            product.optInt(responseBody.InvestorProductId.name()),
                            product.optInt(responseBody.Moneybox.name()),
                            product.optString(responseBody.InvestorProductType.name())));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            CurrentUser.getInstance().setCurrentProducts(products.toArray(new Product[products.size()]));
        }
    }
}
