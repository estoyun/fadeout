package kr.co.company.fadeoutsubstudy;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class LoginActivity extends AppCompatActivity {

    private EditText id;
    private EditText password;

    private Button login;
    private Button signUp;
    private FirebaseRemoteConfig firebaseRemoteConfig; //원격으로 매개변수 제어
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener; //로그인이 되어 있으면 다음 화면으로


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //테마 적용하기(원격으로 받기)
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        String splash_background = firebaseRemoteConfig.getString("splash_background");
        getWindow().setStatusBarColor(Color.parseColor(splash_background)); //원격으로 배경 지정하기

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();

        id = (EditText)findViewById(R.id.loginActivity_editText_id);
        password = (EditText)findViewById(R.id.loginActivity_editText_password);

        login = (Button)findViewById(R.id.loginActivity_button_login);
        signUp = (Button)findViewById(R.id.loginActivity_button_signUp);

        //버튼에 색 칠하기
        login.setBackgroundColor(Color.parseColor(splash_background));
        signUp.setBackgroundColor(Color.parseColor(splash_background));

        //로그인 버튼 클릭 하면!!
        login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                loginEvent();
            }
        });

        //회원가입 버튼 클릭 하면!!
        signUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });

        //로그인 인터페이스 리스너(로그인이 되었는지 확인)
        authStateListener = new FirebaseAuth.AuthStateListener() {
            //로그인이 되었거나 로그아웃이 되었거나(변경되었을때)
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null) //로그인 경우
                {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                }
                else //로그아웃 경우
                {

                }
            }
        };
    }

    void loginEvent()
    {
        firebaseAuth.signInWithEmailAndPassword(id.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    //로그인이 잘 되었는지 판단해주는 곳
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(!task.isSuccessful()) //로그인이 실패했을때
                        {
                            Toast.makeText(LoginActivity.this, "로그인이 실패하였습니다.",
                                    Toast.LENGTH_SHORT).show(); //메세지 띄우기
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }
}
