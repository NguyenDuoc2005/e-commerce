package com.ecommerce.payout.model;

public class EventReplayRequest {
    private String topic;
    private String key;
    private String payload;
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
