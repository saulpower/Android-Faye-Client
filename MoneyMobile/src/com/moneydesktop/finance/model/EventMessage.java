package com.moneydesktop.finance.model;

import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.database.PowerQuery;
import com.moneydesktop.finance.util.Enums.LockType;
import com.moneydesktop.finance.util.Enums.NavDirection;
import com.moneydesktop.finance.views.AnchorView;

import java.util.HashMap;

public class EventMessage {
    
    public class AnchorChangeEvent extends EventMessage {
        private AnchorView mLeft;
        private AnchorView mRight;
        
        public AnchorChangeEvent(AnchorView left, AnchorView right) {
            mLeft = left;
            mRight = right;
        }
        
        public AnchorView getLeft() {
            return mLeft;
        }
        
        public AnchorView getRight() {
            return mRight;
        }
    }
    
	public class AuthEvent extends EventMessage {}
	public class DefaultsEvent extends EventMessage {}
	
	public class FilterEvent extends EventMessage {
	    
	    private PowerQuery mQueries;
	    
	    public FilterEvent(PowerQuery queries) {
	        mQueries = queries;
	    }
	    
	    public PowerQuery getQueries() {
	        return mQueries;
	    }
	}
	
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

    public class ParentAnimationEvent extends EventMessage {
        protected Boolean mOutAnimation;
        protected Boolean mFinished;
        protected boolean mIsNav = false;

        public ParentAnimationEvent(boolean outAnimation, boolean finished) {
            mOutAnimation = outAnimation;
            mFinished = finished;
        }
        
        public ParentAnimationEvent(boolean outAnimation, boolean finished, boolean isNav) {
            mOutAnimation = outAnimation;
            mFinished = finished;
            mIsNav = isNav;
        }
        
        public boolean isNavigation() {
            return mIsNav;
        }
        
        public boolean isOutAnimation() {
            return mOutAnimation;
        }
        
        public boolean isFinished() {
            return mFinished;
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
	
   public class BankStatusUpdateEvent extends EventMessage {
        
        protected Bank mBank;

        public BankStatusUpdateEvent(Bank bank) {
            this.mBank = bank;
        }
        
        public Bank getUpdatedBank() {
            return mBank;
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
