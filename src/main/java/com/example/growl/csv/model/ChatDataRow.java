package com.example.growl.csv.model;

import com.opencsv.bean.CsvBindByName;

public class ChatDataRow {

    @CsvBindByName
    private String id;

    @CsvBindByName(column = "publisher_id")
    private String publisherId;

    @CsvBindByName(column = "user_id")
    private String userId;

    @CsvBindByName(column = "user_email")
    private String userEmail;

    @CsvBindByName(column = "visitor_id")
    private String visitorId;

    @CsvBindByName(column = "chat_id")
    private String chatId;

    @CsvBindByName(column = "session_id")
    private String sessionId;

    @CsvBindByName
    private String url;

    @CsvBindByName(column = "request_headers")
    private String requestHeaders;

    @CsvBindByName
    private String role;

    @CsvBindByName
    private String text;

    @CsvBindByName(column = "created_at")
    private String createdAt;

    public ChatDataRow() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getVisitorId() {
        return visitorId;
    }

    public void setVisitorId(String visitorId) {
        this.visitorId = visitorId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
