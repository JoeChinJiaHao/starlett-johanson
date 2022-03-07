package com.example.demo.services;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.demo.ServerSideApplication;
import com.example.demo.repositories.sqlRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class userDetailService implements UserDetailsService {
    @Autowired
    private sqlRepository myRepo;

    private final Logger logger = Logger.getLogger(ServerSideApplication.class.getName());

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        // TODO Auto-generated method stub
        //use sql method to get username and pass from myRepo
        //is password required?
        String password = myRepo.getPasswordGivenUsername(userName);
        //need a method to get password out
        //logger.log(Level.INFO,userName+">>>>>>"+password);
        return new User(userName,password,new ArrayList<>());
        
    }
}
 