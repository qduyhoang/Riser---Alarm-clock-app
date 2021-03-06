package com.example.hello.alarm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StoreFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mStaggeredGridLayoutManager;
    private ArrayList<StoreItem> storeList;

    private DatabaseReference databaseRef;

    boolean sleepAnalysisIsOwned = false;
    boolean challenge_friend = false;


    public StoreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        sleepAnalysisIsOwned = getArguments().getBoolean("sleep_analysis");
        challenge_friend = getArguments().getBoolean("challenge_friend");

        //Initialize store list
        storeList = new ArrayList<>();
        storeList.add(new StoreItem(R.drawable.ic_sleeptracker, "Sleep Tracker", "Get access to analytical data that tracks and improve your sleep", 500, sleepAnalysisIsOwned));
        storeList.add(new StoreItem(R.drawable.ic_group_button, "Social Feature", "Challenge people and win points. Wake up early and be the winner", 500, challenge_friend));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.store_fragment_layout, container, false);

        //Prepare and set up recycler view for store items
        mRecyclerView = rootView.findViewById(R.id.items_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // use a layout manager
        mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mStaggeredGridLayoutManager);

        // specify an adapter
        mAdapter = new StoreItemsAdapter(storeList);
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }



    public class StoreItemsAdapter extends RecyclerView.Adapter<StoreItemsAdapter.ViewHolder> {
        private ArrayList<StoreItem> storeList;
        private FirebaseUser currentUser;
        private DatabaseReference myRef;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView itemImage;
            public TextView itemTitle;
            public TextView itemPrice;
            public TextView itemDescription;
            public Button itemPurchaseButton;

            public ViewHolder(LinearLayout parentView) {
                super(parentView);
                this.itemImage = parentView.findViewById(R.id.itemImage);
                this.itemTitle = parentView.findViewById(R.id.itemTitle);
                this.itemPrice = parentView.findViewById(R.id.itemPrice);
                this.itemDescription = parentView.findViewById(R.id.itemDescription);
                this.itemPurchaseButton = parentView.findViewById(R.id.purchaseButton);
            }
        }

        // Provide a suitable constructor
        public StoreItemsAdapter(ArrayList<StoreItem> storeList) {
            this.storeList = storeList;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public StoreItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.store_item, parent, false);

            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            Glide.with(StoreFragment.this)
                    .load(storeList.get(position).drawable)
                    .into(holder.itemImage);

            holder.itemTitle.setText(storeList.get(position).title);
            holder.itemPrice.setText(String.valueOf(storeList.get(position).price));
            holder.itemDescription.setText(storeList.get(position).description);

            //Check if item is owned
            if (storeList.get(position).isOwned){
                holder.itemPurchaseButton.setText("Activated");
                holder.itemPurchaseButton.setEnabled(false);
            }


            //Get database reference to user's item list
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            databaseRef = FirebaseDatabase.getInstance().getReference();
            if (currentUser != null){
                myRef = databaseRef.child(currentUser.getUid());
                holder.itemPurchaseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //CHeck if user has enough point
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //Build an alert
                                AlertDialog.Builder builder;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
                                } else {
                                    builder = new AlertDialog.Builder(getContext());
                                }

                                final User user = dataSnapshot.getValue(User.class);
                                int userPoint = user.getPoint();
                                int itemPrice = storeList.get(position).price;
                                if (userPoint < itemPrice){
                                    builder.setTitle("Not enough point")
                                            .setMessage("You currently only have " + userPoint + " point, sleep to earn more point and come back!")
                                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // do nothing
                                                }
                                            })
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .show();
                                }
                                else {
                                    builder.setTitle("Confirm")
                                            .setMessage("This item costs " + itemPrice + " point, are you sure you want to buy this?")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //Update item on the database
                                                    String itemName = holder.itemTitle.getText().toString();
                                                    user.addItem(itemName, 1);
                                                    myRef.setValue(user);

                                                    Toast.makeText(getContext(), "You have successfully unlocked "+ itemName, Toast.LENGTH_SHORT).show();
                                                    MainActivity.incrementPointAndSaveToDb(getContext(), currentUser, false, storeList.get(position).price);
                                                    //Restart application
                                                    getActivity().finish();
                                                    MainActivity.restartActivity(getContext());
                                                }
                                            })
                                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // do nothing
                                                }
                                            })
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return storeList.size();
        }
    }
}
