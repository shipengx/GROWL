package com.example.growl.csv.model;

import com.opencsv.bean.CsvBindByName;

public class Offer {

    @CsvBindByName(column = "Offer ID")
    private String offerId;

    @CsvBindByName
    private String name;

    @CsvBindByName(column = "Preview URL")
    private String previewUrl;

    @CsvBindByName(column = "Payout Type")
    private String payoutType;

    @CsvBindByName(column = "Payout Amount")
    private String payoutAmount;

    @CsvBindByName(column = "Payout Percentage")
    private String payoutPercentage;

    @CsvBindByName(column = "Click to Run")
    private String clickToRun;

    public Offer() {
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getPayoutType() {
        return payoutType;
    }

    public void setPayoutType(String payoutType) {
        this.payoutType = payoutType;
    }

    public String getPayoutAmount() {
        return payoutAmount;
    }

    public void setPayoutAmount(String payoutAmount) {
        this.payoutAmount = payoutAmount;
    }

    public String getPayoutPercentage() {
        return payoutPercentage;
    }

    public void setPayoutPercentage(String payoutPercentage) {
        this.payoutPercentage = payoutPercentage;
    }

    public String getClickToRun() {
        return clickToRun;
    }

    public void setClickToRun(String clickToRun) {
        this.clickToRun = clickToRun;
    }
}
