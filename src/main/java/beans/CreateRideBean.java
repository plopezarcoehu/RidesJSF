package beans;

import javax.faces.bean.ManagedBean;

import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.util.Date;

@ManagedBean(name = "createBean")
@SessionScoped
public class CreateRideBean implements Serializable {
	
	private String departCity;
    private String destCity;
    private Integer nPlaces;
    private Float price;
    private Date rideDate;
    
    public String getDepartCity() {
        return departCity;
    }

    public void setDepartCity(String departCity) {
        this.departCity = departCity;
    }

    public String getDestCity() {
        return destCity;
    }

    public void setDestCity(String destCity) {
        this.destCity = destCity;
    }

    public Integer getNPlaces() {
        return nPlaces;
    }

    public void setNPlaces(Integer nPlaces) {
        this.nPlaces = nPlaces;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Date getRideDate() {
        return rideDate;
    }

    public void setRideDate(Date rideDate) {
        this.rideDate = rideDate;
    }
    
    public void CreateRide() {
    	
    }
}
