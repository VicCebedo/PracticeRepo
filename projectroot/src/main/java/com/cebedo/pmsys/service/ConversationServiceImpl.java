package com.cebedo.pmsys.service;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cebedo.pmsys.domain.Conversation;
import com.cebedo.pmsys.domain.Message;
import com.cebedo.pmsys.enums.AuditAction;
import com.cebedo.pmsys.helper.AuthHelper;
import com.cebedo.pmsys.helper.LogHelper;
import com.cebedo.pmsys.model.Company;
import com.cebedo.pmsys.model.SystemUser;
import com.cebedo.pmsys.repository.ConversationValueRepo;
import com.cebedo.pmsys.token.AuthenticationToken;

@Service
public class ConversationServiceImpl implements ConversationService {

    private static Logger logger = Logger.getLogger(Company.OBJECT_NAME);
    private LogHelper logHelper = new LogHelper();
    private AuthHelper authHelper = new AuthHelper();
    private ConversationValueRepo conversationValueRepo;

    public void setConversationValueRepo(ConversationValueRepo repo) {
	this.conversationValueRepo = repo;
    }

    @Transactional
    @Override
    public void markRead(Conversation obj) {
	AuthenticationToken auth = this.authHelper.getAuth();
	if (this.authHelper.isActionAuthorized(obj)) {
	    // If object is read, not need to mark it.
	    if (!obj.isRead()) {

		// Else, rename it.
		String readKey = Conversation.constructKey(
			obj.getContributors(), true);
		this.conversationValueRepo.rename(obj, readKey);

		// Using the new renamed key,
		// update the object.
		obj.setRead(true);
		this.conversationValueRepo.set(obj);
	    }
	} else {

	    // If not authorized,
	    // log the event.
	    logger.warn(this.logHelper.logUnauthorized(auth,
		    AuditAction.MARK_READ, Conversation.OBJECT_NAME,
		    obj.getKey(), StringUtils.join(obj.getContributors(), ",")));
	}
    }

    @Transactional
    @Override
    public void set(Conversation obj) {
	;
    }

    @Transactional
    @Override
    public Conversation get(String key) {
	AuthenticationToken auth = this.authHelper.getAuth();

	// Get necessary objects.
	Conversation conversation = this.conversationValueRepo.get(key);
	String name = StringUtils.join(conversation.getContributors(), ",");

	if (this.authHelper.isActionAuthorized(conversation)) {
	    // If valid, log then return.
	    logger.info(this.logHelper.logGetObject(auth,
		    Conversation.OBJECT_NAME, key, name));
	    return conversation;
	}
	// If not valid,
	// log as warning then return empty object.
	logger.warn(this.logHelper.logUnauthorized(auth, AuditAction.GET,
		Conversation.OBJECT_NAME, key, name));
	return new Conversation();
    }

    @Transactional
    @Override
    public Set<String> keys(String pattern) {
	return this.conversationValueRepo.keys(pattern);
    }

    /**
     * Get all conversations of a specific user.
     */
    @Transactional
    @Override
    public Set<Conversation> getAllConversations(SystemUser user) {
	// message:conversation:read:true:id:.1.234.56.
	// Construct wildcard pattern with company key part.
	Company company = user.getCompany();
	long companyID = company == null ? 0 : company.getId();
	String companyKeyPart = Company.OBJECT_NAME + ":" + companyID + ":";
	String pattern = companyKeyPart + Message.OBJECT_NAME
		+ ":conversation:read:*:id:*." + user.getId() + ".*";

	// Get the auth object.
	// Get necessary keys.
	AuthenticationToken auth = this.authHelper.getAuth();
	Set<String> keys = this.conversationValueRepo.keys(pattern);
	Set<Conversation> conversations = new HashSet<Conversation>();

	// Loop through each key.
	for (String key : keys) {
	    Conversation conversation = this.conversationValueRepo.get(key);

	    // If the conversation is authorized,
	    // add then continue.
	    if (this.authHelper.isActionAuthorized(conversation)) {
		conversations.add(conversation);
		continue;
	    }

	    // Else, log a warning.
	    String name = StringUtils.join(conversation.getContributors(), ",");
	    logger.warn(this.logHelper.logUnauthorized(auth, AuditAction.GET,
		    Conversation.OBJECT_NAME, conversation.getKey(), name));
	}
	return conversations;
    }

}
