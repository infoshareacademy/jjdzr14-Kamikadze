package com.travel.BizTravel360.delegation.exeptions;

import java.io.IOException;

public class DelegationNotFoundException extends IOException {
    public DelegationNotFoundException(Long delegationId) {
        super(String.format("No found delegation with property id: %d", delegationId));
    }
}
