package beans;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;

import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

import businessLogic.BLFacade;
import domain.Ride;
import exceptions.RideAlreadyExistException;
import exceptions.RideMustBeLaterThanTodayException;

import java.io.Serializable;
import java.util.Date;
import java.util.ResourceBundle;

@ManagedBean(name = "createBean")
@SessionScoped
public class CreateRideBean implements Serializable {

	private String departCity;
	private String destCity;
	private Integer nPlaces;
	private Float price;
	private Date rideDate;

	private BLFacade facadeBL;

	public CreateRideBean() {
		facadeBL = FacadeBean.getBusinessLogic();
		rideDate = new Date();
	}

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

	public void createRide() {
		try {
			Ride r = facadeBL.createRide(departCity, destCity, rideDate, nPlaces, price, "driver1@gmail.com");
			if (r != null) {
				addGlobalMessage("RideCreated");
			}else {
				addGlobalMessage("ErrorQuery");
			}
			System.out.println("NEW RIDE: " + r);
		} catch (RideAlreadyExistException e) {
			addGlobalMessage("RideAlreadyExist");
		} catch (RideMustBeLaterThanTodayException e) {
			addGlobalMessage("ErrorRideMustBeLaterThanToday");
		}
	}

	public void validateNPlaces(FacesContext context, UIComponent comp, Object value) {
		Integer places = (Integer) value;
		if (places != null) {
			if (places <= 0) {
				((UIInput) comp).setValid(false);
				FacesMessage message = new FacesMessage(getMessage("SeatsMustBeGreaterThan0"));
				context.addMessage(comp.getClientId(context), message);
			} else {
				nPlaces = places;
			}
		} else {
			((UIInput) comp).setValid(false);
			FacesMessage message = new FacesMessage(getMessage("ErrorNumber"));
			context.addMessage(comp.getClientId(context), message);
		}
	}

	public void validatePrice(FacesContext context, UIComponent comp, Object value) {
		Float p = (Float) value;
		if (p != null) {
			if (p <= 0) {
				((UIInput) comp).setValid(false);
				FacesMessage message = new FacesMessage(getMessage("PriceMustBeGreaterThan0"));
				context.addMessage(comp.getClientId(context), message);
			} else {
				price = p;
			}
		} else {
			((UIInput) comp).setValid(false);
			FacesMessage message = new FacesMessage(getMessage("ErrorNumber"));
			context.addMessage(comp.getClientId(context), message);
		}
	}

	public void validateDate(FacesContext context, UIComponent comp, Object value) {
		Date d = (Date) value;
		if (d != null) {
			if (d.before(new Date())) {
				((UIInput) comp).setValid(false);
				FacesMessage message = new FacesMessage(getMessage("ErrorRideMustBeLaterThanToday"));
				context.addMessage(comp.getClientId(context), message);
			} else {
				rideDate = d;
			}
		}
	}

	private String getMessage(String key) {
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		return bundle.getString(key);
	}

	private void addGlobalMessage(String messageKey) {
		FacesContext context = FacesContext.getCurrentInstance();
		String mensaje = getMessage(messageKey);
		FacesMessage message = new FacesMessage(mensaje);
		context.addMessage(null, message);
	}
	
	public String index() {
		return "index";
	}
}
