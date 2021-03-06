package com.cebedo.pmsys.bean;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.core.MessageCreator;

public class JMSMessageCreatorImpl implements MessageCreator {

    private JMSMessage message;

    public JMSMessageCreatorImpl() {
	;
    }

    public JMSMessageCreatorImpl(JMSMessage msg) {
	setMessage(msg);
    }

    @Override
    public Message createMessage(Session session) throws JMSException {
	return session.createObjectMessage(this.message);
    }

    public JMSMessage getMessage() {
	return message;
    }

    public void setMessage(JMSMessage message) {
	this.message = message;
    }

}
