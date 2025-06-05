package beans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;

@ManagedBean(name = "indexBean")
@SessionScoped
public class IndexBean implements Serializable {

    public String createRide() {
        return "create";
    }
    
    public String queryRides() {
    	return "query";
    }
}
