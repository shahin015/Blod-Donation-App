package com.shahin.bda.Adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.shahin.bda.Email.JavaMailApi;
import com.shahin.bda.MainActivity;
import com.shahin.bda.Model.User;
import com.shahin.bda.R;

import java.net.URI;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter  extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    private Context context;
    private List<User> userList;
    String forcall;
    private final int REQUST_PHONE=1;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.user_displayed_layout, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
         final  User user = userList.get(position);

         holder.type.setText(user.getType());

         if (user.getType().equals("donor")){
            holder.emailNow.setVisibility(View.VISIBLE);
         }

         holder.userEmail.setText(user.getEmail());
         holder.phoneNumber.setText(user.getPhonenumber());
         holder.userName.setText(user.getName());
         holder.bloodGroup.setText(user.getBloodgroup());
         holder.callnow.setText("Call Now");
         String phonenamber=holder.phoneNumber.getText().toString();
         forcall=String.valueOf(phonenamber);
         String name=holder.userName.getText().toString();


         
         holder.callnow.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {



                 if (phonenamber.trim().length()>10){
                     Toast.makeText(context.getApplicationContext(), name+" কে কল করা হচ্ছে", Toast.LENGTH_SHORT).show();
                     
                     ParmitionClass();
                 }else {
                     Toast.makeText(context.getApplicationContext(), phonenamber+" This Phone NO Invalid", Toast.LENGTH_SHORT).show();
                 }


             }
         });
         

        Glide.with(context).load(user.getProfilepictureurl()).into(holder.userProfileImage);

        final String nameOfTheReceiver = user.getName();
        final String idOfTheReceiver = user.getId();

        //sending the email

        holder.emailNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle("SEND EMAIL")
                        .setMessage("Send email to " + user.getName() + "?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                .child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                reference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String nameOfSender = snapshot.child("name").getValue().toString();
                                        String email = snapshot.child("email").getValue().toString();
                                        String phone = snapshot.child("phonenumber").getValue().toString();
                                        String  blood = snapshot.child("bloodgroup").getValue().toString();

                                        String mEmail = user.getEmail();
                                        String mSubject = "BLOOD DONATION";
                                        String mMessage = "Hello "+ nameOfTheReceiver+", "+nameOfSender+
                                                " would like blood donation from you. Here's his/her details:\n"
                                                +"Name: "+nameOfSender+ "\n"+
                                                "Phone Number: "+phone+ "\n"+
                                                "Email: " +email+"\n"+
                                                "Blood Group: "+blood+ "\n"+
                                                "Kindly Reach out to him/her. Thank you!\n"
                                                +"BLOOD DONATION APP - DONATE BLOOD, SAVE LIVES!";

                                        JavaMailApi javaMailApi = new JavaMailApi(context, mEmail, mSubject, mMessage);
                                        javaMailApi.execute();

                                        DatabaseReference senderRef = FirebaseDatabase.getInstance().getReference("emails")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        senderRef.child(idOfTheReceiver).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                               if (task.isSuccessful()){
                                                   DatabaseReference receiverRef = FirebaseDatabase.getInstance().getReference("emails")
                                                           .child(idOfTheReceiver);
                                                   receiverRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);

                                                   addNotifications(idOfTheReceiver, FirebaseAuth.getInstance().getCurrentUser().getUid());
                                               }
                                            }
                                        });


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });


    }



    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView userProfileImage;
        public TextView type, userName, userEmail, phoneNumber, bloodGroup;
        public Button emailNow,callnow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userProfileImage  = itemView.findViewById(R.id.userProfileImage);
            type = itemView.findViewById(R.id.type);
            userName   = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            phoneNumber = itemView.findViewById(R.id.phoneNumber);
            bloodGroup = itemView.findViewById(R.id.bloodGroup);
            emailNow = itemView.findViewById(R.id.emailNow);
            callnow=itemView.findViewById(R.id.calllNow);

        }
    }

    private void addNotifications(String receiverId, String senderId){
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference().child("notifications").child(receiverId);
        String date = DateFormat.getDateInstance().format(new Date());
        HashMap<String, Object>  hashMap = new HashMap<>();
        hashMap.put("receiverId", receiverId);
        hashMap.put("senderId", senderId);
        hashMap.put("text", "Sent you an email, kindly check it out!");
        hashMap.put("date", date);

        reference.push().setValue(hashMap);
    }

    private void ParmitionClass() {


        Dexter.withContext(context.getApplicationContext()).withPermission(Manifest.permission.CALL_PHONE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {


                        String dail="tel:"+forcall;
                        context.startActivity(new Intent(Intent.ACTION_CALL,Uri.parse(dail)));
                                            }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                        permissionToken.continuePermissionRequest();
                    }
                }).check();

    }


}
