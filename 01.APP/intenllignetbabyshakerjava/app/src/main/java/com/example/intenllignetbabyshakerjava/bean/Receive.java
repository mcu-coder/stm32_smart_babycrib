package com.example.intenllignetbabyshakerjava.bean;

public class Receive {
    private String temp;
    private String humi;
    private String light;
    private String warning;
    private String music_s;
    private String music;
    private String led;
    private String eat;

    public String getBed() {
        return bed;
    }

    private String bed;
    @Override
    public String toString() {
        return "Receive{" +
                "temp='" + temp + '\'' +
                ", humi='" + humi + '\'' +
                ", light='" + light + '\'' +
                ", warning='" + warning + '\'' +
                ", music_s='" + music_s + '\'' +
                ", music='" + music + '\'' +
                ", eat='" + eat + '\'' +
                ", bed='" + bed + '\'' +
                ", led='" + led + '\'' +
                '}';
    }
    public String getEat() {
        return eat;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getHumi() {
        return humi;
    }

    public void setHumi(String humi) {
        this.humi = humi;
    }

    public String getLight() {
        return light;
    }

    public void setLight(String light) {
        this.light = light;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public String getMusic_s() {
        return music_s;
    }

    public void setMusic_s(String music_s) {
        this.music_s = music_s;
    }

    public String getMusic() {
        return music;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public String getLed() {
        return led;
    }

    public void setLed(String led) {
        this.led = led;
    }
}
