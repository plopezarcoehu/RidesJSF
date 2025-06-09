package dataAccess;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.MySQLDialect;

import configuration.UtilDate;
import domain.Driver;
import domain.Ride;
import exceptions.RideAlreadyExistException;
import exceptions.RideMustBeLaterThanTodayException;

public class HibernateDataAccess {
	private SessionFactory sessionFactory;
	private Session db;

	public HibernateDataAccess() {
		open();
        if (isDatabaseEmpty()) {
            initializeDB();
        }
	}

	public HibernateDataAccess(Session session) {
		this.db = session;
	}

	public void open() {
		try {
			Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            String hbm2ddlAuto = configuration.getProperty("hibernate.hbm2ddl.auto");
            System.out.println("Hibernate hbm2ddl.auto property: " + hbm2ddlAuto);

            sessionFactory = configuration.buildSessionFactory();
			
			db = sessionFactory.openSession();
			System.out.println("DataAccess sortuta => Hibernate cfg erabiliz");
		} catch (Throwable ex) {
			System.err.println("Errorea Hibernate SessionFactory sortzerakoan: " + ex);
			ex.printStackTrace();
			throw new ExceptionInInitializerError(ex);
		}
	}

	public void close() {
		if (db != null && db.isOpen()) {
			db.close();
		}
		if (sessionFactory != null && !sessionFactory.isClosed()) {
			sessionFactory.close();
		}
		System.out.println("DataAccess closed.");
	}

	/**
     * Check if database is empty (no drivers exist)
     */
	private boolean isDatabaseEmpty() {
        Session session = sessionFactory.openSession();
        try {
            Long count = (Long) session.createQuery("SELECT COUNT(*) FROM Driver").uniqueResult();
            return count == 0;
        } catch (Exception e) {
            return true; 
        } finally {
            session.close();
        }
    }
	
	public void initializeDB() {
		Session db = sessionFactory.openSession();
		Transaction tx = null;

		try {
			tx = db.beginTransaction();

			Calendar today = Calendar.getInstance();
			int month = today.get(Calendar.MONTH);
			int year = today.get(Calendar.YEAR);
			if (month == 12) {
				month = 1;
				year += 1;
			}

			Driver driver1 = new Driver("driver1@gmail.com", "Aitor Fernandez");
			Driver driver2 = new Driver("driver2@gmail.com", "Ane Gaztañaga");
			Driver driver3 = new Driver("driver3@gmail.com", "Test driver");

			driver1.addRide("Donostia", "Bilbo", UtilDate.newDate(year, month, 15), 4, 7);
			driver1.addRide("Donostia", "Gazteiz", UtilDate.newDate(year, month, 6), 4, 8);
			driver1.addRide("Bilbo", "Donostia", UtilDate.newDate(year, month, 25), 4, 4);
			driver1.addRide("Donostia", "Iruña", UtilDate.newDate(year, month, 7), 4, 8);

			driver2.addRide("Donostia", "Bilbo", UtilDate.newDate(year, month, 15), 3, 3);
			driver2.addRide("Bilbo", "Donostia", UtilDate.newDate(year, month, 25), 2, 5);
			driver2.addRide("Eibar", "Gasteiz", UtilDate.newDate(year, month, 6), 2, 5);

			driver3.addRide("Bilbo", "Donostia", UtilDate.newDate(year, month, 14), 1, 3);

			
			
			db.save(driver1);
			db.save(driver2);
			db.save(driver3);
			
			
			//db.save(r1);
			tx.commit();
			//Ride r1 = createRide("Malaga", "Bilbo",UtilDate.newDate(year, month, 15), 4, 7, driver1.getEmail());
			System.out.println("Database initialized with Hibernate");

		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			db.close();
		}
	}

