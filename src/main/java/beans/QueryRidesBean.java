package beans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;

import businessLogic.BLFacade;
import domain.Ride;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
	private List<String> datesWithRides;
	private List<Ride> rides;

	public QueryRidesBean() {
		facadeBL = FacadeBean.getBusinessLogic();
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
		if (facadeBL != null) {
			departCities = facadeBL.getDepartCities();
		}
		return departCities;
	}

	public List<String> getDestCities() {
		if (departCity == null && !departCities.isEmpty()) {
			departCity = departCities.get(0);
		}
		destCities = facadeBL.getDestinationCities(departCity);
		System.out.println(destCities);
		
		if (destCities != null && !destCities.isEmpty()) {
			destCity = destCities.get(0);
			getDatesWithRides();
		}
		
		return destCities;
	}

	public String getDatesWithRides() {
		List<Date> dates;
		if (departCity == null && !departCities.isEmpty()) {
			departCity = departCities.get(0);
		}
		if (destCity == null && getDestCities() != null) {
			destCity = getDestCities().get(0);
		}
		dates = facadeBL.getThisMonthDatesWithRides(departCity, destCity, Calendar.getInstance().getTime());
		if(dates == null) {
			datesWithRides.clear();
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Collections.sort(dates);
		datesWithRides = new ArrayList<>();
		for (Date d : dates) {
			datesWithRides.add(sdf.format(d));
		}	
		
		System.out.println("DATES: " + datesWithRides);
		return String.join(",", datesWithRides);
	}

	public void findRidesListener(AjaxBehaviorEvent event) {
		getDatesWithRides();
		findRides();
	}

	public String findRides() {
		if (rideDate != null && departCity != null && destCity != null) {
			System.out.println("RIDE DATE: " + rideDate);
			Date date = rideDate;
			rides = facadeBL.getRides(departCity, destCity, date);
		}
		System.out.println("ALL RIDES ---------------------------------------- " + rides);
		return null;
	}

	public void onDepartCityChange(AjaxBehaviorEvent event) {
		if (departCity != null) {
			destCities = getDestCities();

			if (rideDate != null && destCity != null) {
				findRides();
			}
		}
	}
	
	public String index() {
		return "index";
	}

}
