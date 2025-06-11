package beans;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import businessLogic.BLFacade;
import domain.Driver;
import exceptions.DriverAlreadyExistsException;

import java.io.Serializable;
import java.util.ResourceBundle;

@ManagedBean(name = "loginBean")
@SessionScoped
public class LoginBean implements Serializable {

	private String email;
	private String password;

	private Driver loggedInDriver;
	private boolean loggedIn;

	private BLFacade facadeBL;

	public LoginBean() {
		facadeBL = FacadeBean.getBusinessLogic();
		FacesContext.getCurrentInstance().getExternalContext().getFlash()
        .put("logged", false);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Driver getLoggedInDriver() {
		return loggedInDriver;
	}

	public void setLoggedInDriver(Driver loggedInDriver) {
		this.loggedInDriver = loggedInDriver;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public String login() {
		FacesContext context = FacesContext.getCurrentInstance();

		if (email == null || email.trim().isEmpty()) {
			context.addMessage("user", new FacesMessage(getMessage("ErrorQuery")));
			return null;
		}
		if (password == null || password.trim().isEmpty()) {
			context.addMessage("regPass", new FacesMessage(getMessage("ErrorQuery")));
			return null;
		}

		if (!email.contains("@") || !email.contains(".")) {
			context.addMessage("regEmail", new FacesMessage(getMessage("ErrorEmail")));
			return null;
		}

		Driver driver;
		try {
			driver = facadeBL.login(email, password);
			if (driver != null) {
				this.loggedInDriver = driver;
				this.loggedIn = true;

				email = null;
				password = null;
				
				FacesContext.getCurrentInstance().getExternalContext().getFlash()
                .put("logged", true);
				
				return "index";
			} else {
				this.loggedInDriver = null;
				this.loggedIn = false;
				context.addMessage(null, new FacesMessage(getMessage("ErrorLogin")));
			}
		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(getMessage("ErrorLogin")));
			e.printStackTrace();
		}
		return null;
	}

	public String logout() {
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		FacesContext.getCurrentInstance().getExternalContext().getFlash()
        .put("logged", false);
		
		return "/Login.xhtml?faces-redirect=true";
	}

	private String getMessage(String key) {
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		return bundle.getString(key);
	}

}