	/**
	 * This method returns all the cities where rides depart
	 * 
	 * @return collection of cities
	 */
	public List<String> getDepartCities() {
		Session db = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = db.beginTransaction();
			List<String> cities = db.createQuery("SELECT DISTINCT r.fromCity FROM Ride r ORDER BY r.fromCity").list();
			tx.commit();
			return cities;
		} catch (Exception e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			e.printStackTrace();
			throw new RuntimeException("Errorea Depart Cities: " + e.getMessage(), e);
		} finally {
            db.close();
        }
	}

	/**
	 * This method returns all the arrival destinations, from all rides that depart
	 * from a given city
	 * 
	 * @param from the depart location of a ride
	 * @return all the arrival destinations
	 */
	public List<String> getArrivalCities(String from) {
		Session db = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = db.beginTransaction();
			List<String> arrivingCities = db
					.createQuery("SELECT DISTINCT r.toCity FROM Ride r WHERE r.fromCity=:fromParam ORDER BY r.toCity")
					.setParameter("fromParam", from).list();
			tx.commit();
			return arrivingCities;
		} catch (Exception e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			e.printStackTrace();
			throw new RuntimeException("Error retrieving arrival cities: " + e.getMessage(), e);
		} finally {
            db.close();
        }
	}

	/**
	 * This method creates a ride for a driver
	 * 
	 * @param from        the origin location of a ride
	 * @param to          the destination location of a ride
	 * @param date        the date of the ride
	 * @param nPlaces     available seats
	 * @param driverEmail to which ride is added
	 * 
	 * @return the created ride, or null, or an exception
	 * @throws RideMustBeLaterThanTodayException if the ride date is before today
	 * @throws RideAlreadyExistException         if the same ride already exists for
	 *                                           the driver
	 */
	public Ride createRide(String from, String to, Date date, int nPlaces, float price, String driverEmail)
			throws RideAlreadyExistException, RideMustBeLaterThanTodayException {
		System.out.println(">> DataAccess: createRide=> from= " + from + " to= " + to + " driver=" + driverEmail
				+ " date " + date);
		Session db = sessionFactory.openSession();
		Transaction tx = null;
		try {
			if (new Date().compareTo(date) > 0) {
				throw new RideMustBeLaterThanTodayException(
						ResourceBundle.getBundle("msg").getString("ErrorRideMustBeLaterThanToday"));
			}

			tx = db.beginTransaction();

			Driver driver = (Driver) db.get(Driver.class, driverEmail);

			if (driver == null) {
				System.err.println("Driver with email " + driverEmail + " not found.");
				tx.rollback();
                return null;
			}

			if (driver.doesRideExists(from, to, date)) {
				tx.rollback();
				throw new RideAlreadyExistException(
						ResourceBundle.getBundle("msg").getString("RideAlreadyExist"));
			}

			Ride ride = driver.addRide(from, to, date, nPlaces, price);

			db.update(driver);

			tx.commit();

			return ride;
		} catch (RideAlreadyExistException | RideMustBeLaterThanTodayException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e; // Re-throw business exceptions
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error creating ride: " + e.getMessage(), e);
        } finally {
            db.close();
        }
	}

	/**
	 * This method retrieves the rides from two locations on a given date
	 * 
	 * @param from the origin location of a ride
	 * @param to   the destination location of a ride
	 * @param date the date of the ride
	 * @return collection of rides
	 */
	public List<Ride> getRides(String from, String to, Date date) {
		System.out.println(">> DataAccess: getRides=> from= " + from + " to= " + to + " date " + date);
		
		Session db = sessionFactory.openSession();
        Transaction tx = null;
		try {
			tx = db.beginTransaction();
			
			Query query = db.createQuery("FROM Ride r WHERE r.fromCity = :from AND r.toCity = :to AND r.date = :date");
			query.setParameter("from", from);
			query.setParameter("to", to);
			query.setParameter("date", date);

			List<Ride> rides = query.list();

			List<Ride> result = new ArrayList<Ride>();
			for (Ride ride : rides) {
				result.add(ride);
			}

			tx.commit();
			return result;
		} catch (Exception e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			e.printStackTrace();
			throw new RuntimeException("Error retrieving rides: " + e.getMessage(), e);
		} finally {
            db.close();
        }
	}

	/**
	 * This method retrieves from the database the dates a month for which there are
	 * events
	 * 
	 * @param from the origin location of a ride
	 * @param to   the destination location of a ride
	 * @param date of the month for which days with rides want to be retrieved
	 * @return collection of rides
	 */
	public List<Date> getThisMonthDatesWithRides(String from, String to, Date date) {
		System.out.println(">> DataAccess: getEventsMonth");
		Session db = sessionFactory.openSession();
        Transaction tx = null;
		try {
			tx = db.beginTransaction();
			
			Date firstDayMonthDate = UtilDate.firstDayMonth(date);
			Date lastDayMonthDate = UtilDate.lastDayMonth(date);

			Query query = db.createQuery(
					"SELECT DISTINCT r.date FROM Ride r WHERE r.fromCity = :fromParam AND r.toCity = :toParam AND r.date BETWEEN :firstDay AND :lastDay");
			query.setParameter("fromParam", from);
			query.setParameter("toParam", to);
			query.setParameter("firstDay", firstDayMonthDate);
			query.setParameter("lastDay", lastDayMonthDate);
			List<Date> dates = query.list();
			tx.commit();
			return dates;
		} catch (Exception e) {
			if (db.getTransaction() != null && db.getTransaction().isActive()) {
				db.getTransaction().rollback();
			}
			e.printStackTrace();
			throw new RuntimeException("Error retrieving dates with rides: " + e.getMessage(), e);
		} finally {
            db.close();
        }
	}

}