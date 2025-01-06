package com.smart_vehicle.payload.response;

import com.smart_vehicle.models.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
  private String token;
  private String id;
  private String username;

  private Boolean twoFactorAuthentication;

  private List<String> roles;
}
