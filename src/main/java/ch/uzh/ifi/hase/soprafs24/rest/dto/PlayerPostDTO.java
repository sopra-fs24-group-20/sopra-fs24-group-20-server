package ch.uzh.ifi.hase.soprafs24.rest.dto;


import org.apache.tomcat.jni.Local;

import java.time.LocalDate;

public class PlayerPostDTO {


  private String username;
  private String password;


  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
