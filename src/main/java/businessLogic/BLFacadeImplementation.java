package businessLogic;

import java.util.Date;
import java.util.List;
import javax.jws.WebMethod;
import javax.jws.WebService;

import dataAccess.HibernateDataAccess;
import domain.Driver;
import domain.Ride;
import exceptions.RideMustBeLaterThanTodayException;
import exceptions.DriverAlreadyExistsException;
import exceptions.RideAlreadyExistException;

/**
 * It implements the business logic as a web service.
 */
@WebService(endpointInterface = "businessLogic.BLFacade")
public class BLFacadeImplementation implements BLFacade {
	HibernateDataAccess dbManager;

	public BLFacadeImplementation() {
		System.out.println("Creating BLFacadeImplementation instance");

		dbManager = new HibernateDataAccess();

		// dbManager.close();

	}

	public BLFacadeImplementation(HibernateDataAccess da) {

		System.out.println("Creating BLFacadeImplementation instance with DataAccess parameter");

		dbManager = da;
	}

	/**
	 * {@inheritDoc}
	 */
	@WebMethod
	public List<String> getDepartCities() {
		dbManager.open();

		List<String> departLocations = dbManager.getDepartCities();

		dbManager.close();

		return departLocations;

	}

	/**
	 * {@inheritDoc}
	 */
	@WebMethod
	public List<String> getDestinationCities(String from) {
		dbManager.open();

		List<String> targetCities = dbManager.getArrivalCities(from);

		dbManager.close();

		return targetCities;
	}

	/**
	 * {@inheritDoc}
	 */
	@WebMethod
	public Ride createRide(String from, String to, Date date, int nPlaces, float price, String driverEmail)
			throws RideMustBeLaterThanTodayException, RideAlreadyExistException {

		dbManager.open();
		Ride ride = dbManager.createRide(from, to, date, nPlaces, price, driverEmail);
		dbManager.close();
		return ride;
	}

	/**
	 * {@inheritDoc}
	 */
	@WebMethod
	public List<Ride> getRides(String from, String to, Date date) {
		dbManager.open();
		List<Ride> rides = dbManager.getRides(from, to, date);
		dbManager.close();
		return rides;
	}

	/**
	 * {@inheritDoc}
	 */
	@WebMethod
	public List<Date> getThisMonthDatesWithRides(String from, String to, Date date) {
		dbManager.open();
		List<Date> dates = dbManager.getThisMonthDatesWithRides(from, to, date);
		dbManager.close();
		return dates;
	}

	public void close() {
		HibernateDataAccess dB4oManager = new HibernateDataAccess();

		dB4oManager.close();

	}

	/**
	 * {@inheritDoc}
	 */
	@WebMethod
	public void initializeBD() {
		dbManager.open();
		dbManager.initializeDB();
		dbManager.close();
	}

	@Override
	public Driver register(String name, String email, String password) throws DriverAlreadyExistsException {
		dbManager.open();
		Driver d = dbManager.register(name, email, password);
		dbManager.close();
		return d;
	}
	
	@Override
	public Driver login(String email, String password) {
		dbManager.open();
		Driver d = dbManager.login(email, password);
		dbManager.close();
		return d;
	}

}
