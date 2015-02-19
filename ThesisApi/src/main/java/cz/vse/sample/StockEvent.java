package cz.vse.sample;

/**
 *
 * @author Martin Kravec
 */
public class StockEvent {

    //  Time,Open,High,Low,Close,Volume
    //  01.02.2014 00:00:00.000,1.34827,1.34827,1.34827,1.34827,0.0000
    private String time;
    private float open;
    private float high;
    private float low;
    private float close;
    private float volume;

    public StockEvent() {
    }

    public StockEvent(String time, float open, float high, float low, float close, float volume) {
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public float getOpen() {
        return open;
    }

    public void setOpen(float open) {
        this.open = open;
    }

    public float getHigh() {
        return high;
    }

    public void setHigh(float high) {
        this.high = high;
    }

    public float getLow() {
        return low;
    }

    public void setLow(float low) {
        this.low = low;
    }

    public float getClose() {
        return close;
    }

    public void setClose(float close) {
        this.close = close;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "StockEvent [" + open + " " + high + " " + low + " " + close + "]";
    }

}
