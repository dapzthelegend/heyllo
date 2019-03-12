package com.gamecodeschool.heyllo;

import android.util.Log;

public class GroupMessages {
    private String date, message, name, time;
    public GroupMessages(){}

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public GroupMessages(String date, String message, String name, String time) {
        this.date = date;
        this.message = message;
        this.name = name;

        this.time = time;
    }
}