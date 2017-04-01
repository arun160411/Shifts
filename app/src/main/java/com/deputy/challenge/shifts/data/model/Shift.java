package com.deputy.challenge.shifts.data.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by akatta on 3/30/17.
 */

public class Shift implements Parcelable {

    private int id;
    private long startTimeStamp;
    private long endTimestamp;
    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;
    private String imageURL;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStartTimeStamp(long startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }


    public double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(double endLongitude) {
        this.endLongitude = endLongitude;
    }

    public double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public Shift() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeLong(this.startTimeStamp);
        dest.writeLong(this.endTimestamp);
        dest.writeDouble(this.startLongitude);
        dest.writeDouble(this.startLatitude);
        dest.writeDouble(this.endLongitude);
        dest.writeDouble(this.endLatitude);
        dest.writeString(this.imageURL);
    }

    protected Shift(Parcel in) {
        this.id = in.readInt();
        this.startTimeStamp = in.readLong();
        this.endTimestamp = in.readLong();
        this.startLongitude = in.readDouble();
        this.startLatitude = in.readDouble();
        this.endLongitude = in.readDouble();
        this.endLatitude = in.readDouble();
        this.imageURL = in.readString();
    }

    public static final Creator<Shift> CREATOR = new Creator<Shift>() {
        @Override
        public Shift createFromParcel(Parcel source) {
            return new Shift(source);
        }

        @Override
        public Shift[] newArray(int size) {
            return new Shift[size];
        }
    };
}
