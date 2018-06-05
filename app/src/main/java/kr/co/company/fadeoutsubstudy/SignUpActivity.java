package kr.co.company.fadeoutsubstudy;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import kr.co.company.fadeoutsubstudy.model.UserModel;

public class SignUpActivity extends AppCompatActivity {

    private static final int PICK_FROM_ALBUM = 10;
    private EditText email;
    private EditText name;
    private EditText password;
    private Button signUp;

    //배경화면 지정
    private String splash_background;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    //프로필 만들기
    private ImageView profile;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //배경화면 설정하기 위한
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        splash_background = mFirebaseRemoteConfig.getString("splash_background");

        //프로필 설정
        profile = (ImageView)findViewById(R.id.signUpActivity_imageView_profile);
        profile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });

        email = (EditText)findViewById(R.id.signUpActivity_editText_email);
        name = (EditText)findViewById(R.id.signUpActivity_editText_name);
        password = (EditText)findViewById(R.id.signUpActivity_editText_password);

        //원격으로 배경화면 제어
        signUp  = (Button)findViewById(R.id.signUpActivity_button_signUp);
        signUp.setBackgroundColor(Color.parseColor(splash_background));

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //회원가입 시 값을 입력하지 않은 경우
                if(email.getText().toString() == null ||
                        name.getText().toString() == null ||
                        password.getText().toString() == null||
                        imageUri == null)
                    return;

                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(SignUpActivity.this,
                                //회원가입이 완료되면
                                new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        final String uid = task.getResult().getUser().getUid(); //uid값(계정의 구분 값)
                                        FirebaseStorage.getInstance().getReference().child("userImages") //프로필 이미지
                                                .child(uid).putFile(imageUri)
                                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                        @SuppressWarnings("VisibleForTests") //에러 없애기
                                                        String imageUrl = task.getResult().getDownloadUrl().toString();

                                                        UserModel userModel = new UserModel();
                                                        userModel.userName = name.getText().toString();
                                                        userModel.profileImageUrl = imageUrl;
                                                        userModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                                        //데이터베이스에 Username 집어 넣기
                                                        FirebaseDatabase.getInstance().getReference().child("users")
                                                                .child(uid).setValue(userModel);
                                                        SignUpActivity.this.finish();

                                                    }
                                                });


                                    }
                                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK)
        {
            profile.setImageURI(data.getData()); //가운데 뷰를 바꾸기
            imageUri = data.getData(); //이미지 경로 원본
        }
    }
}
