package com.example.intenllignetbabyshakerjava.bean;

public class DataDTO {
    private Integer music_s;
    private Integer music;
    private Integer led;

    public void setBed(Integer bed) {
        this.bed = bed;
    }

    private Integer bed;
    @Override
    public String toString() {
        return "DataDTO{" +
                "music_s=" + music_s +
                ", music=" + music +
                ", led=" + led +
                ", bed=" + bed +
                '}';
    }

    public Integer getMusic_s() {
        return music_s;
    }

    public void setMusic_s(Integer music_s) {
        this.music_s = music_s;
    }

    public Integer getMusic() {
        return music;
    }

    public void setMusic(Integer music) {
        this.music = music;
    }

    public Integer getLed() {
        return led;
    }

    public void setLed(Integer led) {
        this.led = led;
    }
}
