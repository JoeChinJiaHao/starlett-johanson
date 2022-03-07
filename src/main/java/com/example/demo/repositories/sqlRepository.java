package com.example.demo.repositories;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.RowSet;

import com.example.demo.SQL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties.Identityprovider.Verification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Repository;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;

@Repository
public class sqlRepository {
    @Autowired
    private JdbcTemplate template;

    @Autowired
    private JavaMailSender javaMailSender;

    private final Logger logger = Logger.getLogger(sqlRepository.class.getName());

    public boolean userExists(String payload)throws IOException{
        //logger.log(Level.INFO,">>>>>>>>>>"+payload);
        boolean flag =false;
        try(InputStream is = new ByteArrayInputStream(payload.getBytes())){
            //logger.log(Level.INFO, ">>>>");
            JsonReader reader =Json.createReader(new BufferedReader(new InputStreamReader(is)));
            JsonObject data = reader.readObject();
            logger.log(Level.INFO,data.get("username").toString());
            SqlRowSet rs = template.queryForRowSet(SQL.SQL_Check_User,data.get("username").toString().replace("\"",""));
            //logger.log(Level.INFO,"checking rs "+rs.toString());
            while(rs.next()){
                logger.log(Level.INFO, rs.toString());
                //check if password is correct
                
                flag=true;
            }
        }catch(Error err){
            logger.log(Level.INFO, err.getMessage());
        }   
        return flag;
    }
    
    public boolean userPassValid(String payload)throws IOException{
        boolean flag = false;
        
        try(InputStream is = new ByteArrayInputStream(payload.getBytes())){
            logger.log(Level.INFO, ">>>>");
            JsonReader reader =Json.createReader(new BufferedReader(new InputStreamReader(is)));
            JsonObject data = reader.readObject();
            
           
            //case where i just execute
            /* SqlRowSet rsHash = template.queryForRowSet(String.format("select sha1('%s') as 'hashedP';", data.get("password").toString().replace("\"", "") )); 
            String hashedPass="";
            if(rsHash.next()){
                hashedPass=rsHash.getString("hashedP");
                //logger.log(Level.INFO, ">>>>>"+rsHash.getString("hashedP"));
            } */
            String hashedPass=data.get("password").toString().replace("\"","");
            //logger.log(Level.INFO, "pass>>>>>"+hashedPass);
            SqlRowSet rs = template.queryForRowSet(SQL.SQL_Check_User,data.get("username").toString().replace("\"",""));
            
            String accPass="1";
            if(rs.next()){
                //logger.log(Level.INFO, rs.getString("password"));
                accPass=rs.getString("password");
                if(accPass.equals(hashedPass)){
                    flag=true;
                }
            }
            
        }catch(Error err){
            logger.log(Level.INFO, ">>>"+err.getMessage());
        }
        return flag;
    }

    public String getPasswordGivenUsername(String username){
        SqlRowSet rs = template.queryForRowSet(SQL.SQL_Check_User,username);
        String password=null;
        if(rs.next()){
            password=rs.getString("password");
        }
        return password;
    }
    private String generateVerificationCode(){

        Random rnd = new Random();
        Integer Number = rnd.nextInt(999999);

        return String.format("%06d",Number); 
    }

