import jade.Boot;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;
import java.util.Random;

public class CarSellerAgentNoGui extends Agent {

    private Hashtable carCatalogue;
    private Hashtable reservationList;

    protected void setup() {
        carCatalogue = new Hashtable();
        reservationList = new Hashtable();

        Object[] args = getArguments();

        if (args != null && args.length > 0) {

            String brand;
            String model;
            String bodyType;
            String engineType;
            float engineCapacity;
            int yearOfProduction;
            int basePrice;
            int additionalCosts;

            for(Object o : args) {
                String[] s = ((String)o).split("-");
                brand = s[0].toString();
                model = s[1].toString();
                bodyType = s[2].toString();
                engineType = s[3].toString();
                engineCapacity = Float.parseFloat(s[4]);
                yearOfProduction = Integer.parseInt(s[5]);
                basePrice = Integer.parseInt(s[6]);
                additionalCosts = Integer.parseInt(s[7]);

                Car car = new Car(brand, model, bodyType, engineType, engineCapacity, yearOfProduction, basePrice, additionalCosts);
                String brandAndModel = s[0] + " " + s[1];
                updateCatalogue(brandAndModel, car);
                Reservation reservation = new Reservation(null,null,0,0);
                updateReservationList(brandAndModel, reservation);
            }
        }

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("car-selling");
        sd.setName("JADE-car-trading");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new OfferRequestsServer());
        addBehaviour(new PurchaseOrdersServer());
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Agent sprzedający " + getAID().getName() + " kończy działanie.");
    }

    /**
     * Wywoływane gdy sprzedający doda nowy samochód na sprzedaż.
     */
    public void updateCatalogue(final String brandAndModel, final Car car) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                addBehaviour(createSellingCarBehaviour(brandAndModel, car));
            }
        });
    }

    public void updateReservationList(final String brandAndModel, final Reservation reservation) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                addBehaviour(createReservationBehaviour(brandAndModel, reservation));
            }
        });
    }

    private class OfferRequestsServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage aclMessage = myAgent.receive(messageTemplate);
            if (aclMessage != null) {
                String content = aclMessage.getContent();
                ACLMessage reply = aclMessage.createReply();
                Car car = (Car) carCatalogue.get(content);
                Integer price = null;
                if (car != null) {
                    price = car.getTotalPrice();
                }
                if (price != null) {
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price.intValue()));
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    private class PurchaseOrdersServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage aclMessage = myAgent.receive(messageTemplate);

            if (aclMessage != null) {

                String content = aclMessage.getContent();
                String[] parts = content.split(",");

                content = parts[0];
                String isDelayedString = parts[1];
                boolean isDelayedBoolean = Boolean.parseBoolean(isDelayedString);

                ACLMessage reply = aclMessage.createReply();
                Car car = (Car) carCatalogue.get(content);
                Reservation reservation = (Reservation) reservationList.get(content);

                Random random = new Random();
                Integer price = null;
                String buyerName = aclMessage.getSender().getLocalName();

                //sprawdzamy czy istnieje samochód i posiada rezerwację
                if (reservation.getBuyerName() != null){

                    if(reservation.getTimeOfReservation() + reservation.getHowLongNeedsToBeReserved() <= System.currentTimeMillis()
                    && buyerName.equals(reservation.getBuyerName())){
                        //koniec rezerwacji i zakup

                        System.out.println("Koniec rezerwacji u " + getAID().getLocalName() + " przez " + aclMessage.getSender().getLocalName() + "!");

                        if (car != null) {

                            price = car.getTotalPrice();
                        }
                        carCatalogue.remove(content);
                        reservationList.remove(content);

                        if (price != null) {
                            reply.setPerformative(ACLMessage.INFORM);
                            System.out.println(content + " sprzedany agentowi " + aclMessage.getSender().getName());
                        } else {
                            reply.setPerformative(ACLMessage.FAILURE);
                            reply.setContent("not-available");
                        }

                    } else {
                        //rezerwacja istnieje, ale nie dobiegła końca

                        if (price != null) {
                            reply.setPerformative(ACLMessage.FAILURE);
                            reply.setContent("not-available");
                            System.out.println("Nieudana próba kupna: rezerwacja samochodu nadal trwa.");
                        }

                    }
                } else if (reservation.getBuyerName() == null){
                    //istnieje samochód, ale nie posiada rezerwacji

                    //jezeli tak to losujemy czas rezerwacji
                    if(isDelayedBoolean){
                        int randomTimeOfReservation = random.nextInt(20000 + 1);
                        reservation.setBuyerName(aclMessage.getSender().getLocalName());
                        reservation.setCar(car);
                        reservation.setTimeOfReservation(System.currentTimeMillis());
                        reservation.setHowLongNeedsToBeReserved(randomTimeOfReservation);

                        System.out.println("Zarezerwowano samochód u "  + getAID().getLocalName() + " przez " + aclMessage.getSender().getLocalName() + "!");

                    } else if (!isDelayedBoolean){
                        //nie musi być rezerwowany więc pozwalamy go kupić od razu

                        if (car != null) {

                            price = car.getTotalPrice();
                        }
                        carCatalogue.remove(content);

                        if (price != null) {
                            reply.setPerformative(ACLMessage.INFORM);
                            System.out.println(content + " sprzedany agentowi " + aclMessage.getSender().getName());
                        } else {
                            reply.setPerformative(ACLMessage.FAILURE);
                            reply.setContent("not-available");
                            System.out.println("Nieudana próba kupna: wybrany samochód nie jest dostępny.");
                        }
                    }
                } else {
                    //nie ma takiego samochodu w katalogu
                }

                if(carCatalogue.isEmpty()){
                    doDelete();
                    System.out.println(getAID().getName() + " jest usuwany, bo sprzedał wszystkie auta.");
                }

                myAgent.send(reply);
            } else {
                block();
            }
        }
    }


    private OneShotBehaviour createSellingCarBehaviour(final String brandAndModel, final Car car) {
        return new OneShotBehaviour() {
            private static final long serialVersionUID = 1L;

            public void action() {
                carCatalogue.put(brandAndModel, car);
                System.out.println(brandAndModel + " został dodany do katalogu. Cena = " + car.getTotalPrice());
            }
        };
    }

    private OneShotBehaviour createReservationBehaviour(final String brandAndModel, final Reservation reservation) {
        return new OneShotBehaviour() {
            private static final long serialVersionUID = 1L;

            public void action() {
                reservationList.put(brandAndModel, reservation);
               /* System.out.println("> Rezerwacja kupującego: " + brandAndModel
                        + " została dodana do listy rezerwacji. Zarezerwowany samochód: "
                        + reservation.getCar().getBrand() + " " + reservation.getCar().getModel());*/
            }
        };
    }
}

