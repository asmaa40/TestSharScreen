package com.infralayer.test.testsharscreen;

/**
 * Created by asmaaali on 8/23/16.
 */
public class screenShot {
    private  String pic;
    private  String deviceID;
    private  int isSender;

    public screenShot(String pic,String deviceID,int isSender) {
        this.pic = pic;
        this.deviceID=deviceID;
        this.isSender=isSender;

    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public int getIsSender() {
        return isSender;
    }

    public void setIsSender(int isSender) {
        this.isSender = isSender;
    }
}
