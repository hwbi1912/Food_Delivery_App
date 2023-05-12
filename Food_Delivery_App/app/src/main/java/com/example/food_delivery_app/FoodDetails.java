package com.example.food_delivery_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.food_delivery_app.Database.Database;
import com.example.food_delivery_app.Model.Food;
import com.example.food_delivery_app.Model.OrderDetail;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class FoodDetails extends AppCompatActivity {

    TextView food_name, food_price, food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
//    FloatingActionButton floatingActionButton;
    Button btnCart;
    ElegantNumberButton elegantNumberButton;
    String foodId;
    Food currentFood;
    FirebaseDatabase database;
    DatabaseReference foods;

    RadioGroup radioFoodSize;
    RadioButton radioFoodButton;
    String price = "Select item size";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_details);

        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");

        elegantNumberButton = findViewById(R.id.number_button);
        btnCart = findViewById(R.id.btnCart);


        radioFoodSize = findViewById(R.id.food_size);

        // Bước 1.1: Chọn size món ăn
        radioFoodSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioFoodButton = findViewById(checkedId);
                if(radioFoodButton.getText().equals("Small"))
                    price = currentFood.getPrice();
                else if(radioFoodButton.getText().equals("Medium"))
                    price = String.valueOf(Double.parseDouble(currentFood.getPrice())*1.5);
                else
                    price = String.valueOf(Double.parseDouble(currentFood.getPrice())*2);

                getDetailFood(foodId);
            }
        });

        // Bước 2: Thêm món ăn vào giỏ hàng
        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new Database(getBaseContext()).cleanCart();
//                int selectedId = radioFoodSize.getCheckedRadioButtonId();
//                radioFoodButton = findViewById(selectedId);
//                System.out.println(radioFoodButton.getText());
//                if(radioFoodButton.getText().equals("Small"))
//                    price = currentFood.getPrice();
//                else if(radioFoodButton.getText().equals("Medium"))
//                    price = String.valueOf(Integer.parseInt(currentFood.getPrice())*2);
//                else
//                    price = String.valueOf(Integer.parseInt(currentFood.getPrice())*3);

                // Bước 2.1: Hệ thống thêm dữ liệu vào giỏ hàng
                new Database(getBaseContext()).addToCart(new OrderDetail(
                        foodId,
                        currentFood.getName(),
                        elegantNumberButton.getNumber(),
                        price,
                        currentFood.getDiscount()
                ));
                Toast.makeText(FoodDetails.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_right);
            }
        });
        food_description = findViewById(R.id.food_description);
        food_image = findViewById(R.id.img_food);
        food_name = findViewById(R.id.food_name);
        food_price = findViewById(R.id.food_price);

        collapsingToolbarLayout = findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        if(getIntent() != null) {
            foodId = getIntent().getStringExtra("FoodId");
        }

        if(!foodId.isEmpty()) {
            getDetailFood(foodId);
        }
        overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_left);
    }

    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);
                Picasso.with(getBaseContext()).load(currentFood.getImage()).into(food_image);
                collapsingToolbarLayout.setTitle(currentFood.getName());
                if(price.charAt(0)== 'S')
                    food_price.setText(price);
                else
                    food_price.setText(price + " \u20AB");
                food_name.setText(currentFood.getName());
                food_description.setText(currentFood.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_right);
    }
}
