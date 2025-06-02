package com.smartvehicle.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParentSignupReq extends SignupRequest {

    private String smParentId;
}
