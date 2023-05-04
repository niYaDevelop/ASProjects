package space.yakimov.firstapp;

public class FriendsClass {

    private String name;
    private boolean isOnline = false;

    FriendsClass(){

    }
    FriendsClass(String name, boolean online){
        this.name = name;
        this.isOnline = online;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
