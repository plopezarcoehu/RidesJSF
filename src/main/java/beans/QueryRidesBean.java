package beans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;

import businessLogic.BLFacade;
import domain.Ride;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@ManagedBean(name = "queryBean")
@SessionScoped
public class QueryRidesBean implements Serializable {

	private String departCity;
	private String destCity;
	private Date rideDate;
	
	private BLFacade facadeBL;

	private List<String> departCities;
	private List<String> destCities;
		
	private List<Ride> rides;

	public QueryRidesBean() {
		facadeBL = FacadeBean.getBusinessLogic();
		rideDate = new Date();
	}

	public String getDepartCity() {
		return departCity;
	}

	public void setDepartCity(String departureCity) {
		this.departCity = departureCity;
	}

	public String getDestCity() {
		return destCity;
	}

	public void setDestCity(String destCity) {
		this.destCity = destCity;
	}

	public Date getRideDate() {
		return rideDate;
	}

	public void setRideDate(Date rideDate) {
		this.rideDate = rideDate;
	}
	
	public List<Ride> getRides() {
		return rides;
	}

	public void setRides(List<Ride> rides) {
		this.rides = rides;
	}

	public List<String> getDepartCities() {
		
		if(facadeBL != null) {
			departCities = facadeBL.getDepartCities();
		}
		return departCities;
	}

	public List<String> getDestCities() {
		if (departCity == null && !departCities.isEmpty()) {
			departCity = departCities.get(0);
		}
		destCities = facadeBL.getDestinationCities(departCity);
		destCity = destCities.get(0);
		return destCities;
	}

	public List<String> getDatesWithRides() {
		List<Date> datesWithRides;
		if (departCity == null && !departCities.isEmpty()) {
			departCity = departCities.get(0);
		}
		if (destCity == null && getDestCities() != null) {
			destCity = getDestCities().get(0);
		}
		datesWithRides = facadeBL.getThisMonthDatesWithRides(departCity, destCity, Calendar.getInstance().getTime());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		List<String> rideDates = new ArrayList<>();
		for(Date d: datesWithRides) {
			rideDates.add(sdf.format(d));
		}
		return rideDates;
	}

	
	public void findRidesListener(AjaxBehaviorEvent event) {
        System.out.println("Método findRides() llamado. Fecha seleccionada: " + rideDate);
        
        findRides();
    }
	
	public String findRides() {
		if (rideDate != null && departCity != null && destCity != null) {
			if(departCity.equals(destCity)) {
				// Oraindik hiriak aldatu ez badira
				destCity = getDestCities().get(0);
			}
			System.out.println("RIDE DATE: " + rideDate);
			Date date = rideDate;
			rides = facadeBL.getRides(departCity, destCity, date);
		}
		System.out.println("ALL RIDES ---------------------------------------- " + rides);
		return null;
	}

}
