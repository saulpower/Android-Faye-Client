package com.moneydesktop.finance.model;

import java.util.HashMap;

import com.moneydesktop.finance.util.Enums.LockType;

public class EventMessage {

	public class DefaultsEvent extends EventMessage {}
	public class AuthEvent extends EventMessage {}
	public class LoginEvent extends EventMessage {}
	public class LogoutEvent extends EventMessage {}
	public class LockEvent extends EventMessage {
		
		protected LockType type;

		public LockEvent(LockType type) {
			this.type = type;
		}
		
		public LockType getType() {
			return type;
		}
	}
	
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
