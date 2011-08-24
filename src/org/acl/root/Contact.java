package org.acl.root;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class represents a contact in the Android contact list. 
 * It implements the builder pattern.
 * 
 * @author Francisco PŽrez-Sorrosal (fperez)
 *
 */
public class Contact implements Serializable {
	
	private static final long serialVersionUID = 83472973490823798L ;
	
	private String id;
	private String name;
	private ArrayList<String> phoneNumbers;
	private ArrayList<String> emailAddresses;
	private String poBox;
	private String street;
	private String city;
	private String state;
	private String postalCode;
	private String country;
	private String type;

	public static class Builder {
		// Required
		private String id;
		private String name;
		
		// Optional
		private ArrayList<String> phoneNumbers = new ArrayList<String>();
		private ArrayList<String> emailAddresses = new ArrayList<String>();
		private String poBox = "";
		private String street = "";
		private String city = "";
		private String state = "";
		private String postalCode = "";
		private String country = "";
		private String type = "";
		
		public Builder(String id, String name) {
			this.id = id;
			this.name = name;
		}
		
		public Builder addPhoneNumber(String phoneNumber) {
			phoneNumbers.add(phoneNumber); return this;
		}
		
		public Builder addEmailAddress(String emailAddress) {
			emailAddresses.add(emailAddress); return this;
		}

		public Builder poBox(String poBox) {
			this.poBox = poBox; return this;
		}

		public Builder street(String street) {
			this.street = street; return this;
		}

		public Builder city(String city) {
			this.city = city; return this;
		}

		public Builder state(String state) {
			this.state = state; return this;
		}
		
		public Builder postalCode(String postalCode) {
			this.postalCode = postalCode; return this;
		}

		public Builder country(String country) {
			this.country = country; return this;
		}

		public Builder type(String type) {
			this.type = type; return this;
		}
		
		public Contact build() {
			return new Contact(this);
		}

	}
	
	private Contact(Builder builder) {
		id = builder.id;
		name = builder.name;
		phoneNumbers = builder.phoneNumbers;
		emailAddresses = builder.emailAddresses;
		poBox = builder.poBox;
		street = builder.street;
		city = builder.city;
		state = builder.state;
		postalCode = builder.postalCode;
		country = builder.country;
		type = builder.type;
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<String> getPhoneNumbers() {
		return phoneNumbers;
	}
	
	public ArrayList<String> getEmailAddresses() {
		return emailAddresses;
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		s.defaultReadObject();
	}
	
	@Override
	public String toString() {
		return getName() + " " + getPhoneNumbers();
	}

}
