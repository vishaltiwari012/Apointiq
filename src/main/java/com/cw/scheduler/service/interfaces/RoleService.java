package com.cw.scheduler.service.interfaces;

import com.cw.scheduler.entity.Role;


public interface RoleService {
    Role getByName(String name);
    Role getCustomerRole();
    Role getAdminRole();
    Role getServiceProviderRole();
}
