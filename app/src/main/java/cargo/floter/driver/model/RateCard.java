package cargo.floter.driver.model;

import java.io.Serializable;
import java.util.List;

public class RateCard implements Serializable {
    private static final long serialVersionUID = 75264784632562247L;
    private String code;
    private String message;
    private List<RateCardResponse> response;
    private String status;

    public class RateCardResponse implements Serializable {
        private static final long serialVersionUID = 752647229536227847L;
        private String base_fare;
        private String capacity;
        private String car_name;
        private String charge_after_free_time;
        private String free_load_unload_time;
        private String height;
        private String length;
        private String price_per_km;
        private String price_per_min_after_60_min;
        private String size;
        private String width;

        public String getFree_load_unload_time() {
            return this.free_load_unload_time;
        }

        public void setFree_load_unload_time(String free_load_unload_time) {
            this.free_load_unload_time = free_load_unload_time;
        }

        public String getCharge_after_free_time() {
            return this.charge_after_free_time;
        }

        public void setCharge_after_free_time(String charge_after_free_time) {
            this.charge_after_free_time = charge_after_free_time;
        }

        public String getPrice_per_min_after_60_min() {
            return this.price_per_min_after_60_min;
        }

        public void setPrice_per_min_after_60_min(String price_per_min_after_60_min) {
            this.price_per_min_after_60_min = price_per_min_after_60_min;
        }

        public String getLength() {
            return this.length;
        }

        public void setLength(String length) {
            this.length = length;
        }

        public String getHeight() {
            return this.height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getWidth() {
            return this.width;
        }

        public void setWidth(String width) {
            this.width = width;
        }

        public String getCar_name() {
            return this.car_name;
        }

        public void setCar_name(String car_name) {
            this.car_name = car_name;
        }

        public String getBase_fare() {
            return this.base_fare;
        }

        public void setBase_fare(String base_fare) {
            this.base_fare = base_fare;
        }

        public String getPrice_per_km() {
            return this.price_per_km;
        }

        public void setPrice_per_km(String price_per_km) {
            this.price_per_km = price_per_km;
        }

        public String getCapacity() {
            return this.capacity;
        }

        public void setCapacity(String capacity) {
            this.capacity = capacity;
        }

        public String getSize() {
            return this.size;
        }

        public void setSize(String size) {
            this.size = size;
        }
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<RateCardResponse> getResponse() {
        return this.response;
    }

    public void setResponse(List<RateCardResponse> response) {
        this.response = response;
    }
}
