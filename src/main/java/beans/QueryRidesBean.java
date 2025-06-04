package beans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import businessLogic.BLFacade;
import domain.Ride;

import java.io.Serializable;
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

	private static BLFacade facadeBL;

	private List<String> departCities;

	private List<Ride> ridesOnSelectedDate;

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

	public List<String> getDepartCities() {
		departCities = facadeBL.getDepartCities();
		return departCities;
	}

	public List<String> getDestCities() {
		List<String> destCities;
		if (departCity == null && !departCities.isEmpty()) {
			departCity = departCities.get(0);
		}
		destCities = facadeBL.getDestinationCities(departCity);
		return destCities;
	}

	public List<Date> getDatesWithRides() {
		List<Date> datesWithRides;
		if (departCity == null && !departCities.isEmpty()) {
			departCity = departCities.get(0);
		}
		if (destCity == null && getDestCities() != null) {
			destCity = getDestCities().get(0);
		}
		System.out.println("from:" + departCity + "to: " + destCity);
		System.out.println("date:" + Calendar.getInstance().getTime());
		datesWithRides = facadeBL.getThisMonthDatesWithRides(departCity, destCity, Calendar.getInstance().getTime());
		return datesWithRides;
	}

	public List<Ride> search() {
		System.out.println(rideDate);
		if (rideDate != null && departCity != null && destCity != null) {
			System.out.println(rideDate);
			ridesOnSelectedDate = facadeBL.getRides(departCity, destCity, rideDate);
		}
		System.out.println(ridesOnSelectedDate);
		return ridesOnSelectedDate;
	}

}
