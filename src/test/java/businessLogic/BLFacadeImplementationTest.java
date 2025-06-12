package businessLogic;

import org.junit.Before; 
import org.junit.Test; 
import org.junit.runner.RunWith; 
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner; 

import dataAccess.HibernateDataAccess; 
import domain.Driver;

import static org.junit.Assert.*; 
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class BLFacadeImplementationTest {


    @Mock
    private HibernateDataAccess mockDbManager; 


    @InjectMocks
    private BLFacadeImplementation blFacade;

    private Driver genericTestDriver;
    
    @Before
    public void setUp() {
    	genericTestDriver = new Driver();
    	genericTestDriver.setEmail("pablo@gmail.com");
        genericTestDriver.setPassword("1234");
        
        doNothing().when(mockDbManager).open();
        doNothing().when(mockDbManager).close();
    }


    @Test
    public void testLogin_Successful() {
    	String testEmail = genericTestDriver.getEmail();
        String testPassword = genericTestDriver.getPassword();
        when(mockDbManager.login(testEmail, testPassword)).thenReturn(genericTestDriver);

        Driver actualDriver = blFacade.login(testEmail, testPassword);

        assertEquals(genericTestDriver, actualDriver);

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
}