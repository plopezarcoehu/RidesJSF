package dataAccess;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import configuration.UtilDate;
import domain.Driver;
import domain.Ride;
import exceptions.DriverAlreadyExistsException;
import exceptions.RideAlreadyExistException;
import exceptions.RideMustBeLaterThanTodayException;

import org.hibernate.classic.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Query;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class HibernateDataAccessTest {

	@Mock
	private SessionFactory mockSessionFactory;

	@Mock
	private Session mockSession;

	@Mock
	private Transaction mockTransaction;

	@Mock
	private Query mockQuery;

	@InjectMocks
	private HibernateDataAccess hibernateDataAccess;

	private String commonFromCity;
	private String commonToCity;
	private Date commonFutureDate;
	private int commonNPlaces;
	private float commonPrice;
	private String commonDriverEmail;
	private Driver commonDriver;
	private Ride commonRide;

	@Before
	public void setUp() {
		when(mockSessionFactory.openSession()).thenReturn(mockSession);

		when(mockSession.beginTransaction()).thenReturn(mockTransaction);

		doNothing().when(mockTransaction).commit();

		when(mockSession.close()).thenReturn(null);

		commonFromCity = "Donostia";
		commonToCity = "Bilbo";
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, 5);
		commonFutureDate = cal.getTime();
		commonNPlaces = 3;
		commonPrice = 10.0f;
		commonDriverEmail = "testdriver@example.com";

		commonDriver = new Driver(commonDriverEmail, "Test Driver");
		commonDriver.setPassword("securepass");

		commonRide = new Ride(commonFromCity, commonToCity, commonFutureDate, commonNPlaces, commonPrice, commonDriver);
		commonDriver.addRide(commonFromCity, commonToCity, commonFutureDate, commonNPlaces, commonPrice);
	}

	@Test
	public void testIsDatabaseEmpty_True() {
		when(mockSession.createQuery("SELECT COUNT(*) FROM Driver")).thenReturn(mockQuery);
		when(mockQuery.uniqueResult()).thenReturn(0L);

		boolean isEmpty = hibernateDataAccess.isDatabaseEmpty();

		assertTrue("Database should be reported as empty", isEmpty);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).createQuery("SELECT COUNT(*) FROM Driver");
		verify(mockQuery, times(1)).uniqueResult();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test
	public void testIsDatabaseEmpty_False() {
		when(mockSession.createQuery("SELECT COUNT(*) FROM Driver")).thenReturn(mockQuery);
		when(mockQuery.uniqueResult()).thenReturn(10L);

		boolean isEmpty = hibernateDataAccess.isDatabaseEmpty();

		assertFalse("Database should be reported as NOT empty", isEmpty);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).createQuery("SELECT COUNT(*) FROM Driver");
		verify(mockQuery, times(1)).uniqueResult();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test
	public void testIsDatabaseEmpty_ErrorHandling() {
		when(mockSession.createQuery("SELECT COUNT(*) FROM Driver")).thenReturn(mockQuery);
		when(mockQuery.uniqueResult()).thenThrow(new RuntimeException("Simulated DB connection error during count"));

		boolean isEmpty = hibernateDataAccess.isDatabaseEmpty();

		assertTrue("Database should be reported as empty on error", isEmpty);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).createQuery("SELECT COUNT(*) FROM Driver");
		verify(mockQuery, times(1)).uniqueResult();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test
	public void testGetDepartCities_Successful() {
		List<String> expectedCities = Arrays.asList("Donostia", "Eibar", "Bilbo");

		when(mockSession.createQuery("SELECT DISTINCT r.fromCity FROM Ride r ORDER BY r.fromCity"))
				.thenReturn(mockQuery);
		when(mockQuery.list()).thenReturn(expectedCities);

		List<String> actualCities = hibernateDataAccess.getDepartCities();

		assertNotNull("Returned list should not be null", actualCities);
		assertEquals("Returned list should match expected cities", expectedCities, actualCities);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1)).createQuery("SELECT DISTINCT r.fromCity FROM Ride r ORDER BY r.fromCity");
		verify(mockQuery, times(1)).list();
		verify(mockTransaction, times(1)).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test
	public void testGetDepartCities_EmptyList() {
		when(mockSession.createQuery("SELECT DISTINCT r.fromCity FROM Ride r ORDER BY r.fromCity"))
				.thenReturn(mockQuery);
		when(mockQuery.list()).thenReturn(Collections.emptyList());

		List<String> actualCities = hibernateDataAccess.getDepartCities();

		assertNotNull("Returned list should not be null even if empty", actualCities);
		assertTrue("Returned list should be empty", actualCities.isEmpty());

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1)).createQuery("SELECT DISTINCT r.fromCity FROM Ride r ORDER BY r.fromCity");
		verify(mockQuery, times(1)).list();
		verify(mockTransaction, times(1)).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test(expected = RuntimeException.class)
	public void testGetDepartCities_DataAccessError() {
		when(mockSession.createQuery("SELECT DISTINCT r.fromCity FROM Ride r ORDER BY r.fromCity"))
				.thenReturn(mockQuery);
		when(mockQuery.list()).thenThrow(new RuntimeException("Simulated DB error during query"));

		hibernateDataAccess.getDepartCities();

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1)).createQuery("SELECT DISTINCT r.fromCity FROM Ride r ORDER BY r.fromCity");
		verify(mockQuery, times(1)).list();
		verify(mockTransaction, times(1)).rollback();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test
	public void testGetArrivalCities_Successful() {
		String fromParam = "Donostia";
		List<String> expectedCities = Arrays.asList("Bilbo", "Gasteiz");

		when(mockSession
				.createQuery("SELECT DISTINCT r.toCity FROM Ride r WHERE r.fromCity=:fromParam ORDER BY r.toCity"))
				.thenReturn(mockQuery);
		when(mockQuery.setParameter(eq("fromParam"), eq(fromParam))).thenReturn(mockQuery);
		when(mockQuery.list()).thenReturn(expectedCities);

		List<String> actualCities = hibernateDataAccess.getArrivalCities(fromParam);

		assertNotNull("Returned list should not be null", actualCities);
		assertEquals("Returned list should match expected cities", expectedCities, actualCities);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1))
				.createQuery("SELECT DISTINCT r.toCity FROM Ride r WHERE r.fromCity=:fromParam ORDER BY r.toCity");
		verify(mockQuery, times(1)).setParameter("fromParam", fromParam);
		verify(mockQuery, times(1)).list();
		verify(mockTransaction, times(1)).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test
	public void testGetArrivalCities_NoResults() {
		String fromParam = "NonExistentCity";
		when(mockSession
				.createQuery("SELECT DISTINCT r.toCity FROM Ride r WHERE r.fromCity=:fromParam ORDER BY r.toCity"))
				.thenReturn(mockQuery);
		when(mockQuery.setParameter(eq("fromParam"), eq(fromParam))).thenReturn(mockQuery); // Ensure parameter setting
																							// is mocked
		when(mockQuery.list()).thenReturn(Collections.emptyList());

		List<String> actualCities = hibernateDataAccess.getArrivalCities(fromParam);

		assertNotNull("Returned list should not be null", actualCities);
		assertTrue("Returned list should be empty", actualCities.isEmpty());

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1))
				.createQuery("SELECT DISTINCT r.toCity FROM Ride r WHERE r.fromCity=:fromParam ORDER BY r.toCity");
		verify(mockQuery, times(1)).setParameter("fromParam", fromParam);
		verify(mockQuery, times(1)).list();
		verify(mockTransaction, times(1)).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test(expected = RuntimeException.class)
	public void testGetArrivalCities_DataAccessError() {
		String fromParam = "AnyCity";
		when(mockSession
				.createQuery("SELECT DISTINCT r.toCity FROM Ride r WHERE r.fromCity=:fromParam ORDER BY r.toCity"))
				.thenReturn(mockQuery);
		when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery); // Stub param setting
		when(mockQuery.list()).thenThrow(new RuntimeException("DB connection lost for arrival cities"));

		hibernateDataAccess.getArrivalCities(fromParam);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1))
				.createQuery("SELECT DISTINCT r.toCity FROM Ride r WHERE r.fromCity=:fromParam ORDER BY r.toCity");
		verify(mockQuery, times(1)).setParameter("fromParam", fromParam);
		verify(mockQuery, times(1)).list();
		verify(mockTransaction, times(1)).rollback();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test
	public void testCreateRide_Successful() throws RideAlreadyExistException, RideMustBeLaterThanTodayException {
		when(mockSession.get(eq(Driver.class), eq(commonDriverEmail))).thenReturn(commonDriver);
		doNothing().when(mockSession).update(any(Driver.class));

		String newCity = "Tokio";
		Ride actualRide = hibernateDataAccess.createRide(newCity, commonToCity, commonFutureDate, commonNPlaces,
				commonPrice, commonDriverEmail);

		assertNotNull("The created ride should not be null.", actualRide);
		assertEquals(newCity, actualRide.getFromCity());
		assertEquals(commonToCity, actualRide.getToCity());
		assertEquals(commonFutureDate, actualRide.getDate());
		assertEquals(commonNPlaces, actualRide.getnPlaces());
		assertEquals(commonPrice, actualRide.getPrice(), 1);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1)).get(eq(Driver.class), eq(commonDriverEmail));
		verify(mockSession, times(1)).update(eq(commonDriver));
		verify(mockTransaction, times(1)).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test(expected = RideMustBeLaterThanTodayException.class)
	public void testCreateRide_DateNotLaterThanToday()
			throws RideAlreadyExistException, RideMustBeLaterThanTodayException {
		Calendar cal = Calendar.getInstance();
		int year = Calendar.YEAR;
		int month = Calendar.MONTH;
		int day = Calendar.DAY_OF_MONTH;
		cal.set(year, month - 1, day);
		Date pastDate = cal.getTime();

		hibernateDataAccess.createRide(commonFromCity, commonToCity, pastDate, commonNPlaces, commonPrice,
				commonDriverEmail);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, never()).get(any(Class.class), any(Serializable.class));
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test
	public void testCreateRide_DriverNotFound() throws RideAlreadyExistException, RideMustBeLaterThanTodayException {
		String nonExistentDriverEmail = "nonexistent@example.com";
		when(mockSession.get(eq(Driver.class), eq(nonExistentDriverEmail))).thenReturn(null);

		Ride actualRide = hibernateDataAccess.createRide(commonFromCity, commonToCity, commonFutureDate, commonNPlaces,
				commonPrice, nonExistentDriverEmail);

		assertNull("Ride should be null if driver not found.", actualRide);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1)).get(eq(Driver.class), eq(nonExistentDriverEmail));
		verify(mockTransaction, times(1)).rollback();
		verify(mockTransaction, never()).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test(expected = RideAlreadyExistException.class)
	public void testCreateRide_RideAlreadyExists() throws RideAlreadyExistException, RideMustBeLaterThanTodayException {
		Driver mockDriverWithExistingRide = mock(Driver.class);
		when(mockSession.get(eq(Driver.class), eq(commonDriverEmail))).thenReturn(mockDriverWithExistingRide);
		when(mockDriverWithExistingRide.doesRideExists(commonFromCity, commonToCity, commonFutureDate))
				.thenReturn(true);

		hibernateDataAccess.createRide(commonFromCity, commonToCity, commonFutureDate, commonNPlaces, commonPrice,
				commonDriverEmail);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1)).get(eq(Driver.class), eq(commonDriverEmail));
		verify(mockTransaction, times(1)).rollback();
		verify(mockTransaction, never()).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery,
				mockDriverWithExistingRide);
	}

	@Test(expected = RuntimeException.class)
	public void testCreateRide_DataAccessFailure() throws RideAlreadyExistException, RideMustBeLaterThanTodayException {
		when(mockSession.get(eq(Driver.class), eq(commonDriverEmail)))
				.thenThrow(new RuntimeException("Simulated DB connection error during driver lookup"));

		hibernateDataAccess.createRide(commonFromCity, commonToCity, commonFutureDate, commonNPlaces, commonPrice,
				commonDriverEmail);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1)).get(eq(Driver.class), eq(commonDriverEmail));
		verify(mockTransaction, times(1)).rollback();
		verify(mockTransaction, never()).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test
	public void testRegister_Successful() throws DriverAlreadyExistsException {
		String name = "New User";
		String email = "newuser@example.com";
		String password = "newpass";

		when(mockSession.get(eq(Driver.class), eq(email))).thenReturn(null);

		Driver actualDriver = hibernateDataAccess.register(name, email, password);

		assertNotNull("Registered driver should not be null", actualDriver);
		assertEquals("Email should match", email, actualDriver.getEmail());
		assertEquals("Name should match", name, actualDriver.getName());
		assertEquals("Password should be set", password, actualDriver.getPassword());

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1)).get(eq(Driver.class), eq(email));
		verify(mockSession, times(1)).save(argThat(d -> d instanceof Driver && ((Driver) d).getEmail().equals(email)
				&& ((Driver) d).getName().equals(name) && ((Driver) d).getPassword().equals(password)));
		verify(mockTransaction, times(1)).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test(expected = DriverAlreadyExistsException.class)
	public void testRegister_DriverAlreadyExists() throws DriverAlreadyExistsException {
		String existingEmail = "existing@example.com";
		String name = "Existing User";
		String password = "pass";

		when(mockSession.get(eq(Driver.class), eq(existingEmail))).thenReturn(new Driver(existingEmail, name));

		hibernateDataAccess.register(name, existingEmail, password);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1)).get(eq(Driver.class), eq(existingEmail));
		verify(mockTransaction, times(1)).rollback();
		verify(mockTransaction, never()).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test(expected = RuntimeException.class)
	public void testRegister_DataAccessError() throws DriverAlreadyExistsException {
		String name = "Error User";
		String email = "error@example.com";
		String password = "errorpass";

		when(mockSession.get(eq(Driver.class), eq(email))).thenThrow(new RuntimeException("DB error during get"));

		hibernateDataAccess.register(name, email, password);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1)).get(eq(Driver.class), eq(email));
		verify(mockTransaction, times(1)).rollback();
		verify(mockTransaction, never()).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test
	public void testLogin_Successful() {
		String email = "login@example.com";
		String password = "correctpass";
		Driver expectedDriver = new Driver(email, "Login User");
		expectedDriver.setPassword(password);

		when(mockSession.get(eq(Driver.class), eq(email))).thenReturn(expectedDriver);

		Driver actualDriver = hibernateDataAccess.login(email, password);

		assertNotNull("Returned driver should not be null", actualDriver);
		assertEquals("Returned driver should match the expected driver", expectedDriver, actualDriver);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1)).get(eq(Driver.class), eq(email));
		verify(mockTransaction, times(1)).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test
	public void testLogin_DriverNotFound() {
		String email = "nonexistent@example.com";
		String password = "anypass";

		when(mockSession.get(eq(Driver.class), eq(email))).thenReturn(null);

		Driver actualDriver = hibernateDataAccess.login(email, password);

		assertNull("Returned driver should be null if not found", actualDriver);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1)).get(eq(Driver.class), eq(email));
		verify(mockTransaction, times(1)).rollback();
		verify(mockTransaction, never()).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test
	public void testLogin_IncorrectPassword() {
		String email = "user@example.com";
		String correctPassword = "correctpass";
		String incorrectPassword = "wrongpass";
		Driver foundDriver = new Driver(email, "User");
		foundDriver.setPassword(correctPassword);

		when(mockSession.get(eq(Driver.class), eq(email))).thenReturn(foundDriver);

		Driver actualDriver = hibernateDataAccess.login(email, incorrectPassword);

		assertNull("Returned driver should be null for incorrect password", actualDriver);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1)).get(eq(Driver.class), eq(email));
		verify(mockTransaction, times(1)).rollback();
		verify(mockTransaction, never()).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test(expected = RuntimeException.class)
	public void testLogin_DataAccessError() {
		String email = "error@example.com";
		String password = "anypass";

		when(mockSession.get(eq(Driver.class), eq(email))).thenThrow(new RuntimeException("DB error during login get"));

		hibernateDataAccess.login(email, password);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1)).get(eq(Driver.class), eq(email));
		verify(mockTransaction, times(1)).rollback();
		verify(mockTransaction, never()).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test
	public void testGetRides_Successful() {
		List<Ride> expectedRides = Arrays.asList(commonRide);

		when(mockSession.createQuery("FROM Ride r WHERE r.fromCity = :from AND r.toCity = :to AND r.date = :date"))
				.thenReturn(mockQuery);
		when(mockQuery.list()).thenReturn(expectedRides);
		when(mockQuery.setParameter(eq("from"), eq(commonRide.getFromCity()))).thenReturn(mockQuery);
		when(mockQuery.setParameter(eq("to"), eq(commonRide.getToCity()))).thenReturn(mockQuery);
		when(mockQuery.setParameter(eq("date"), eq(commonRide.getDate()))).thenReturn(mockQuery);

		List<Ride> actualRides = hibernateDataAccess.getRides(commonRide.getFromCity(), commonRide.getToCity(),
				commonRide.getDate());

		assertNotNull("Returned list should not be null", actualRides);
		assertEquals("Returned list should match expected rides", expectedRides, actualRides);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1))
				.createQuery("FROM Ride r WHERE r.fromCity = :from AND r.toCity = :to AND r.date = :date");
		verify(mockQuery, times(1)).setParameter("from", commonRide.getFromCity());
		verify(mockQuery, times(1)).setParameter("to", commonRide.getToCity());
		verify(mockQuery, times(1)).setParameter("date", commonRide.getDate());
		verify(mockQuery, times(1)).list();
		verify(mockTransaction, times(1)).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test
	public void testGetRides_EmptyList() {
		String from = "CityX";
		String to = "CityY";
		Date date = new Date();
		when(mockSession.createQuery("FROM Ride r WHERE r.fromCity = :from AND r.toCity = :to AND r.date = :date"))
				.thenReturn(mockQuery);
		when(mockQuery.list()).thenReturn(Collections.emptyList());
		when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);

		List<Ride> actualRides = hibernateDataAccess.getRides(from, to, date);

		assertNotNull("Returned list should not be null", actualRides);
		assertTrue("Returned list should be empty", actualRides.isEmpty());

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1))
				.createQuery("FROM Ride r WHERE r.fromCity = :from AND r.toCity = :to AND r.date = :date");
		verify(mockQuery, times(1)).setParameter("from", from);
		verify(mockQuery, times(1)).setParameter("to", to);
		verify(mockQuery, times(1)).setParameter("date", date);
		verify(mockQuery, times(1)).list();
		verify(mockTransaction, times(1)).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test(expected = RuntimeException.class)
	public void testGetRides_DataAccessError() {
		String from = "ErrCity";
		String to = "ErrDest";
		Date date = new Date();
		when(mockSession.createQuery("FROM Ride r WHERE r.fromCity = :from AND r.toCity = :to AND r.date = :date"))
				.thenReturn(mockQuery);
		when(mockQuery.list()).thenThrow(new RuntimeException("DB error during getRides list()"));
		when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery); // Stub param setting

		hibernateDataAccess.getRides(from, to, date);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1))
				.createQuery("FROM Ride r WHERE r.fromCity = :from AND r.toCity = :to AND r.date = :date");
		verify(mockQuery, times(1)).list();
		verify(mockTransaction, times(1)).rollback();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test
	public void testGetThisMonthDatesWithRides_Successful() {
		String from = "CityA";
		String to = "CityB";
		Calendar today = Calendar.getInstance();
		int month = today.get(Calendar.MONTH);
		int year = today.get(Calendar.YEAR);
		if (month == 12) {
			month = 1;
			year += 1;
		}
		Date testDate = UtilDate.newDate(year, month, 15);
		Date firstDay = UtilDate.firstDayMonth(testDate);
		Date lastDay = UtilDate.lastDayMonth(testDate);

		List<Date> expectedDates = Arrays.asList(testDate);

		when(mockSession.createQuery(
				"SELECT DISTINCT r.date FROM Ride r WHERE r.fromCity = :fromParam AND r.toCity = :toParam AND r.date BETWEEN :firstDay AND :lastDay"))
				.thenReturn(mockQuery);
		when(mockQuery.list()).thenReturn(expectedDates);
		when(mockQuery.setParameter(eq("fromParam"), eq(from))).thenReturn(mockQuery);
		when(mockQuery.setParameter(eq("toParam"), eq(to))).thenReturn(mockQuery);
		when(mockQuery.setParameter(eq("firstDay"), eq(firstDay))).thenReturn(mockQuery);
		when(mockQuery.setParameter(eq("lastDay"), eq(lastDay))).thenReturn(mockQuery);

		List<Date> actualDates = hibernateDataAccess.getThisMonthDatesWithRides(from, to, testDate);

		assertNotNull("Returned list should not be null", actualDates);
		assertEquals("Returned list should match expected dates", expectedDates, actualDates);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1)).createQuery(
				"SELECT DISTINCT r.date FROM Ride r WHERE r.fromCity = :fromParam AND r.toCity = :toParam AND r.date BETWEEN :firstDay AND :lastDay");
		verify(mockQuery, times(1)).setParameter("fromParam", from);
		verify(mockQuery, times(1)).setParameter("toParam", to);
		verify(mockQuery, times(1)).setParameter("firstDay", firstDay);
		verify(mockQuery, times(1)).setParameter("lastDay", lastDay);
		verify(mockQuery, times(1)).list();
		verify(mockTransaction, times(1)).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test
	public void testGetThisMonthDatesWithRides_EmptyList() {
		String from = "NoRidesFrom";
		String to = "NoRidesTo";
		Calendar today = Calendar.getInstance();
		int month = today.get(Calendar.MONTH);
		int year = today.get(Calendar.YEAR);
		if (month == 12) {
			month = 1;
			year += 1;
		}
		Date testDate = UtilDate.newDate(year, month, 15);
		Date firstDay = UtilDate.firstDayMonth(testDate);
		Date lastDay = UtilDate.lastDayMonth(testDate);
		when(mockSession.createQuery(
				"SELECT DISTINCT r.date FROM Ride r WHERE r.fromCity = :fromParam AND r.toCity = :toParam AND r.date BETWEEN :firstDay AND :lastDay"))
				.thenReturn(mockQuery);
		when(mockQuery.list()).thenReturn(Collections.emptyList());
		when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);

		List<Date> actualDates = hibernateDataAccess.getThisMonthDatesWithRides(from, to, testDate);

		assertNotNull("Returned list should not be null", actualDates);
		assertTrue("Returned list should be empty", actualDates.isEmpty());

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1)).createQuery(
				"SELECT DISTINCT r.date FROM Ride r WHERE r.fromCity = :fromParam AND r.toCity = :toParam AND r.date BETWEEN :firstDay AND :lastDay");
		verify(mockQuery, times(1)).setParameter("fromParam", from);
		verify(mockQuery, times(1)).setParameter("toParam", to);
		verify(mockQuery, times(1)).setParameter("firstDay", firstDay);
		verify(mockQuery, times(1)).setParameter("lastDay", lastDay);
		verify(mockQuery, times(1)).list();
		verify(mockTransaction, times(1)).commit();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}

	@Test(expected = RuntimeException.class)
	public void testGetThisMonthDatesWithRides_DataAccessError() {
		String from = "ErrorFrom";
		String to = "ErrorTo";
		Date testDate = new Date();
		Date firstDay = UtilDate.firstDayMonth(testDate);
		Date lastDay = UtilDate.lastDayMonth(testDate);

		when(mockSession.createQuery(
				"SELECT DISTINCT r.date FROM Ride r WHERE r.fromCity = :fromParam AND r.toCity = :toParam AND r.date BETWEEN :firstDay AND :lastDay"))
				.thenReturn(mockQuery);
		when(mockQuery.list()).thenThrow(new RuntimeException("DB error during date retrieval"));
		when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);

		hibernateDataAccess.getThisMonthDatesWithRides(from, to, testDate);

		verify(mockSessionFactory, times(1)).openSession();
		verify(mockSession, times(1)).beginTransaction();
		verify(mockSession, times(1)).createQuery(
				"SELECT DISTINCT r.date FROM Ride r WHERE r.fromCity = :fromParam AND r.toCity = :toParam AND r.date BETWEEN :firstDay AND :lastDay");
		verify(mockQuery, times(1)).setParameter("fromParam", from);
		verify(mockQuery, times(1)).setParameter("toParam", to);
		verify(mockQuery, times(1)).setParameter("firstDay", firstDay);
		verify(mockQuery, times(1)).setParameter("lastDay", lastDay);
		verify(mockQuery, times(1)).list();
		verify(mockTransaction, times(1)).rollback();
		verify(mockSession, times(1)).close();
		verifyNoMoreInteractions(mockSessionFactory, mockSession, mockTransaction, mockQuery);
	}
}