package sharma.gq.whatsup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import sharma.gq.whatsup.databinding.ActivityProfileBinding;

public class Profile extends AppCompatActivity {

    ActivityProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        getSupportActionBar().hide();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating Profile");
        dialog.setCancelable(false);
        dialog.show();

        binding.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 45);
            }
        });
        binding.prosetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = binding.nameBox.getText().toString();

                if(name.isEmpty()){
                    binding.nameBox.setError("Please Enter Your Name!!");
                }else{
                  if(selectedImage !=null){
                      StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());
                      reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                          @Override
                          public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                         if (task.isSuccessful()){
                             reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                 @Override
                                 public void onSuccess(Uri uri) {
                                     String imgUri = uri.toString();
                                     String uid = auth.getUid();
                                     String phone = auth.getCurrentUser().getPhoneNumber();
                                     String name = binding.nameBox.getText().toString();
                                     User user = new User(uid, name, phone, imgUri);

                                     database.getReference()
                                             .child("users")
                                             .child(uid)
                                             .setValue(user)
                                             .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                 @Override
                                                 public void onSuccess(Void unused) {
                                                     dialog.dismiss();
                                                     Intent intent = new Intent(Profile.this, MainActivity.class);
                                                     startActivity(intent);
                                                     finish();
                                                 }
                                             });
                                 }
                             });
                         }
                          }
                      });
                  }
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data !=null){
            if (data.getData() !=null){
                binding.profile.setImageURI(data.getData());
                selectedImage = data.getData();
            }
        }
    }
}