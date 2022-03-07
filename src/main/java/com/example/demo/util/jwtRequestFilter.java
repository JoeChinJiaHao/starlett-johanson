package com.example.demo.util;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.demo.services.userDetailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class jwtRequestFilter extends OncePerRequestFilter{

    @Autowired
    private userDetailService myUserDetailService;

    @Autowired
    private jwtUtil jwtutility;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // TODO Auto-generated method stub,  check if jwt is valid in header
        final String authorizationHeader=request.getHeader("Authorization");
        String username =null;
        String jwt=null;

        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            jwt=authorizationHeader.substring(7);
            username=jwtutility.extractUserName(jwt);
        }
        if(username !=null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails=this.myUserDetailService.loadUserByUsername(username);
            if(jwtutility.validateToken(jwt, userDetails)){
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null,userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);    
            }
        }
        chain.doFilter(request,response);
    }
    
}
