import java.util.ArrayList;
import java.util.List;

public class PaymentDelayManager {
    private List<Reservation> paymentDelays = new ArrayList<>();

    public synchronized boolean isPaymentDelayed(final String buyerName, final Car car) {
        return paymentDelays.stream().anyMatch(
                paymentDelays -> paymentDelays.getCar().equals(car) && paymentDelays.getBuyerName()
                        .equals(buyerName));
    }
}
