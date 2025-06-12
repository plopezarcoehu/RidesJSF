package beans;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import businessLogic.BLFacade;
import configuration.MessageHelper;
import domain.Driver;
import domain.Ride;
import exceptions.DriverAlreadyExistsException;

import java.io.Serializable;
import java.util.ResourceBundle;

@ManagedBean(name = "registerBean")
@SessionScoped
public class RegisterBean implements Serializable {

	private String name;
	private String email;
	private String password;
	private String confirmPassword;
	
	private transient BLFacade facadeBL;
	
	public RegisterBean() {
		facadeBL = FacadeBean.getBusinessLogic();
	}

	public String getName() {
		return name;
	}

	public void setName(String username) {
		this.name = username;
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

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String register() {
	    FacesContext context = FacesContext.getCurrentInstance();
	    
	    if (validateInputs(context)) {

	        performRegistration(context);
	        
	        clearForm();
	    }
	    
	    return null;
	}

	private boolean validateInputs(FacesContext context) {
	    String errorQuery = "ErrorQuery";

	    if (isNullOrEmpty(name)) {
	        addErrorMessage(context, "regUser", errorQuery);
	        return false;
	    }

	    if (isNullOrEmpty(email)) {
	        addErrorMessage(context, "regEmail", errorQuery);
	        return false;
	    }

	    if (!isValidEmailFormat(email)) {
	        addErrorMessage(context, "regEmail", "ErrorEmail");
	        return false;
	    }

	    if (isNullOrEmpty(password)) {
	        addErrorMessage(context, "regPass", errorQuery);
	        return false;
	    }

	    if (isNullOrEmpty(confirmPassword)) {
	        addErrorMessage(context, "confirmPass", errorQuery);
	        return false;
	    }
	    
	    if (!password.equals(confirmPassword)) {
	        addErrorMessage(context, "regPass", "ErrorPassword");
	        return false;
	    }
	    
	    return true;
	}

	private void performRegistration(FacesContext context) {
	    try {
	        Driver driver = facadeBL.register(name, email, password);
	        if (driver != null) {
	            addSuccessMessage(context, null, "SuccessRegister");
	        }
	    } catch (DriverAlreadyExistsException e) {
	        addErrorMessage(context, null, "ErrorExistEmail");
	    }
	}

	private boolean isNullOrEmpty(String value) {
	    return value == null || value.trim().isEmpty();
	}

	private boolean isValidEmailFormat(String email) {
	    return email.contains("@") && email.contains(".");
	}

	private void addErrorMessage(FacesContext context, String componentId, String messageKey) {
	    context.addMessage(componentId, new FacesMessage(MessageHelper.getMessage(messageKey)));
	}

	private void addSuccessMessage(FacesContext context, String componentId, String messageKey) {
	    context.addMessage(componentId, new FacesMessage(MessageHelper.getMessage(messageKey)));
	}

	private void clearForm() {
		this.name = null;
		this.email = null;
		this.password = null;
		this.confirmPassword = null;
	}
}
