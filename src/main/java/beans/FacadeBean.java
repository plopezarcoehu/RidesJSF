package beans;

import businessLogic.BLFacade;
import businessLogic.BLFacadeImplementation;
import dataAccess.DataAccess;

public class FacadeBean {
    
    private static FacadeBean singleton = new FacadeBean();
    private static BLFacade facadeInterface;
    
    private FacadeBean() {
        try {
            facadeInterface = new BLFacadeImplementation(new DataAccess());
            System.out.println("FacadeBean Logic correct");
        } catch (Exception e) {
        	System.out.println("FacadeBean: negozioaren logika sortzean errorea: "+ e.getMessage());
            e.printStackTrace();
            facadeInterface = null;
        }
    }
    
    public static BLFacade getBusinessLogic() {
    	if (facadeInterface == null) {
            throw new RuntimeException("FacadeBean: negozioaren logika sortzean errorea: NULL");
        }
        return facadeInterface;
    }
    
    public static boolean isInitialized() {
        return facadeInterface != null;
    }
    
    public static void reinitialize() {
    	try {
            facadeInterface = new BLFacadeImplementation(new DataAccess());
        } catch (Exception e) {
            facadeInterface = null;
        }
    }
}