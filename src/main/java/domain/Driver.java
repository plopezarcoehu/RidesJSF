package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "driver")
public class Driver implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	private String email;
	private String name;
	private String password;

	@OneToMany(targetEntity = Ride.class, mappedBy = "driver", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(value = FetchMode.SELECT)
	private List<Ride> rides = new ArrayList<>();

	public Driver() {
		super();
	}

	public Driver(String email, String name) {
		this.email = email;
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String toString() {
		return email + ";" + name + rides;
	}

	/**
	 * This method creates a bet with a question, minimum bet ammount and percentual
	 * profit
	 * 
	 * @param question   to be added to the event
	 * @param betMinimum of that question
	 * @return Bet
	 */
	public Ride addRide(String from, String to, Date date, int nPlaces, float price) {
		Ride ride = new Ride(from, to, date, nPlaces, price, this);
		rides.add(ride);
		return ride;
	}

	/**
	 * This method checks if the ride already exists for that driver
	 * 
	 * @param from the origin location
	 * @param to   the destination location
	 * @param date the date of the ride
	 * @return true if the ride exists and false in other case
	 */
	public boolean doesRideExists(String from, String to, Date date) {
		for (Ride r : rides)
			if ((java.util.Objects.equals(r.getFromCity(), from)) && (java.util.Objects.equals(r.getToCity(), to))
					&& (java.util.Objects.equals(r.getDate(), date)))
				return true;

		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Driver other = (Driver) obj;
		return (email != other.email);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	public Ride removeRide(String from, String to, Date date) {
		boolean found = false;
		int index = 0;
		Ride r = null;
		while (!found && index <= rides.size()) {
			r = rides.get(++index);
			if ((java.util.Objects.equals(r.getFromCity(), from)) && (java.util.Objects.equals(r.getToCity(), to))
					&& (java.util.Objects.equals(r.getDate(), date)))
				found = true;
		}

		if (found) {
			rides.remove(index);
			return r;
		} else
			return null;
	}

}
