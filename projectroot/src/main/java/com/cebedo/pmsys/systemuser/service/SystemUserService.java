package com.cebedo.pmsys.systemuser.service;

import java.util.List;

import com.cebedo.pmsys.systemuser.model.SystemUser;

public interface SystemUserService {

	public void initRoot();

	public List<SystemUser> list();

	public void create(SystemUser user);

	public void update(SystemUser user);

	public void update(SystemUser user, boolean systemOverride);

	public void delete(long id);

	public SystemUser getByID(long id);

	public SystemUser getByID(long id, boolean override);

	public SystemUser searchDatabase(String name);

}
