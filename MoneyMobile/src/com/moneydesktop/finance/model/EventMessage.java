package com.moneydesktop.finance.model;

import java.util.HashMap;

import com.moneydesktop.finance.util.Enums.LockType;
import com.moneydesktop.finance.util.Enums.NavDirection;

public class EventMessage {

    public class ParentAnimationEvent extends EventMessage {
        protected Boolean mOutAnimation;
        protected Boolean mFinished;

        public ParentAnimationEvent(boolean outAnimation, boolean finished) {
            mOutAnimation = outAnimation;
            mFinished = finished;
        }
        
        public boolean isOutAnimation() {
            return mOutAnimation;
        }
        
        public boolean isFinished() {
            return mFinished;
        }
    }
	public class AuthEvent extends EventMessage {}
	public class DefaultsEvent extends EventMessage {}
	public class LoginEvent extends EventMessage {}
	public class LogoutEvent extends EventMessage {}
	public class LockEvent extends EventMessage {
		
		protected LockType mType;

		public LockEvent(LockType type) {
			this.mType = type;
		}
		
		public LockType getType() {
			return mType;
		}
	}
	public class NavigationEvent extends EventMessage {
		
		protected Boolean mShowing;
		protected NavDirection mDirection;
		
		public NavigationEvent() {
			this.mShowing = null;
			this.mDirection = null;
		}
		
		public NavigationEvent(boolean showing) {
			this.mShowing = showing;
			this.mDirection = null;
		}
		
		public NavigationEvent(NavDirection direction) {
			this.mShowing = null;
			this.mDirection = direction;
		}
		
		public Boolean isShowing() {
			return mShowing;
		}
		
		public NavDirection getDirection() {
			return mDirection;
		}
	}
	
	public class SyncEvent extends EventMessage {
		
		protected boolean mFinished;

		public SyncEvent(boolean finished) {
			this.mFinished = finished;
		}
		
		public boolean isFinished() {
			return mFinished;
		}

		public void setFinished(boolean finished) {
			this.mFinished = finished;
		}
	}
	
	protected HashMap<String, Object> mInfo;
	protected String mMessage;
	
	public HashMap<String, Object> getInfo() {
		return mInfo;
	}
	
	public void setInfo(HashMap<String, Object> info) {
		this.mInfo = info;
	}
	
	public String getMessage() {
		return mMessage;
	}
	
	public void setMessage(String message) {
		this.mMessage = message;
	}
}
