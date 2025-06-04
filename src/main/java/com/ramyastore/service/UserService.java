package com.ramyastore.service;

import com.ramyastore.exception.UserException;
import com.ramyastore.model.User;

public interface UserService {

	public User findUserProfileByJwt(String jwt) throws UserException;
	
	public User findUserByEmail(String email) throws UserException;


}
