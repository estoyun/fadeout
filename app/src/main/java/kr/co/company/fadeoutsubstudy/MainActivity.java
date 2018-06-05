package kr.co.company.fadeoutsubstudy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import kr.co.company.fadeoutsubstudy.fragment.ChatFragment;
import kr.co.company.fadeoutsubstudy.fragment.PeopleFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //목록 관리
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.mainActivity_bottomNavigationView);
        getFragmentManager().beginTransaction().replace(R.id.mainActivity_frameLayout,new PeopleFragment()).commit(); //기본은 친구 목록 보기

        final FirebaseAuth mFirebaseAuth = null;

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_people: //친구 목록 보기
                        getFragmentManager().beginTransaction().replace(R.id.mainActivity_frameLayout,new PeopleFragment()).commit();
                        return true;
                    case R.id.action_chat: //채팅 목록 보기
                        getFragmentManager().beginTransaction().replace(R.id.mainActivity_frameLayout,new ChatFragment()).commit();
                        return true;
                    case R.id.action_quit: //앱 종료
                        moveTaskToBack(true);
                        finish();

                }

                return false;
            }
        });
        passPushTokenToServer();

    }
    void passPushTokenToServer(){

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();
        Map<String,Object> map = new HashMap<>();
        map.put("pushToken",token);

        FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map);


    }
}