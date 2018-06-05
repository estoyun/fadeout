package kr.co.company.fadeoutsubstudy.chat;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import kr.co.company.fadeoutsubstudy.R;
import kr.co.company.fadeoutsubstudy.model.ChatModel;
import kr.co.company.fadeoutsubstudy.model.UserModel;

public class MessageActivity extends AppCompatActivity {
    private final int EDITTEXT_LENGTH = 20; //입력 길이 제한(실시간으로 사라지는 채팅들의 가독성을 위해)

    private final int OUT_TIME = 5000; //5초 후에 메시지 사라짐

    private String destinationUid;
    private String uid;
    private String chatRoomUid;

    private Button button;
    private EditText editText;

    private RecyclerView recyclerView;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");


    private UserModel destinationUserModel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();//채팅을 요구하는 아이디(단말기에 로그인 된 uid)
        destinationUid = getIntent().getStringExtra("destinationUid"); //채팅을 당하는 아이디
        button = (Button) findViewById(R.id.messageActivity_button);
        editText = (EditText) findViewById(R.id.messageActivity_editText);


        recyclerView = (RecyclerView) findViewById(R.id.messageActivity_recylerView);

        //버튼을 누르면 대화방 만들기
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatModel chatModel = new ChatModel();
                chatModel.users.put(uid, true);
                chatModel.users.put(destinationUid, true);

                if (chatRoomUid == null) {
                    //send 버튼 여러번 순간적으로 빠르게 눌렀을때 1번만 DB에 입력하도록
                    button.setEnabled(false);

                    //데이터베이스로 넣기(push()를 이용 primary하게 채팅방이 독립적으로 쌓이도록)
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            checkChatRoom();
                            }
                    });

                    } else {
                    ChatModel.Comment comment = new ChatModel.Comment();
                    comment.uid = uid;
                    comment.message = editText.getText().toString();
                    comment.timestamp = ServerValue.TIMESTAMP;
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            editText.setText(""); //채팅의 입력 값 초기화
                        }
                    });

                }


            }
        });

        checkChatRoom();

    }
        //채팅방 uid로 중복 체크 하기
    void checkChatRoom() {
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/" + uid)
                .equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    //Firebase Realtime Database에서 uid 받아오기
                    ChatModel chatModel = item.getValue(ChatModel.class);

                    if (chatModel.users.containsKey(destinationUid)) {
                        chatRoomUid = item.getKey(); //방 id 받아오기
                        button.setEnabled(true); //다시 입력 받기 위해 순간적으로 1번만 입력되도록 한 것 해제
                        recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                        recyclerView.setAdapter(new RecyclerViewAdapter());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        public Object messageViewHolder;
        List<ChatModel.Comment> comments;
        UserModel userModel; //유저에 대한 정보

        public RecyclerViewAdapter() {
            comments = new ArrayList<>();

            //유저 정보 뱓아오기
            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            destinationUserModel = dataSnapshot.getValue(UserModel.class); //계정 정보 받아오기
                            getMessageList(); //메시지 받아오기(계정 정보 받은 후에 받아야 어색하지 않음)
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

        }

        //메시지 리스트 받아오기
        void getMessageList() {
            //채팅 내용(comments) 받아오기!!
            FirebaseDatabase.getInstance().getReference()
                    .child("chatrooms").child(chatRoomUid).child("comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    comments.clear(); //clear() 처리를 안해주면 대회 내용들이 채팅창에 누적되어 기록됨

                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        comments.add(item.getValue(ChatModel.Comment.class));
                    }

                    //데이터 갱신(메시지가 새로 DB에 올라가면 새로 적을 수 있도록)
                    notifyDataSetChanged();

                    recyclerView.scrollToPosition(comments.size() - 1); //맨 마지막으로 positioning
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);

            return new MessageViewHolder(view);
        }

        //정보들 바인딩 시키기
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final MessageViewHolder messageViewHolder = ((MessageViewHolder) holder);

            //내가 보낸 메시지 출력
            if (comments.get(position).uid.equals(uid)) //현재 계정과 DB의 메시지를 상대방에게 보낸 계정이 동일하면
            {
                messageViewHolder.textView_message.setText(comments.get(position).message); //메시지 내용
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.rightbubble); //메시지 버블
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE); //내가 톡할때는 상대방 메시지 버블 감추기
                messageViewHolder.linearlayout_main.setGravity(Gravity.RIGHT); //채팅 오른쪽 정렬(default는 왼쪽)

                editText.setFilters(new InputFilter[]{
                        new InputFilter.LengthFilter(EDITTEXT_LENGTH)//입력 길이 수 제한
                });

                messageViewHolder.textView_message.setTextSize(25); //글씨 크기


                // 2초간 멈추게 하고싶다면
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        FirebaseDatabase.getInstance().getReference()
                                .child("chatrooms").child(chatRoomUid).child("comments").setValue(null); //방을 나가면 DB의 채팅 내용 사라짐
                    }
                }, OUT_TIME);  // 1000은 1초를 의미합니다.

            }
            //상대방이 보낸 메시지
            else {

                Glide.with(holder.itemView.getContext())
                        .load(destinationUserModel.profileImageUrl)
                        .apply(new RequestOptions().circleCrop())
                        .into(messageViewHolder.imageView_profile); //채팅창에 상대방의 프로필 사진 가져오기
                messageViewHolder.textView_name.setText(destinationUserModel.userName); //상대 계정 내용
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE); //상대방 채팅쪽 보이도록
                messageViewHolder.linearlayout_main.setGravity(Gravity.LEFT); //채팅 왼쪽 정렬(default지만 확실히 하기 위해 gravity 지정)
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.leftbubble); //메시지 버블
                messageViewHolder.textView_message.setText(comments.get(position).message); //메시지 내용
                messageViewHolder.textView_message.setTextSize(25); //글씨 크기

                editText.setFilters(new InputFilter[]{
                        new InputFilter.LengthFilter(EDITTEXT_LENGTH)//입력 길이 수 제한
                });

                // 2초간 멈추게 하고싶다면
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        FirebaseDatabase.getInstance().getReference()
                                .child("chatrooms").child(chatRoomUid).child("comments").setValue(null); //방을 나가면 DB의 채팅 내용 사라짐
                    }
                }, OUT_TIME);  // 1000은 1초를 의미합니다.
            }

            long unixTime = (long) comments.get(position).timestamp;
            Date date = new Date(unixTime);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); //서울의 시간으로 받기
            String time = simpleDateFormat.format(date);
            messageViewHolder.textView_timestamp.setText(time);

        }

        @Override
        public int getItemCount() //메시지 개수 세기
        {
            return comments.size(); //메시지 개수 리턴
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textView_name;
            public ImageView imageView_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearlayout_main;
            public TextView textView_timestamp;

            public MessageViewHolder(View view) {
                super(view);
                //채팅 내용
                textView_message = (TextView) view.findViewById(R.id.messageItem_textView_message);

                //채팅 버블 적용시켜 입히기!!
                textView_name = (TextView) view.findViewById(R.id.messageItem_textview_name);
                imageView_profile = (ImageView) view.findViewById(R.id.messageItem_imageview_profile);
                linearLayout_destination = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_destination);
                linearlayout_main = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_main);
                textView_timestamp = (TextView) view.findViewById(R.id.messageItem_textview_timestamp);
            }
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);

    }


}