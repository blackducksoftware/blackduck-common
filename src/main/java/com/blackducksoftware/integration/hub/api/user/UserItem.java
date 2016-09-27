package com.blackducksoftware.integration.hub.api.user;

import java.util.UUID;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.util.HubUrlParser;

public class UserItem extends HubItem {
	public static final String USER_URL_IDENTIFIER = "users";

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

	public UUID getUserId() throws MissingUUIDException {
		if (getMeta() == null || getMeta().getHref() == null) {
			return null;
		}
		return HubUrlParser.getUUIDFromURLString(USER_URL_IDENTIFIER, getMeta().getHref());
	}

	@Override
	public String toString() {
		return "UserItem [userName=" + userName + ", firstName=" + firstName + ", lastName=" + lastName + ", email="
				+ email + ", type=" + type + ", active=" + active + "]";
	}

}
