package kr.co.company.fadeoutsubstudy.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yun on 2018-05-28.
 */
public class ChatModel {

    public Map<String,Boolean> users = new HashMap<>(); //채팅방의 유저들(본인의 uid 정보, 상대방 uid 정보)
    public Map<String,Comment> comments = new HashMap<>(); //채팅방의 대화내용

    public static class Comment
    {
        public String uid;
        public String message;
        public Object timestamp;
    }
}
