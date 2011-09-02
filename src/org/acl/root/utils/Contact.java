package org.acl.root.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.acl.root.R;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * This class represents a contact in the Android contact list. It implements
 * the builder pattern.
 * 
 * @author Francisco PŽrez-Sorrosal (fperez)
 * 
 */
public class Contact implements Serializable {

	private static final String TAG = "Contact";

	private static final long serialVersionUID = 83472973490823798L;

	private  String id;
	private  String name;
	private transient Bitmap photo;
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
		private Bitmap photo;
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

		public Builder addPhoto(Bitmap userPhoto) {
			photo = userPhoto;
			return this;
		}

		public Builder addPhoneNumber(String phoneNumber) {
			phoneNumbers.add(phoneNumber);
			return this;
		}

		public Builder addEmailAddress(String emailAddress) {
			emailAddresses.add(emailAddress);
			return this;
		}

		public Builder poBox(String poBox) {
			this.poBox = poBox;
			return this;
		}

		public Builder street(String street) {
			this.street = street;
			return this;
		}

		public Builder city(String city) {
			this.city = city;
			return this;
		}

		public Builder state(String state) {
			this.state = state;
			return this;
		}

		public Builder postalCode(String postalCode) {
			this.postalCode = postalCode;
			return this;
		}

		public Builder country(String country) {
			this.country = country;
			return this;
		}

		public Builder type(String type) {
			this.type = type;
			return this;
		}

		public Contact build() {
			return new Contact(this);
		}

	}

	private Contact(Builder builder) {
		id = builder.id;
		name = builder.name;
		photo = builder.photo;
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

	public Bitmap getPhoto() {
		return photo;
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

	private void readObject(ObjectInputStream s) throws IOException,
			ClassNotFoundException {
		s.defaultReadObject();
	}

	@Override
	public String toString() {
		return getName() + " " + getPhoneNumbers();
	}

	public static Contact getContactFromAndroidUserContacts(Activity activity, String id) {

		Contact contact = null;

		// Original line: Cursor cursor =  activity.managedQuery(intent.getData(), null, null, null, null);
		Cursor cursor =  activity.managedQuery(
				ContactsContract.Contacts.CONTENT_URI
				, null
				, ContactsContract.Contacts._ID + " = ?"
				, new String[] { id }
				, null);
		activity.startManagingCursor(cursor);
		Log.d(TAG, "Elements: " + cursor.getCount());

		while (cursor.moveToNext()) {           
			String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)); 

			Contact.Builder contactBuilder = new Contact.Builder(contactId, name);
	
            int photoID=cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
    			Bitmap userPhoto;
            if (photoID!=0) {
                Uri uri=ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
                InputStream in = ContactsContract.Contacts.openContactPhotoInputStream(activity.getContentResolver(), uri);
                userPhoto = BitmapFactory.decodeStream(in);
            } else {
		        userPhoto = BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon);
		    }
            contactBuilder.addPhoto(userPhoto);

			String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

			if (hasPhone.equalsIgnoreCase("1"))
				hasPhone = "true";
			else
				hasPhone = "false" ;

			if (Boolean.parseBoolean(hasPhone)) {
				Cursor phones = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,null, null);
				while (phones.moveToNext()) {
					String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					// When receiving a call the number includes spaces, brackets and hyphens.
					phoneNumber = phoneNumber.replaceAll("[\\s\\-()]", "");
					contactBuilder.addPhoneNumber(phoneNumber);
					Log.d(TAG, "Phone Number: " + phoneNumber);
				}
				phones.close();
			}

			// Find Email Addresses
			Cursor emails = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,null,ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId,null, null);
			while (emails.moveToNext()) {
				String emailAddress = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
				contactBuilder.addEmailAddress(emailAddress);
			}
			emails.close();

			Cursor address = activity.getContentResolver().query(
					ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
					null,
					ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = " + contactId,
					null, null);
			while (address.moveToNext()) { 
				// These are all private class variables, don't forget to create them.
				contactBuilder.poBox(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX)));
				contactBuilder.street(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)));
				contactBuilder.city(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)));
				contactBuilder.state(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)));
				contactBuilder.postalCode(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)));
				contactBuilder.country(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)));
				contactBuilder.type(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE)));
			}  //address.moveToNext()   

			contact = contactBuilder.build();
		}  //while (cursor.moveToNext())        
		cursor.close();
		activity.stopManagingCursor(cursor);
		return contact;
	}
}
