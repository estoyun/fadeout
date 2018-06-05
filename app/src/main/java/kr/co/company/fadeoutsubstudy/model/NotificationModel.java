package kr.co.company.fadeoutsubstudy.model;

/**
 * Created by Yun on 2018-05-28.
 */
public class NotificationModel
{

    public String to;

    public Notification notification = new Notification();

    public static class Notification {
        public String title;
        public String text;
    }
}