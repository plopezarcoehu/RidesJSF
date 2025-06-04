package beans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Locale;

@ManagedBean(name = "localeBean")
@SessionScoped
public class LocaleBean implements Serializable {

    private String language = "es"; 
    
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
        FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(language));
    }
}
