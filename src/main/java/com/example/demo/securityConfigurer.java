package com.example.demo;

import com.example.demo.services.userDetailService;
import com.example.demo.util.jwtRequestFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
public class securityConfigurer extends WebSecurityConfigurerAdapter{
    @Autowired
    private userDetailService myUserDetailService;
    
    @Autowired
    private jwtRequestFilter jwtRequestFilterp;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
        //load in user details using custom userdetail service
        auth.userDetailsService(myUserDetailService);
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception{
        //part to disable security for selected sites. *note null string not allowed for list of sites
        http.csrf().disable().authorizeRequests().antMatchers("/api/authenticate","/api/getNasaPicJ",
        "/api/checkUser","/api/checkUser2","/api/addUser","/api/addUser","/api/sendEmail","/api/gmap",
        "/assets/images/**","/index.html**","/*")
        .permitAll().
        //antMatchers("/assets/**").permitAll().
        anyRequest().authenticated().
        and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(jwtRequestFilterp,UsernamePasswordAuthenticationFilter.class);
    }

    
    @Bean
    public PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean()
        throws Exception {
        return super.authenticationManagerBean();
    }

}
