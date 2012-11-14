package com.moneydesktop.finance.model;

import java.util.HashMap;

public class EventMessage {

	public class DefaultsEvent extends EventMessage {}
	public class AuthEvent extends EventMessage {}
	public class LoginEvent extends EventMessage {}
	
	public class SyncEvent extends EventMessage {
		
		protected boolean finished;

		public SyncEvent(boolean finished) {
			this.finished = finished;
		}
		
		public boolean isFinished() {
			return finished;
		}

		public void setFinished(boolean finished) {
			this.finished = finished;
		}
	}
	
	protected HashMap<String, Object> info;
	protected String message;
	
	public HashMap<String, Object> getInfo() {
		return info;
	}
	
	public void setInfo(HashMap<String, Object> info) {
		this.info = info;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}
