package beans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;

@ManagedBean(name = "index")
@SessionScoped
public class IndexBean implements Serializable {
    private String language;

    public void createRide() {
        System.out.println("Create Ride clicked");
    }
    
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
