package beans;

import businessLogic.BLFacade;
import businessLogic.BLFacadeImplementation;
import dataAccess.HibernateDataAccess;

public class FacadeBean {
    
    private static FacadeBean singleton = new FacadeBean();
    private static BLFacade facadeInterface;
    
    private FacadeBean() {
        try {
            facadeInterface = new BLFacadeImplementation(new HibernateDataAccess());
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
}