package businessLogic;

import org.junit.Before; 
import org.junit.Test; 
import org.junit.runner.RunWith; 
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import configuration.UtilDate;
import dataAccess.HibernateDataAccess; 
import domain.Driver;
import domain.Ride;
import exceptions.DriverAlreadyExistsException;
import exceptions.RideAlreadyExistException;
import exceptions.RideMustBeLaterThanTodayException;

import static org.junit.Assert.*; 
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;


@RunWith(MockitoJUnitRunner.class)
public class BLFacadeImplementationTest {

    @Mock
    private HibernateDataAccess mockDbManager; 

    @InjectMocks
    private BLFacadeImplementation blFacade;

    private Driver defaultRegisteredDriver;
    
    private Ride expectedRide;
    
    private String fromCity;
    private String toCity;
    private Date date;
    private Integer nPlaces;
    private float price;
    
    @Before

    public void setUp() {
        doNothing().when(mockDbManager).open();
        doNothing().when(mockDbManager).close();
        
        // Default Driver for tests
        defaultRegisteredDriver = new Driver("pablo@gmail.com", "Pablo");
        defaultRegisteredDriver.setPassword("1234");
        
        // Default Ride for tests
        fromCity = "Donostia";
        toCity = "Bilbo";
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 7);
        date = cal.getTime();
        nPlaces = 4;
        price = 15.50f;

