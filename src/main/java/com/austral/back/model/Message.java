package com.austral.back.model;

public class Message {

    private String role; // "user" o "model"
    private String text;

    public Message() {}

    public Message(String role, String text) {
        this.role = role;
        this.text = text;
    }

    public String getRole() {
        return role;
    }

    public String getText() {
        return text;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setText(String text) {
        this.text = text;
    }
}