package org.jalu.dto;

public class LogImmersionRequestDto {
    private String mediaType;
    private String title;
    private Integer amount;
    private String unit;
    private String date;
    private String comment;

    public LogImmersionRequestDto() {

    }

    public LogImmersionRequestDto(String mediaType, String title, Integer amount, String unit, String date, String comment) {
        this.mediaType = mediaType;
        this.title = title;
        this.amount = amount;
        this.unit = unit;
        this.date = date;
        this.comment = comment;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
