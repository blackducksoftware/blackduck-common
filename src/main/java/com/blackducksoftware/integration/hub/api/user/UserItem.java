package com.blackducksoftware.integration.hub.api.user;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class UserItem extends HubItem {
	private final String userName;
	private final String firstName;
	private final String lastName;
	private final String email;
	private final UserType type;
	private final boolean active;

	public UserItem(final MetaInformation meta, final String userName, final String firstName, final String lastName,
			final String email, final UserType type, final boolean active) {
		super(meta);

		this.userName = userName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.type = type;
		this.active = active;
	}

	public String getUserName() {
		return userName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmail() {
		return email;
	}

	public UserType getType() {
		return type;
	}

	public boolean isActive() {
		return active;
	}

	@Override
	public String toString() {
		return "UserItem [userName=" + userName + ", firstName=" + firstName + ", lastName=" + lastName + ", email="
				+ email + ", type=" + type + ", active=" + active + "]";
	}

}
