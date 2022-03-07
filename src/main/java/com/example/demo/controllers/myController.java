package com.example.demo.controllers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;

import com.example.demo.constant;
import com.example.demo.securityConfigurer;
import com.example.demo.models.AuthenticationRequest;
import com.example.demo.models.AuthenticationResponse;
import com.example.demo.repositories.sqlRepository;
import com.example.demo.services.serverService;
import com.example.demo.services.userDetailService;
import com.example.demo.util.jwtUtil;
import com.fasterxml.jackson.annotation.JsonValue;
import com.mysql.cj.x.protobuf.MysqlxDatatypes.Any;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;

@RestController()
public class myController{
    @Autowired
    private sqlRepository myRepo;
    
    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;

    @Autowired
    private userDetailService myUserDetailService;

    @Autowired
    private jwtUtil jwtTokenUtil;

    private final Logger logger = Logger.getLogger(myController.class.getName());

    @GetMapping(path="/api/getNasaPicJ")
    public ResponseEntity<?> getPicJson() throws IOException{
        String apikey=System.getenv("NasaApiKey");
        String baseURL=constant.NASA_BASE_APOD_URL+apikey;
        logger.log(Level.INFO, baseURL);
        //get result and post back
        URI myUri=URI.create(baseURL);
        HttpHeaders headers=new HttpHeaders();
        headers.add("Content-Type","application/json");
        RequestEntity<Void> req = RequestEntity
                                        .get(myUri)
                                        .accept(MediaType.APPLICATION_JSON)
                                        .build();
        try{
            RestTemplate template = new RestTemplate();
            ResponseEntity<String> resp=template.exchange(req,String.class);
            try(InputStream is = new ByteArrayInputStream(resp.getBody().getBytes(StandardCharsets.UTF_8))){
                JsonReader reader = Json.createReader(new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8)));
                JsonObject data = reader.readObject();
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(data);
            }catch(Error err){
                logger.log(Level.INFO, err.getMessage());
                return new ResponseEntity<>(headers,HttpStatus.OK);
            }
        }catch(Error err){
            logger.log(Level.INFO, err.getMessage());
            return new ResponseEntity<>(headers,HttpStatus.OK);
        }
        //return ResponseEntity.status(HttpStatus.ACCEPTED).body();
    }

    @PostMapping(path="/api/checkUser")
    public ResponseEntity<Boolean> checkingUser(@RequestBody String payload) throws IOException{

        return ResponseEntity.ok(myRepo.userExists(payload));
    }
    @PostMapping(path="/api/checkUser2")
    public ResponseEntity<Boolean> checkIfUserExists2(@RequestBody String payload) throws IOException{
        //logger.log(Level.INFO,">>>>>here");
        
        return ResponseEntity.ok(myRepo.userPassValid(payload));
    }

    @PostMapping(path="/api/addUser")
    public ResponseEntity<Boolean> addUser(@RequestBody String payload)throws IOException{
        
        return ResponseEntity.ok(myRepo.addUser(payload));
    }

    
    @GetMapping(path="/api/hello")
    public ResponseEntity<String> hello(){
        JsonObjectBuilder JOB= Json.createObjectBuilder();
            JOB.add("name", "Hello");
        return ResponseEntity.ok(JOB.build().toString());
    }

    //for jwt to work, an authenticate API end point is required, that accepts userID and pass, then returns a JWT as response
    //client has to hold onto the jwt and send it in subsequent header

    @RequestMapping(path="/api/authenticate",method=RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest )throws Exception{
        try{
            
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        }catch(BadCredentialsException e){
            throw new Exception("Incorrect username or password",e);
        };
        final UserDetails userDetails=myUserDetailService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }

    @PostMapping(path="/api/verifyEmail")
    public ResponseEntity<Boolean> VerifyUserEmail(@RequestBody String payload) throws Exception{
        return ResponseEntity.ok(myRepo.checkUserEmailCode(payload));
    }

    @PostMapping(path="/api/myVerificationStatus")
    public ResponseEntity<Boolean>VerifyUserEmailStatus(@RequestBody String payload)throws Exception{
        return ResponseEntity.ok(myRepo.getVerificationStatus(payload)); 
    }

    @GetMapping(path="/api/AsteroidList")
    public ResponseEntity<?>getAsteriodList() throws IOException{
        String apikey=System.getenv("NasaApiKey");
        String baseURL=constant.NASA_BASE_NEO_URL+apikey;
        logger.log(Level.INFO, baseURL);
        //get result and post back
        URI myUri=URI.create(baseURL);
        HttpHeaders headers=new HttpHeaders();
        headers.add("Content-Type","application/json");
        RequestEntity<Void> req = RequestEntity
                                        .get(myUri)
                                        .accept(MediaType.APPLICATION_JSON)
                                        .build();
        try{
            RestTemplate template = new RestTemplate();
            ResponseEntity<String> resp=template.exchange(req,String.class);
            try(InputStream is = new ByteArrayInputStream(resp.getBody().getBytes(StandardCharsets.UTF_8))){
                JsonReader reader = Json.createReader(new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8)));
                JsonObject data = reader.readObject();
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(data);
            }catch(Error err){
                logger.log(Level.INFO, err.getMessage());
                return new ResponseEntity<>(headers,HttpStatus.OK);
            }
        }catch(Error err){
            logger.log(Level.INFO, err.getMessage());
            return new ResponseEntity<>(headers,HttpStatus.OK);
        }
    }
    @GetMapping(path="/api/AsteroidLookup")
    public ResponseEntity<?>getAsteroidLookup(@RequestParam String id)throws Exception{
        String apikey=System.getenv("NasaApiKey");
        String baseURL=String.format(constant.NASA_BASE_NEO_LOOKUP_URL,id)+apikey;
        logger.log(Level.INFO, baseURL);
        //get result and post back
        URI myUri=URI.create(baseURL);
        HttpHeaders headers=new HttpHeaders();
        headers.add("Content-Type","application/json");
        RequestEntity<Void> req = RequestEntity
                                        .get(myUri)
                                        .accept(MediaType.APPLICATION_JSON)
                                        .build();
        try{
            RestTemplate template = new RestTemplate();
            ResponseEntity<String> resp=template.exchange(req,String.class);
            try(InputStream is = new ByteArrayInputStream(resp.getBody().getBytes(StandardCharsets.UTF_8))){
                JsonReader reader = Json.createReader(new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8)));
                JsonObject data = reader.readObject();
                //jakarta.json.JsonValue diameterObj=data.get("estimated_diameter");
                
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(data);
            }catch(Error err){
                logger.log(Level.INFO, err.getMessage());
                return new ResponseEntity<>(headers,HttpStatus.OK);
            }
        }catch(Error err){
            logger.log(Level.INFO, err.getMessage());
            return new ResponseEntity<>(headers,HttpStatus.OK);
        }
    }
    @GetMapping(path="/api/AsteroidLookup2")
    public ResponseEntity<?>getAsteroidLookup2(@RequestParam String id)throws Exception{
        String apikey=System.getenv("NasaApiKey");
        String baseURL=String.format(constant.NASA_BASE_NEO_LOOKUP_URL,id)+apikey;
        logger.log(Level.INFO, baseURL);
        //get result and post back
        URI myUri=URI.create(baseURL);
        HttpHeaders headers=new HttpHeaders();
        headers.add("Content-Type","application/json");
        RequestEntity<Void> req = RequestEntity
                                        .get(myUri)
                                        .accept(MediaType.APPLICATION_JSON)
                                        .build();
        try{
            RestTemplate template = new RestTemplate();
            ResponseEntity<String> resp=template.exchange(req,String.class);
            try(InputStream is = new ByteArrayInputStream(resp.getBody().getBytes(StandardCharsets.UTF_8))){
                JsonReader reader = Json.createReader(new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8)));
                JsonObject data = reader.readObject();
                //jakarta.json.JsonValue diameterObj=data.get("estimated_diameter");
                JsonObject diaObj=(JsonObject) data.get("estimated_diameter");
                JsonObject diaObjMeter=(JsonObject) diaObj.get("meters");
                jakarta.json.JsonValue diaValue= diaObjMeter.get("estimated_diameter_min");
                String myString=diaValue.toString();
                logger.log(Level.INFO, ">>>>>>>"+myString);
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(myString);
            }catch(Error err){
                logger.log(Level.INFO, err.getMessage());
                return new ResponseEntity<>(headers,HttpStatus.OK);
            }
        }catch(Error err){
            logger.log(Level.INFO, err.getMessage());
            return new ResponseEntity<>(headers,HttpStatus.OK);
        }
    }

    @PostMapping(path="/api/checkWatchlist")
    public ResponseEntity<?>checkMyWatchlist(@RequestBody String payload) throws IOException{
        return ResponseEntity.ok(myRepo.checkWatchlist(payload)); 
    }
    @PostMapping(path="/api/addWatchlist")
    public ResponseEntity<?>addMyWatchlist(@RequestBody String payload) throws IOException{
        return ResponseEntity.ok(myRepo.addWatchlist(payload)); 
    }
    @PostMapping(path="/api/getMyWatchlist")
    public ResponseEntity<?>getMyWatchlist(@RequestBody String payload) throws IOException{
        return ResponseEntity.ok(myRepo.getMyWatchlist(payload)); 
    }
    @GetMapping(path="/api/gmap")
    public ResponseEntity<String>getGmap(){
        String mykey=System.getenv("testAppGmap");
        return ResponseEntity.ok(mykey);
    }
}
