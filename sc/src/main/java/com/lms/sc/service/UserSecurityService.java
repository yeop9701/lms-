package com.lms.sc.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.SiteUserDetails;
import com.lms.sc.repository.UserRepository;
import com.lms.sc.role.UserRole;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserSecurityService implements UserDetailsService {
	private final UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<SiteUser> _siteUser = this.userRepository.findByEmail(username);
		if(_siteUser.isEmpty()) {
			System.err.printf("사용자없음" + username);
			throw new UsernameNotFoundException("사용자 없다");
		}
		SiteUser siteUser = _siteUser.get();
		List<GrantedAuthority> authorities = new ArrayList<>();
		if("admin".equals(username)) {
			authorities.add(new SimpleGrantedAuthority(UserRole.ADMIN.getValue()));
		} else {
			authorities.add(new SimpleGrantedAuthority(UserRole.USER.getValue()));
		}
		return new SiteUserDetails(siteUser, authorities);
	}
}
