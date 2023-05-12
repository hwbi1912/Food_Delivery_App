package com.example.food_delivery_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.food_delivery_app.Interface.ItemClickListener;
import com.example.food_delivery_app.Model.Food;
import com.example.food_delivery_app.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FoodList extends AppCompatActivity {

    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;
    String CategoryId = "";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");

        recycler_menu = (RecyclerView) findViewById(R.id.recycler_food);
        recycler_menu.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler_menu.setLayoutManager(layoutManager);

        if(getIntent() != null) {
            CategoryId = getIntent().getStringExtra("CategoryId");
        }

        if(!CategoryId.isEmpty() && CategoryId != null) {
            loadFoodList(CategoryId);
        }
        overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_left);
    }

    private void loadFoodList(String categoryId) {
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder> (Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("menuId").equalTo(categoryId)) {

                @Override
                protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                    viewHolder.foodName.setText(model.getName());
                    Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.foodImage);
                    final Food local = model;
                    viewHolder.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onClick(View view, int position, boolean isLongClick) {
                            Intent foodDetails = new Intent(FoodList.this, FoodDetails.class);
                            foodDetails.putExtra("FoodId", adapter.getRef(position).getKey());
                            startActivity(foodDetails);
                        }
                    });

                }

        };
        recycler_menu.setAdapter(adapter);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_right);
    }
}