    public Boolean addUser(String payload)throws IOException{
        //logger.log(Level.INFO,">>>>>>");
        try(InputStream is = new ByteArrayInputStream(payload.getBytes())){
            //logger.log(Level.INFO, ">>>>"+payload);
            JsonReader reader =Json.createReader(new BufferedReader(new InputStreamReader(is)));
            JsonObject data = reader.readObject(); 
            String myVerificationCode = generateVerificationCode();
            this.template.update(SQL.SQL_ADD_ONE_USER,data.get("username").toString().replace("\"", ""),
                                data.get("password").toString().replace("\"", ""),data.get("email").toString().replace("\"", ""),
                                myVerificationCode.replace("\"", ""),false);
            //start verification process
            //send email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noReply@StarlettApp.com");
            message.setTo(data.get("email").toString());
            message.setSubject("Please verify your registration using the code provided.");
            message.setText("Please verify using the 6 digit code below \n \n "+myVerificationCode+"\n \n Thanks! \n Starlett App Team");
            javaMailSender.send(message);
            return true;
        }catch(Error err){
            logger.log(Level.INFO, err.getMessage());
            return false;
        }
    }
    public Boolean getVerificationStatus(String payload) throws IOException{
        try(InputStream is = new ByteArrayInputStream(payload.getBytes())){
            //logger.log(Level.INFO, ">>>>"+payload);
            JsonReader reader =Json.createReader(new BufferedReader(new InputStreamReader(is)));
            JsonObject data = reader.readObject();
            String username = data.get("username").toString().replace("\"","");
            SqlRowSet rs= template.queryForRowSet(SQL.SQL_Check_User,username);
            if(rs.next()){
                if(rs.getBoolean("verified")){
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }
        }catch(Error err){
            logger.log(Level.INFO, err.getMessage());
            return false;
        }
    }
    public Boolean checkUserEmailCode(String payload) throws IOException{
        try(InputStream is = new ByteArrayInputStream(payload.getBytes())){
            //logger.log(Level.INFO, ">>>>"+payload);
            JsonReader reader =Json.createReader(new BufferedReader(new InputStreamReader(is)));
            JsonObject data = reader.readObject(); 
            //payload will contain username and verification code
            String username=data.get("username").toString().replace("\"","");
            String verificationCode=data.get("verificationCode").toString().replace("\"","");
            //get values from sql
            SqlRowSet rs= template.queryForRowSet(SQL.SQL_Check_User,username);
            logger.log(Level.INFO, ">>>>>"+verificationCode);
            if(rs.next()){
                Integer storedCode=rs.getInt("verification");
                if(verificationCode.equals(storedCode.toString())){
                    //need to update status to verified
                    template.update(SQL.SQL_UPDATE_VERIFIED,username);
                    return true;
                }else{
                    return false;
                }
                
            }else{
                return false;
            }
        }catch(Error err){
            logger.log(Level.INFO, err.getMessage());
            return false;
        }
    }
    public Boolean checkWatchlist(String payload) throws IOException{
        try(InputStream is = new ByteArrayInputStream(payload.getBytes())){
            //logger.log(Level.INFO, ">>>>"+payload);
            JsonReader reader =Json.createReader(new BufferedReader(new InputStreamReader(is)));
            JsonObject data = reader.readObject(); 
            //payload will contain username and verification code
            String username=data.get("username").toString().replace("\"","");
            String asteroidId=data.get("asteroidId").toString().replace("\"","");
            //get values from sql
            SqlRowSet rs= template.queryForRowSet(SQL.SQL_CHECK_WATCHLIST,username,asteroidId);
            
            if(rs.next()){
                return true;
            }else{
                return false;
            }
        }catch(Error err){
            logger.log(Level.INFO, err.getMessage());
            return false;
        }
    }
    public Boolean addWatchlist(String payload) throws IOException{
        try(InputStream is = new ByteArrayInputStream(payload.getBytes())){
            //logger.log(Level.INFO, ">>>>"+payload);
            JsonReader reader =Json.createReader(new BufferedReader(new InputStreamReader(is)));
            JsonObject data = reader.readObject(); 
            //payload will contain username and verification code
            String username=data.get("username").toString().replace("\"","");
            String asteroidName=data.get("asteroid_name").toString().replace("\"","");
            String asteroidId=data.get("asteroid_id").toString().replace("\"","");
            String asteroidNextD=data.get("next_approach_date").toString().replace("\"","");
            
            //get values from sql
            template.update(SQL.SQL_ADD_ONE_WATCHLIST,username,asteroidName,asteroidId,asteroidNextD);
            return true;
            
        }catch(Error err){
            logger.log(Level.INFO, err.getMessage());
            return false;
        }
        
    }
    public String getMyWatchlist(String payload) throws IOException{
        try(InputStream is = new ByteArrayInputStream(payload.getBytes())){
            //logger.log(Level.INFO, ">>>>"+payload);
            JsonReader reader =Json.createReader(new BufferedReader(new InputStreamReader(is)));
            JsonObject data = reader.readObject(); 
            //payload will contain username and verification code
            String username=data.get("username").toString().replace("\"","");
            //get values from sql
            SqlRowSet rs= template.queryForRowSet(SQL.SQL_GET_USER_WATCHLIST,username);
            JsonObjectBuilder JO = Json.createObjectBuilder();
            JsonArrayBuilder JA = Json.createArrayBuilder();

            if(rs.next()){
                //add first entry
                JO.add("asteroid_name",rs.getString("asteroid_name"))
                    .add("asteroid_id",rs.getString("asteroid_id"))
                    .add("next_approach_date",rs.getString("next_approach_date"));
                JA.add(JO.build());
                JO=Json.createObjectBuilder();    
                //add rest of entry
                while(rs.next()){
                    JO.add("asteroid_name",rs.getString("asteroid_name"))
                    .add("asteroid_id",rs.getString("asteroid_id"))
                    .add("next_approach_date",rs.getString("next_approach_date"));
                    JA.add(JO.build());
                    JO=Json.createObjectBuilder(); 
                }
                JsonObjectBuilder myJO=Json.createObjectBuilder().add("watchlist",JA.build());
                return myJO.build().toString();
            }else{
                return null;
            }
            
            
        }catch(Error err){
            logger.log(Level.INFO, err.getMessage());
            return null;
        }
    }
}
