package com.example.food_delivery_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.food_delivery_app.Common.Common;
import com.example.food_delivery_app.Database.Database;
import com.example.food_delivery_app.Model.OrderDetail;
import com.example.food_delivery_app.Model.Order;
import com.example.food_delivery_app.Remote.IGoogleService;
import com.example.food_delivery_app.ViewHolder.CartAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Cart extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
                GoogleApiClient.OnConnectionFailedListener, LocationListener {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    TextView txtTotalPrice;
    Button btnPlaceOrder;
    Button btnClearCart;
    List<OrderDetail> cart = new ArrayList<>();
    CartAdapter adapter;

    // Location
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;
    private static int LOCATION_REQUEST_CODE = 9999;
    private static int PLAY_SERVICES_REQUEST = 9997;

    private int check = 0;
    IGoogleService mGoogleMapServices;
    private static double distancek = 0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        mGoogleMapServices = Common.getGoogleMapsAPI();


        // Runtime permission
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("1");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_REQUEST_CODE);
        }
        else {
            if(checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        // Bước 8.1: Hệ thống lưu dữ liệu đơn hàng vào Database
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        txtTotalPrice = findViewById(R.id.total);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnClearCart = findViewById(R.id.btnClearCart);

        // Bước 7: Nhập địa chỉ giao hàng và chọn phương thức thanh toán
        btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
            }
        });

        btnClearCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCart();
            }
        });
        loadListFood();
        overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_left);
    }

    private void clearCart() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Xóa Giỏ Hàng");
        alertDialog.setMessage("Bạn có chắc xóa toàn bộ giỏ hàng?");

        alertDialog.setPositiveButton("XÓA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                new Database(getBaseContext()).cleanCart();
                Toast.makeText(Cart.this, "Giỏ hàng đã xóa", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        alertDialog.setNegativeButton("HỦY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.show();

    }

    // Bước 2.2: Hệ thống hiển thị dữ liệu lên màn hình
    private void loadListFood() {
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart, this);
        recyclerView.setAdapter(adapter);

        double total = 0;
        for(OrderDetail orderDetail :cart)
            total += ((Double.parseDouble(orderDetail.getPrice())) * (Double.parseDouble(orderDetail.getQuantity())));

        Locale locale = new Locale("vi", "VN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 9999) {
            System.out.println(grantResults[0]);
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("2");
                if (checkPlayServices()) {
                    buildGoogleApiClient();
                    createLocationRequest();
                }
            }
        }
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS) {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_REQUEST).show();
            else {
                Toast.makeText(this, "Thiết bị không được hỗ trợ", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    // Bước 7: Nhập địa chỉ giao hàng và chọn phương thức thanh toán
    @SuppressLint("MissingInflatedId")
    private void showAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Thanh Toán");
        alertDialog.setMessage("Nhập địa chỉ giao hàng: ");

        LayoutInflater inflater = this.getLayoutInflater();
        View order_address = inflater.inflate(R.layout.order_address, null);
        final MaterialEditText edtAddress = order_address.findViewById(R.id.edtaddress);
        alertDialog.setView(order_address);
        alertDialog.setIcon(R.drawable.shopping_cart);
//        alertDialog.setIcon(R.drawable.ic_attach_money_black_24dp);


        final double localLat = 25.5529486;
        final double localLng = 81.8789919;

        RadioButton rdiShipToAddress = order_address.findViewById(R.id.rdiShipToAddress);
        RadioButton rdiHomeAddress = order_address.findViewById(R.id.rdiHomeAddress);
        RadioButton rdiCashOnDelivery = order_address.findViewById(R.id.rdiCashOnDelivery);
        RadioButton rdiPayEWallet = order_address.findViewById(R.id.rdiPayEWAllet);
        RadioButton rdiPayATM = order_address.findViewById(R.id.rdiPayATMCard);

        final Geocoder geocoder;
        geocoder = new Geocoder(this, Locale.getDefault());
        rdiShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked == true) {
                    try {
                        List <Address> addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                        String address = addresses.get(0).getAddressLine(0);
                        edtAddress.setText(address);
                        distancek = distance(localLat, localLng, mLastLocation.getLatitude(), mLastLocation.getLongitude(), "K");
                        System.out.println(distancek);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        rdiHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                edtAddress.setText("TRƯỜNG ĐẠI HỌC NÔNG LÂM TP.HCM");
                distancek = 10.345;
            }
        });

        // Bước 8: Nhấn nút Đặt hàng để hoàn tất quá trình mua hàng
        alertDialog.setPositiveButton("ĐẶT HÀNG", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Order order = new Order(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        edtAddress.getText().toString(),
                        txtTotalPrice.getText().toString(),
                        cart,
                        String.valueOf(distancek)
                );

                requests.child(String.valueOf(System.currentTimeMillis())).setValue(order);

                new Database(getBaseContext()).cleanCart();

                Toast.makeText(Cart.this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show();

                // Bước 9: Màn hình hiển thị trang Đơn hàng
                Intent intent = new Intent(Cart.this, OrderStatus.class);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("HỦY BỎ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null) {
            Log.d("LOCATION", "Your Location : "+ mLastLocation.getLatitude()+ " ," + mLastLocation.getLongitude());
        }
        else {
            Log.d("LOCATION", "Couldn't find Location");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_right);
    }
}
