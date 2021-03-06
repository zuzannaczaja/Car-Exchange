public class Car {

    String brand;
    String model;
    String bodyType;
    String engineType;
    float engineCapacity;
    int yearOfProduction;
    int basePrice;
    int additionalCosts;

    public Car(String brand, String model, String bodyType, String engineType, float engineCapacity, int yearOfProduction, int basicPrice, int additionalCosts) {
        this.brand = brand;
        this.model = model;
        this.bodyType = bodyType;
        this.engineType = engineType;
        this.engineCapacity = engineCapacity;
        this.yearOfProduction = yearOfProduction;
        this.basePrice = basicPrice;
        this.additionalCosts = additionalCosts;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getBodyType() {
        return bodyType;
    }

    public String getEngineType() {
        return engineType;
    }

    public float getEngineCapacity() {
        return engineCapacity;
    }

    public int getYearOfProduction() {
        return yearOfProduction;
    }

    public int getBasePrice() {
        return basePrice;
    }

    public int getAdditionalCosts() {
        return additionalCosts;
    }

    public int getTotalPrice() {
        return (additionalCosts + basePrice);
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public void setEngineCapacity(float engineCapacity) {
        this.engineCapacity = engineCapacity;
    }

    public void setYearOfProduction(int yearOfProduction) {
        this.yearOfProduction = yearOfProduction;
    }

    public void setBasePrice(int basePrice) {
        this.basePrice = basePrice;
    }

    public void setAdditionalCosts(int additionalCosts) {
        this.additionalCosts = additionalCosts;
    }

}
