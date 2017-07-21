package cargo.floter.driver.model;

/**
 * Created by SONI on 5/26/2017.
 */

public class Payment {

    private String payment_id;
    private String trip_id;
    private String u_fname;
    private String d_fname;
    private String pay_trans_id;
    private String pay_date;
    private String pay_mode;
    private String pay_amount;
    private String pay_status;
    private String promo_id;
    private String pay_promo_code;
    private String pay_promo_amt;
    private String pay_time;
    private String settlement;
    private String pay_created;
    private String pay_modified;
    private Trip Trip;

    public String getPayment_id() {
        return payment_id;
    }

    public void setPayment_id(String payment_id) {
        this.payment_id = payment_id;
    }

    public String getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

    public String getU_fname() {
        return u_fname;
    }

    public void setU_fname(String u_fname) {
        this.u_fname = u_fname;
    }

    public String getD_fname() {
        return d_fname;
    }

    public void setD_fname(String d_fname) {
        this.d_fname = d_fname;
    }

    public String getPay_trans_id() {
        return pay_trans_id;
    }

    public void setPay_trans_id(String pay_trans_id) {
        this.pay_trans_id = pay_trans_id;
    }

    public String getPay_date() {
        return pay_date;
    }

    public void setPay_date(String pay_date) {
        this.pay_date = pay_date;
    }

    public String getPay_mode() {
        return pay_mode;
    }

    public void setPay_mode(String pay_mode) {
        this.pay_mode = pay_mode;
    }

    public String getPay_amount() {
        return pay_amount;
    }

    public void setPay_amount(String pay_amount) {
        this.pay_amount = pay_amount;
    }

    public String getPay_status() {
        return pay_status;
    }

    public void setPay_status(String pay_status) {
        this.pay_status = pay_status;
    }

    public String getPromo_id() {
        return promo_id;
    }

    public void setPromo_id(String promo_id) {
        this.promo_id = promo_id;
    }

    public String getPay_promo_code() {
        return pay_promo_code;
    }

    public void setPay_promo_code(String pay_promo_code) {
        this.pay_promo_code = pay_promo_code;
    }

    public String getPay_promo_amt() {
        return pay_promo_amt;
    }

    public void setPay_promo_amt(String pay_promo_amt) {
        this.pay_promo_amt = pay_promo_amt;
    }

    public String getPay_time() {
        return pay_time;
    }

    public void setPay_time(String pay_time) {
        this.pay_time = pay_time;
    }

    public String getSettlement() {
        return settlement;
    }

    public void setSettlement(String settlement) {
        this.settlement = settlement;
    }

    public String getPay_created() {
        return pay_created;
    }

    public void setPay_created(String pay_created) {
        this.pay_created = pay_created;
    }

    public String getPay_modified() {
        return pay_modified;
    }

    public void setPay_modified(String pay_modified) {
        this.pay_modified = pay_modified;
    }

    public cargo.floter.driver.model.Trip getTrip() {
        return Trip;
    }

    public void setTrip(cargo.floter.driver.model.Trip trip) {
        Trip = trip;
    }
}
