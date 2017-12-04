package com.objects;

/**
 * Created by jieping_yang on 2017/7/11.
 */

public class User {
    public String userId;
    public double money;
    public boolean isVIP = false;
    public int score;
    public User(String userId,double money, boolean isVIP,int score){
        this.userId = userId;
        this.money = money;
        this.isVIP = isVIP;
        this.score = score;
    }

}
