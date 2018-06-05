package kr.co.company.fadeoutsubstudy.fragment;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.List;

import kr.co.company.fadeoutsubstudy.R;
import kr.co.company.fadeoutsubstudy.chat.MessageActivity;
import kr.co.company.fadeoutsubstudy.model.UserModel;

/**
 * Created by Yun on 2018-05-28.
 */
public class PeopleFragment extends Fragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people, container, false);
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.peopleFragment_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new PeopleFragmentRecyclerViewAdapter());

        return view;
    }

    //리사이클러뷰 처리
    class PeopleFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    {

        List<UserModel> userModels;
        public PeopleFragmentRecyclerViewAdapter()
        {
            userModels = new ArrayList<>();

            //현재 사용자의 uid를 가져옴
            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference().child("users")
                    .addValueEventListener(new ValueEventListener() {

                        //서버에서 넘어온 데이터
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //데이터 쌓기
                            userModels.clear(); //누적되는 데이터 삭제

                            for(DataSnapshot snapshot :dataSnapshot.getChildren())
                            {
                                //본인의 계정을 친구목록에서 지우기
                                UserModel userModel = snapshot.getValue(UserModel.class);

                                if(userModel.uid.equals(myUid))
                                    continue;
                                userModels.add(userModel);
                            }
                            //새로고침(데이터 쌓은 걸로 친구 목록 띄우기)
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend,parent,false);

            return new CustomViewHolder(view);
        }

        //이미지 넣기
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

            Glide.with(holder.itemView.getContext())
                    .load(userModels.get(position).profileImageUrl)
                    .apply(new RequestOptions().circleCrop())
                    .into(((CustomViewHolder)holder).imageView);

            ((CustomViewHolder)holder).textView.setText(userModels.get(position).userName);

            //친구 프로필 누르면 채팅창 열기
            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(view.getContext(), MessageActivity.class);
                    intent.putExtra("destinationUid", userModels.get(position).uid);
                    //화면 전환 애니메이션 효과 삽입
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(),
                            R.anim.fromright, R.anim.toleft);
                    startActivity(intent, activityOptions.toBundle());
                }

            });

        }

        @Override
        public int getItemCount()
        {
            return userModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView;

            public CustomViewHolder(View view) {
                super(view);
                imageView = (ImageView)view.findViewById(R.id.friendItem_imageView);
                textView = (TextView)view.findViewById(R.id.friendItem_textView);
            }
        }
    }
}
