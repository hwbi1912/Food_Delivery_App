package com.example.food_delivery_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.food_delivery_app.Common.Common;
import com.example.food_delivery_app.Model.Order;
import com.example.food_delivery_app.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

import static com.example.food_delivery_app.Common.Common.convertCodeToStatus;

public class OrderStatus extends AppCompatActivity {

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Order, OrderViewHolder> adapter;
    FirebaseDatabase database;
    DatabaseReference requests;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if (getIntent().getExtras() == null)
            loadOrders(Common.currentUser.getPhone());
        else
            loadOrders(Objects.requireNonNull(getIntent()).getStringExtra("userPhone"));

        overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_left);
    }

    // Bước 8.2: Hệ thống trả dữ liệu và hiển thị thông tin đơn hàng lên trang Đơn hàng
    private void loadOrders(String phone) {
        adapter = new FirebaseRecyclerAdapter<Order, OrderViewHolder>(
                Order.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests.orderByChild("phone").equalTo(phone)
        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder orderViewHolder, Order order, int i) {
                orderViewHolder.txtOrderId.setText(adapter.getRef(i).getKey());
                orderViewHolder.txtOrderStatus.setText(convertCodeToStatus(order.getStatus()));
                orderViewHolder.txtOrderAddress.setText(order.getAddress());
                orderViewHolder.txtOrderPhone.setText(order.getPhone());
                double d = Double.parseDouble(order.getDistance());
                double price = (d-10)*5.0;
                Locale locale = new Locale("vi", "VN");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                orderViewHolder.txtOrderDistance.setText(order.getTotal() + " + " + fmt.format(price));
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_right);
    }
}
