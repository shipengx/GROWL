package com.example.growl.csv.model;

import com.opencsv.bean.CsvBindByName;

public class AdOpportunity {

    @CsvBindByName
    private String id;

    @CsvBindByName(column = "session_id")
    private String sessionId;

    @CsvBindByName(column = "slot_id")
    private String slotId;

    @CsvBindByName(column = "visitor_id")
    private String visitorId;

    @CsvBindByName(column = "publisher_id")
    private String publisherId;

    @CsvBindByName(column = "ad_unit_id")
    private String adUnitId;

    @CsvBindByName(column = "slot_name")
    private String slotName;

    @CsvBindByName(column = "slot_key")
    private String slotKey;

    @CsvBindByName(column = "slot_data")
    private String slotData;

    @CsvBindByName
    private String theme;

    @CsvBindByName(column = "ad_bg_color")
    private String adBgColor;

    @CsvBindByName(column = "payout_eligible")
    private String payoutEligible;

    @CsvBindByName(column = "source_url")
    private String sourceUrl;

    @CsvBindByName(column = "request_headers")
    private String requestHeaders;

    @CsvBindByName(column = "client_ip")
    private String clientIp;

    @CsvBindByName(column = "device_type")
    private String deviceType;

    @CsvBindByName(column = "os_name")
    private String osName;

    @CsvBindByName
    private String browser;

    @CsvBindByName(column = "geo_country_code")
    private String geoCountryCode;

    @CsvBindByName(column = "geo_country_name")
    private String geoCountryName;

    @CsvBindByName(column = "geo_region")
    private String geoRegion;

    @CsvBindByName(column = "geo_city")
    private String geoCity;

    @CsvBindByName(column = "created_at")
    private String createdAt;

    public AdOpportunity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public String getVisitorId() {
        return visitorId;
    }

    public void setVisitorId(String visitorId) {
        this.visitorId = visitorId;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getAdUnitId() {
        return adUnitId;
    }

    public void setAdUnitId(String adUnitId) {
        this.adUnitId = adUnitId;
    }

    public String getSlotName() {
        return slotName;
    }

    public void setSlotName(String slotName) {
        this.slotName = slotName;
    }

    public String getSlotKey() {
        return slotKey;
    }

    public void setSlotKey(String slotKey) {
        this.slotKey = slotKey;
    }

    public String getSlotData() {
        return slotData;
    }

    public void setSlotData(String slotData) {
        this.slotData = slotData;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getAdBgColor() {
        return adBgColor;
    }

    public void setAdBgColor(String adBgColor) {
        this.adBgColor = adBgColor;
    }

    public String getPayoutEligible() {
        return payoutEligible;
    }

    public void setPayoutEligible(String payoutEligible) {
        this.payoutEligible = payoutEligible;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getGeoCountryCode() {
        return geoCountryCode;
    }

    public void setGeoCountryCode(String geoCountryCode) {
        this.geoCountryCode = geoCountryCode;
    }

    public String getGeoCountryName() {
        return geoCountryName;
    }

    public void setGeoCountryName(String geoCountryName) {
        this.geoCountryName = geoCountryName;
    }

    public String getGeoRegion() {
        return geoRegion;
    }

    public void setGeoRegion(String geoRegion) {
        this.geoRegion = geoRegion;
    }

    public String getGeoCity() {
        return geoCity;
    }

    public void setGeoCity(String geoCity) {
        this.geoCity = geoCity;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
