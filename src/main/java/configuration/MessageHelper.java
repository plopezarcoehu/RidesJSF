package configuration;

import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class MessageHelper {

	private MessageHelper() {

	}

	public static String getMessage(String key) {
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		return bundle.getString(key);
	}

	public static void addGlobalMessage(String messageKey) {
		FacesContext context = FacesContext.getCurrentInstance();
		String mensaje = MessageHelper.getMessage(messageKey);
		FacesMessage message = new FacesMessage(mensaje);
		context.addMessage(null, message);
	}

}