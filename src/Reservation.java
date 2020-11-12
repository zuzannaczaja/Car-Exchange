public class Reservation {

    String buyerName;
    Car car;
    long timeOfReservation;
    long howLongNeedsToBeReserved;

    public Reservation(String buyerName, Car car, long timeOfReservation, long howLongNeedsToBeReserved) {
        this.buyerName = buyerName;
        this.car = car;
        this.timeOfReservation = timeOfReservation;
        this.howLongNeedsToBeReserved = howLongNeedsToBeReserved;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public Car getCar() {
        return car;
    }

    public long getTimeOfReservation() {
        return timeOfReservation;
    }

    public long getHowLongNeedsToBeReserved() {
        return howLongNeedsToBeReserved;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public void setTimeOfReservation(long timeOfReservation) {
        this.timeOfReservation = timeOfReservation;
    }

    public void setHowLongNeedsToBeReserved(long howLongNeedsToBeReserved) {
        this.howLongNeedsToBeReserved = howLongNeedsToBeReserved;
    }
}
