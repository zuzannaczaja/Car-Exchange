import java.time.ZonedDateTime;

public class Reservation {

    String buyerName;
    Car car;
    long endTime;

    public Reservation(String buyerName, Car car, long endTime) {
        this.buyerName = buyerName;
        this.car = car;
        this.endTime = endTime;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public Car getCar() {
        return car;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
