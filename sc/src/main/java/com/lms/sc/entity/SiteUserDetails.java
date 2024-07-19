package com.lms.sc.entity;

import java.util.Collection;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@SuppressWarnings("serial")
public class SiteUserDetails implements UserDetails {
	
	private final SiteUser user;
	private final Collection<? extends GrantedAuthority> authorities;
	
	public SiteUserDetails(SiteUser siteUser, Collection<? extends GrantedAuthority> authorities) {
        this.user = siteUser;
        this.authorities = authorities;
    }
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}
	
	public String getName() {
		return user.getName();
	}
	
	public String getTellNumber() {
		return user.getTellNumber();
	}

	public String getProfileImage() {
		return user.getProfileImage();
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
