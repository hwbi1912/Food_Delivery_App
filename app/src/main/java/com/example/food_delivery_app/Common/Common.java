package com.example.food_delivery_app.Common;
import com.example.food_delivery_app.Model.User;
import com.example.food_delivery_app.Remote.IGoogleService;
import com.example.food_delivery_app.Remote.RetrofitClient;

public class Common {

    public static User currentUser;
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    public static IGoogleService getGoogleMapsAPI() {
        return RetrofitClient.getGoogleClient(GOOGLE_API_URL).create(IGoogleService.class);
    }

    public static String convertCodeToStatus(String status) {
        if(status.equals("0"))
            return "Đang xử lý";
        else if(status.equals("1"))
            return "Đang vận chuyển";
        else
            return "Giao hàng thành công";
    }
}