        expectedRide = new Ride(fromCity, toCity, date, nPlaces, price, defaultRegisteredDriver);
    }
    
    private Ride callCreateRide(Date dateToUse) throws RideMustBeLaterThanTodayException, RideAlreadyExistException {
        return blFacade.createRide(fromCity, toCity, dateToUse, nPlaces, price, defaultRegisteredDriver.getEmail());
    }

    private void setupMockCreateRideSuccessful(Ride rideToReturn) throws RideMustBeLaterThanTodayException, RideAlreadyExistException {
        when(mockDbManager.createRide(fromCity, toCity, date, nPlaces, price, defaultRegisteredDriver.getEmail()))
            .thenReturn(rideToReturn);
    }
    
    @Test
    public void testCreateRide_Successful() throws RideMustBeLaterThanTodayException, RideAlreadyExistException {
        when(mockDbManager.createRide(fromCity, toCity, date, nPlaces, price, defaultRegisteredDriver.getEmail()))
            .thenReturn(expectedRide);

        Ride actualRide = callCreateRide(date);

        assertNotNull("The created ride should not be null.", actualRide);
        assertEquals("The returned ride should match the expected ride.", expectedRide, actualRide);

        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).createRide(fromCity, toCity, date, nPlaces, price, defaultRegisteredDriver.getEmail());
        verify(mockDbManager, times(1)).close();
        verifyNoMoreInteractions(mockDbManager);
    }

    @Test(expected = RideMustBeLaterThanTodayException.class)
    public void testCreateRide_DateNotLaterThanToday() throws RideMustBeLaterThanTodayException, RideAlreadyExistException {
        Date pastDate = new Date(); 
        when(mockDbManager.createRide(fromCity, toCity, pastDate, nPlaces, price, defaultRegisteredDriver.getEmail()))
            .thenThrow(new RideMustBeLaterThanTodayException());

        callCreateRide(pastDate);

        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).createRide(fromCity, toCity, pastDate, nPlaces, price, defaultRegisteredDriver.getEmail());
        verify(mockDbManager, times(1)).close();
        verifyNoMoreInteractions(mockDbManager);
    }

    @Test(expected = RideAlreadyExistException.class)
    public void testCreateRide_AlreadyExists() throws RideMustBeLaterThanTodayException, RideAlreadyExistException {
        // Arrange
        when(mockDbManager.createRide(fromCity, toCity, date, nPlaces, price, defaultRegisteredDriver.getEmail()))
            .thenThrow(new RideAlreadyExistException());

        callCreateRide(date);

        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).createRide(fromCity, toCity, date, nPlaces, price, defaultRegisteredDriver.getEmail());
        verify(mockDbManager, times(1)).close();
        verifyNoMoreInteractions(mockDbManager);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateRide_DataAccessError() throws RideMustBeLaterThanTodayException, RideAlreadyExistException {
        when(mockDbManager.createRide(fromCity, toCity, date, nPlaces, price, defaultRegisteredDriver.getEmail()))
            .thenThrow(new RuntimeException("Database connection lost"));

        callCreateRide(date);

        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).createRide(fromCity, toCity, date, nPlaces, price, defaultRegisteredDriver.getEmail());
        verify(mockDbManager, times(1)).close();
        verifyNoMoreInteractions(mockDbManager);
    }
    
    @Test
    public void testGetDepartCities_Successful() {
        List<String> expectedCities = Arrays.asList("Donostia", "Bilbo", "Eibar", "Madril", "Madrid");
        when(mockDbManager.getDepartCities()).thenReturn(expectedCities);

        List<String> actualCities = blFacade.getDepartCities();

        assertNotNull("The returned list of cities should not be null.", actualCities);
        assertEquals("The returned list of cities should match the expected list.", expectedCities, actualCities);
        assertEquals("The returned list should have the correct number of cities.", expectedCities.size(), actualCities.size());
        assertTrue("The returned list should contain all expected cities.", actualCities.containsAll(expectedCities));

        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).getDepartCities();
        verify(mockDbManager, times(1)).close();

        verifyNoMoreInteractions(mockDbManager);
    }

    @Test(expected = RuntimeException.class) 
    public void testGetDepartCities_DataAccessError() {
       
    	when(mockDbManager.getDepartCities()).thenThrow(new RuntimeException("Database connection error"));

        blFacade.getDepartCities();

        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).getDepartCities();
        verify(mockDbManager, times(1)).close();
        verifyNoMoreInteractions(mockDbManager);
    }
    
    @Test
    public void testGetDestinationCities_Successful() {
        String fromCity = "Donostia";
        List<String> expectedDestinationCities = Arrays.asList("Bilbo", "Gazteiz", "Iruña");
        when(mockDbManager.getArrivalCities(fromCity)).thenReturn(expectedDestinationCities);

        List<String> actualDestinationCities = blFacade.getDestinationCities(fromCity);

        assertNotNull("The returned list of destination cities should not be null.", actualDestinationCities);
        assertEquals("The returned list should match the expected list.", expectedDestinationCities, actualDestinationCities);
        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).getArrivalCities(fromCity);
        verify(mockDbManager, times(1)).close();
        verifyNoMoreInteractions(mockDbManager);
    }
    
    @Test
    public void testGetDestinationCities_EmptyList() {
        String fromCity = "NonExistentCity";
        List<String> expectedDestinationCities = Collections.emptyList();
        when(mockDbManager.getArrivalCities(fromCity)).thenReturn(expectedDestinationCities);

        List<String> actualDestinationCities = blFacade.getDestinationCities(fromCity);

        assertNotNull("The returned list of destination cities should not be null.", actualDestinationCities);
        assertTrue("The returned list of destination cities should be empty.", actualDestinationCities.isEmpty());
        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).getArrivalCities(fromCity);
        verify(mockDbManager, times(1)).close();
        verifyNoMoreInteractions(mockDbManager);
    }
    
    @Test(expected = RuntimeException.class)
    public void testGetDestinationCities_DataAccessError() {
        String fromCity = "AnyCity";
        when(mockDbManager.getArrivalCities(fromCity)).thenThrow(new RuntimeException("DB access failed"));

        blFacade.getDestinationCities(fromCity);

        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).getArrivalCities(fromCity);
        verify(mockDbManager, times(1)).close();
        verifyNoMoreInteractions(mockDbManager);
    }    
    
    @Test
    public void testGetRides_Successful() {
        String fromCity = "Donostia";
        String toCity = "Bilbo";
        Calendar today = Calendar.getInstance();
		int month = today.get(Calendar.MONTH);
		int year = today.get(Calendar.YEAR);
		if (month == 12) {
			month = 1;
			year += 1;
		}
        Date testDate = UtilDate.newDate(year, month, 15);

        Driver driver1 = new Driver("driver1@gmail.com", "Aitor Fernandez");
		Driver driver2 = new Driver("driver2@gmail.com", "Ane Gaztañaga");
        
        Ride ride1 = new Ride(fromCity, toCity, testDate, 4, 7, driver1);
        Ride ride2 = new Ride(fromCity, toCity, testDate, 3, 3, driver2);
        List<Ride> expectedRides = Arrays.asList(ride1, ride2);

        when(mockDbManager.getRides(fromCity, toCity, testDate)).thenReturn(expectedRides);

        List<Ride> actualRides = blFacade.getRides(fromCity, toCity, testDate);

        assertNotNull("The returned list of rides should not be null.", actualRides);
        assertEquals("The returned list of rides should match the expected list.", expectedRides, actualRides);
        assertEquals("The returned list should have the correct number of rides.", expectedRides.size(), actualRides.size());

        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).getRides(fromCity, toCity, testDate);
        verify(mockDbManager, times(1)).close();
        verifyNoMoreInteractions(mockDbManager);
    }

    @Test
    public void testGetRides_EmptyList() {
        String fromCity = "Unknown";
        String toCity = "Destination";
        Date testDate = new Date();
        List<Ride> expectedRides = Collections.emptyList();

        when(mockDbManager.getRides(fromCity, toCity, testDate)).thenReturn(expectedRides);

        List<Ride> actualRides = blFacade.getRides(fromCity, toCity, testDate);

        assertNotNull("The returned list of rides should not be null even if empty.", actualRides);
        assertTrue("The returned list of rides should be empty.", actualRides.isEmpty());

        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).getRides(fromCity, toCity, testDate);
        verify(mockDbManager, times(1)).close();
        verifyNoMoreInteractions(mockDbManager);
    }

    @Test(expected = RuntimeException.class)
    public void testGetRides_DataAccessError() {
        String fromCity = "AnyFrom";
        String toCity = "AnyTo";
        Date testDate = new Date();

        when(mockDbManager.getRides(fromCity, toCity, testDate)).thenThrow(new RuntimeException("DB error during getRides"));

        blFacade.getRides(fromCity, toCity, testDate);

        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).getRides(fromCity, toCity, testDate);
        verify(mockDbManager, times(1)).close();
        verifyNoMoreInteractions(mockDbManager);
    }
    
    @Test
    public void testGetThisMonthDatesWithRides_Successful() {
        String fromCity = "OriginCity";
        String toCity = "DestCity";
        Calendar today = Calendar.getInstance();
		int month = today.get(Calendar.MONTH);
		int year = today.get(Calendar.YEAR);
		if (month == 12) {
			month = 1;
			year += 1;
		}
        Date testDate = UtilDate.newDate(year, month, 15);
        List<Date> expectedDates = Arrays.asList(
        		testDate
        );

        when(mockDbManager.getThisMonthDatesWithRides(fromCity, toCity, testDate)).thenReturn(expectedDates);

        List<Date> actualDates = blFacade.getThisMonthDatesWithRides(fromCity, toCity, testDate);

        assertNotNull("The returned list of dates should not be null.", actualDates);
        assertEquals("The returned list should match the expected dates.", expectedDates, actualDates);
        assertEquals("The list size should be correct.", expectedDates.size(), actualDates.size());

        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).getThisMonthDatesWithRides(fromCity, toCity, testDate);
        verify(mockDbManager, times(1)).close();
        verifyNoMoreInteractions(mockDbManager);
    }

    @Test
    public void testGetThisMonthDatesWithRides_EmptyList() {
        String fromCity = "NoRidesFrom";
        String toCity = "NoRidesTo";
        Date queryDate = new Date();

        List<Date> expectedDates = Collections.emptyList();
        when(mockDbManager.getThisMonthDatesWithRides(fromCity, toCity, queryDate)).thenReturn(expectedDates);

        List<Date> actualDates = blFacade.getThisMonthDatesWithRides(fromCity, toCity, queryDate);

        assertNotNull("The returned list should not be null, even if empty.", actualDates);
        assertTrue("The returned list should be empty.", actualDates.isEmpty());

        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).getThisMonthDatesWithRides(fromCity, toCity, queryDate);
        verify(mockDbManager, times(1)).close();
        verifyNoMoreInteractions(mockDbManager);
    }

    @Test(expected = RuntimeException.class)
    public void testGetThisMonthDatesWithRides_DataAccessError() {
        String fromCity = "ErrorFrom";
        String toCity = "ErrorTo";
        Date queryDate = new Date();

        when(mockDbManager.getThisMonthDatesWithRides(fromCity, toCity, queryDate))
            .thenThrow(new RuntimeException("Simulated database connection error"));

        blFacade.getThisMonthDatesWithRides(fromCity, toCity, queryDate);

        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).getThisMonthDatesWithRides(fromCity, toCity, queryDate);
        verify(mockDbManager, times(1)).close();
        verifyNoMoreInteractions(mockDbManager);
    }
    
    @Test
    public void testLogin_Successful() {
    	String testEmail = defaultRegisteredDriver.getEmail();
        String testPassword = defaultRegisteredDriver.getPassword();
        when(mockDbManager.login(testEmail, testPassword)).thenReturn(defaultRegisteredDriver);

        Driver actualDriver = blFacade.login(testEmail, testPassword);
        
        assertEquals(testEmail, actualDriver.getEmail());
        assertEquals(testPassword, actualDriver.getPassword());

        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).login(testEmail, testPassword);
        verify(mockDbManager, times(1)).close();

        verifyNoMoreInteractions(mockDbManager);
    }

    @Test
    public void testLogin_FailedInvalidCredentials() {
        String testEmail = "wrong@example.com";
        String testPassword = "wrongpassword";

        when(mockDbManager.login(testEmail, testPassword)).thenReturn(null);

        Driver actualDriver = blFacade.login(testEmail, testPassword);
        
        assertNull("The returned driver should be null for invalid credentials.", actualDriver);

        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).login(testEmail, testPassword);
        verify(mockDbManager, times(1)).close();
        verifyNoMoreInteractions(mockDbManager);
    }
    
    @Test
    public void testRegister_Successful() throws DriverAlreadyExistsException {
    	String testName = "John Doe";
    	String testEmail = "johndoe@email.com";
    	String testPassword = "admin";
    	Driver testDriver = new Driver(testEmail, testName);
    	testDriver.setPassword(testPassword);
    	
        when(mockDbManager.register(testName, testEmail, testPassword)).thenReturn(testDriver);

        Driver actualDriver = blFacade.register(testName, testEmail, testPassword);

        assertNotNull("The returned driver should not be null.", actualDriver);
        assertEquals("The registered driver should have the correct email.", testEmail, actualDriver.getEmail());
        assertEquals("The registered driver should have the correct name.", testName, actualDriver.getName());
        
        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).register(testName, testEmail, testPassword);
        verify(mockDbManager, times(1)).close();

        verifyNoMoreInteractions(mockDbManager);
    }

    @Test(expected = DriverAlreadyExistsException.class)
    public void testRegister_DriverAlreadyExistsException() throws DriverAlreadyExistsException {

        when(mockDbManager.register(defaultRegisteredDriver.getName(), defaultRegisteredDriver.getEmail(), defaultRegisteredDriver.getPassword()))
            .thenThrow(new DriverAlreadyExistsException());

        blFacade.register(defaultRegisteredDriver.getName(), defaultRegisteredDriver.getEmail(), defaultRegisteredDriver.getPassword());

        verify(mockDbManager, times(1)).open();
        verify(mockDbManager, times(1)).register(defaultRegisteredDriver.getName(), defaultRegisteredDriver.getEmail(), defaultRegisteredDriver.getPassword());
        verify(mockDbManager, times(1)).close();
        verifyNoMoreInteractions(mockDbManager);
    }
}