package com.moneydesktop.finance.model;

import android.graphics.Bitmap;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.Enums.LockType;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.database.PowerQuery;
import com.moneydesktop.finance.views.AnchorView;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

	public class UpdateSpecificBankStatus extends EventMessage {
		private Bank mBank;
		
		public UpdateSpecificBankStatus(Bank bank) {
			this.mBank = bank;
		}
		
		public Bank getBank() {
			return mBank;
		}
	}
	
	public class MfaQuestionsRecieved extends EventMessage {	
		private List<String> mList;
		
		public MfaQuestionsRecieved(List<String> mQuestionLabels) {
			this.mList = mQuestionLabels;
		}
		
		public List<String> getQuestions() {
			return mList;
		}
	}
	
	public class SaveInstitutionFinished extends EventMessage {
		private JSONObject object;
	
		public SaveInstitutionFinished(JSONObject jsonResponse) {
			this.object = jsonResponse;
		}
		
		public JSONObject getJsonResponse() {
			return object;
		}
	}
	
	
	public class UpdateCredentialsFinished extends EventMessage {
		private JSONObject object;
	
		public UpdateCredentialsFinished(JSONObject jsonResponse) {
			this.object = jsonResponse;
		}
		
		public JSONObject getJsonResponse() {
			return object;
		}
	}
	
	public class GetLogonCredentialsFinished extends EventMessage {
		private boolean isAddedForFirstTime = false;
		private List<String> logonTxtLabels;
		
	       public GetLogonCredentialsFinished(boolean isAccountAddedForFirstTime, List<String> loginLabels) {
	           this.isAddedForFirstTime = isAccountAddedForFirstTime;
	           this.logonTxtLabels = loginLabels;
	       }
	       
	       public boolean isAddedForFistTime() {
	           return isAddedForFirstTime;
	       }
	       
	       public List<String> getLogonLabels() {
	           return logonTxtLabels;
	       }
		
	}
	
	public class BackEvent extends EventMessage {}
	public class ChartImageEvent extends EventMessage {
		
		private Bitmap image;
		
		public ChartImageEvent(Bitmap image) {
			this.image = image;
		}
		
		public Bitmap getImage() {
			return image;
		}
	}
    public class DatabaseSaveEvent extends EventMessage {
    	
    	private List<Class<?>> mClasses = new ArrayList<Class<?>>();
    	
    	public DatabaseSaveEvent(List<Class<?>> classes) {
    		mClasses = classes;
    	}
    	
    	public List<Class<?>> getChangedClassesList() {
    		return mClasses;
    	}
    	
    	public boolean didDatabaseChange() {
    		return mClasses.size() > 0;
    	}
    }
	public class FeedbackEvent extends EventMessage {}
	
	public class FilterEvent extends EventMessage {
	    
	    private PowerQuery mQueries;
	    
	    public FilterEvent(PowerQuery queries) {
	        mQueries = queries;
	    }
	    
	    public PowerQuery getQuery() {
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
	
	public class MenuEvent extends EventMessage {
	    
	    private int mGroupPosition, mChildPosition;
	    private FragmentType mFragmentType;
	    
	    public MenuEvent(int groupPosition, int childPosition, FragmentType fragmentType) {
	    	mGroupPosition = groupPosition;
	    	mChildPosition = childPosition;
	    	mFragmentType = fragmentType;
	    }
	    
	    public int getGroupPosition() {
	        return mGroupPosition;
	    }
	    
	    public int getChildPosition() {
	        return mChildPosition;
	    }
	    
	    public FragmentType getFragmentType() {
	        return mFragmentType;
	    }

        /**
         * Provides a unique int for the combined group and child position
         *
         * @return the combined id
         */
        public int getAction() {
            return ((mGroupPosition << 8) + mChildPosition);
        }
	}

	public class NavigationEvent extends EventMessage {
		
		protected Boolean mShowing;
		protected FragmentType mType;
		
		public NavigationEvent(boolean showing) {
			this.mShowing = showing;
		}
		
		public FragmentType getType() {
			return mType;
		}
		
		public Boolean isShowing() {
			return mShowing;
		}
	}

    public class VisibilityEvent extends EventMessage {

        private FragmentType mType;
        private boolean mVisible;

        public VisibilityEvent(FragmentType type, boolean visible) {
            mType = type;
            mVisible = visible;
        }

        public FragmentType getType() {
            return mType;
        }

        public boolean isVisible() {
            return mVisible;
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
   
   public class RefreshAccountEvent extends EventMessage {
       
       protected Bank mBank;

       public RefreshAccountEvent(Bank bank) {
           this.mBank = bank;
       }
       
       public Bank getRefreshedBank() {
           return mBank;
       }

   }
   
   public class ReloadBannersEvent extends EventMessage {
       public ReloadBannersEvent() {
           super();
       }
   }
   
   public class BankDeletedEvent extends EventMessage {
       protected Bank mBank;

       public BankDeletedEvent(Bank bank) {
           this.mBank = bank;
       }
       
       public Bank getDeletedBank() {
           return mBank;
       }
   }
	
   public class RemoveAccountTypeEvent extends EventMessage {
       protected AccountType mAccountType;

       public RemoveAccountTypeEvent(AccountType accountToBeRemoved) {
           this.mAccountType = accountToBeRemoved;
       }
       
       public AccountType getAccountType() {
           return mAccountType;
       }
   }
   
	public class CheckRemoveBankEvent extends EventMessage {
		protected Bank mBank;
		
		public CheckRemoveBankEvent(Bank bank) {
			this.mBank = bank;
		}
			       
        public Bank getBank() {
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
