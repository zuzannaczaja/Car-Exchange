import java.time.ZonedDateTime;

public class PaymentDelay {
    String buyerName;
    Car car;
    ZonedDateTime endTime;

    public PaymentDelay(String buyerName, Car car, ZonedDateTime endTime) {
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

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
    }
}
