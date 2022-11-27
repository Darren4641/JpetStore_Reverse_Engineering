package org.mybatis.jpetstore.domain;


import java.io.Serializable;

public class ChatRoom implements Serializable {

    private static final long serialVersionUID = 6620528781626504362L;

    private String roomId;
    private String userId;
    private String roomName;
    private String imgurl;
    private int isRead;

    public ChatRoom() {

    }

    public ChatRoom(String roomId, String userId, String roomName, String imgurl, int isRead) {
        this.roomId = roomId;
        this.userId = userId;
        this.roomName = roomName;
        this.imgurl = imgurl;
        this.isRead = isRead;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getIsRead() {
        return isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }


}
