package com.objects;

import android.graphics.drawable.Drawable;

/**
 * Created by jieping_yang on 2017/7/10.
 */

public class Operation {
    public String operationName;
    public Drawable icon;
    public int type;
    public Operation(String operationName, Drawable icon, int type){
        this.operationName = operationName;
        this.icon = icon;
        this.type = type;
    }
}
