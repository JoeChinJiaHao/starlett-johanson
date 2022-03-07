package com.example.demo;

public class SQL {
    
    public static final String SQL_Check_User="select * from user where username like ?";
    public static final String SQL_ADD_ONE_USER="insert into user (username,password,email,verification,verified) values (?,SHA1(?),?,?,?)";
    public static final String SQL_HASH_PASSWORD="select SHA1(?) as 'hashedPassword'";
    public static final String SQL_CREATE_TEMP_TABLE="create table temp(username char(64),password char(255),primary key (username)); ";
    public static final String SQL_INSERT_INTO_TEMP_TABLE="insert into temp (username, password) values (%s,SHA1(%s));";
    public static final String SQL_Query_TEMP_TABLE="select * from temp;";
    public static final String SQL_DROP_TEMP_TABLE="drop table if exists temp;";
    public static final String SQL_UPDATE_VERIFIED="UPDATE user SET verified='1' where (username= ?)";
    public static final String SQL_CHECK_WATCHLIST="select * from watchlist where (username=?) and (asteroid_id=?)";
    public static final String SQL_ADD_ONE_WATCHLIST="insert into watchlist (username,asteroid_name,asteroid_id,next_approach_date) values (?,?,?,?)";
    public static final String SQL_GET_USER_WATCHLIST="select * from watchlist where (username=?)";
}
