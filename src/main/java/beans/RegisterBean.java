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
	
	private BLFacade facadeBL;
	
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
		
		String error = "ErrorQuery";
		if (name == null || name.trim().isEmpty()) {
			context.addMessage("regUser",
					new FacesMessage(MessageHelper.getMessage(error)));
			return null;
		}
		if (email == null || email.trim().isEmpty()) {
			context.addMessage("regEmail",
					new FacesMessage(MessageHelper.getMessage(error)));
			return null;
		}
		if (password == null || password.trim().isEmpty()) {
			context.addMessage("regPass",
					new FacesMessage(MessageHelper.getMessage(error)));
			return null;
		}
		if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
			context.addMessage("confirmPass",
					new FacesMessage(MessageHelper.getMessage(error)));
			return null;
		}

		if (!password.equals(confirmPassword)) {
			context.addMessage("regPass",
					new FacesMessage(MessageHelper.getMessage("ErrorPassword")));
			return null;
		}

		if (!email.contains("@") || !email.contains(".")) {
			context.addMessage("regEmail",
					new FacesMessage(MessageHelper.getMessage("ErrorEmail")));
			return null;
		}
	
		Driver driver;
		try {
			driver = facadeBL.register(name, email, password);
			if (driver != null) {
				context.addMessage(null,
						new FacesMessage(MessageHelper.getMessage("SuccessRegister")));
			}
		} catch (DriverAlreadyExistsException e) {
			context.addMessage(null,
					new FacesMessage(MessageHelper.getMessage("ErrorExistEmail")));
		}

		clearForm();

		return null;
	}

	private void clearForm() {
		this.name = null;
		this.email = null;
		this.password = null;
		this.confirmPassword = null;
	}
}
