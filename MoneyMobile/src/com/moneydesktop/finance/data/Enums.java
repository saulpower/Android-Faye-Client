package com.moneydesktop.finance.data;

public class Enums {
    
    public enum TabletFragments {
        ACCOUNT_SUMMARY_TABLET (0),
        ACCOUNT_TYPES_TABLET (1),
        SETTINGS_TABLET (2),
        TRANSACTIONS_TABLET (3);
        
        private int index;
        
        TabletFragments(int index) {
            this.index = index;
        }
        
        public static TabletFragments fromInteger(Integer value) {
            
            switch(value) {
            case 0:
                return ACCOUNT_SUMMARY_TABLET;
            case 1:
                return ACCOUNT_TYPES_TABLET;
            case 2:
                return SETTINGS_TABLET;
            case 3:
                return TRANSACTIONS_TABLET;
            }
            
            return null;
        }
        
        public int index() {
            return index;
        }
        
    };
    
    public enum HandsetFragments {
        
    }

    public enum TxFilter {
        ALL, UNCLEARED
    }
    
	public enum TxType {
		INSERT, UPDATE, DELETE
	}

	public enum LockType {
		CHANGE, LOCK, NEW
	}
	
	public enum NavDirection {
		NEXT, PREVIOUS
	}
    
    public enum FragmentType {

        DASHBOARD(0),
        ACCOUNT_TYPES(1),
        TRANSACTIONS(2),
        SETTINGS(3), 
        POPUP_TAGS(10),
        POPUP_CATEGORIES(11),
        LOCK_SCREEN(12),
        TRANSACTIONS_PAGE(13),
        ACCOUNT_SUMMARY(14),
        SPENDING_SUMMARY(15),
        BUDGET_SUMMARY(16),
        TRANSACTION_SUMMARY(17),
        TRANSACTIONS_SUMMARY_TABLET (18);
        ACCOUNT_SETTINGS(19);
        private final int index;
        
        FragmentType(int index) {
            this.index = index;
        }
        
        public static FragmentType fromInteger(Integer value) {
            
            switch(value) {
            case 0:
                return DASHBOARD;
            case 1:
                return ACCOUNT_TYPES;
            case 2:
                return TRANSACTIONS;
            case 3:
                return SETTINGS;
            case 10:
                return POPUP_TAGS;
            case 11:
                return POPUP_CATEGORIES;
            case 12:
                return LOCK_SCREEN;
            case 13:
                return TRANSACTIONS_PAGE;
            case 14:
                return ACCOUNT_SUMMARY;
            case 15:
                return SPENDING_SUMMARY;
            case 16:
                return BUDGET_SUMMARY;
            case 17:
                return TRANSACTION_SUMMARY;
            }
            
            return null;
        }
        
        public int index() {
            return index;
        }
    }
	
	public enum DataState {
		
		DATA_STATE_UNCHANGED (0),
	    DATA_STATE_MODIFIED (1),
		DATA_STATE_NEW (2),
		DATA_STATE_DELETED (3);
		
		private final int index;
		
		DataState(int index) {
			this.index = index;
		}
		
		public static DataState fromInteger(Integer value) {
	        
			switch(value) {
	        case 0:
	            return DATA_STATE_UNCHANGED;
	        case 1:
	            return DATA_STATE_MODIFIED;
	        case 2:
	            return DATA_STATE_NEW;
	        case 3:
	            return DATA_STATE_DELETED;
	        }
	        
	        return null;
	    }
		
		public int index() {
			return index;
		}
	};
    
    public enum FilterType {
        
        FILTER_BANKS (1),
        FILTER_CATEGORIES (2),
        FILTER_TAGS (3),
        FILTER_PAYEES (4);
        
        private final int index;
        
        FilterType(int index) {
            this.index = index;
        }
        
        public static FilterType fromInteger(Integer value) {
            
            switch(value) {
            case 1:
                return FILTER_BANKS;
            case 2:
                return FILTER_CATEGORIES;
            case 3:
                return FILTER_TAGS;
            case 4:
                return FILTER_PAYEES;
            }
            
            return null;
        }
        
        public int index() {
            return index;
        }
    };
	
	public enum CollectionChangeType {
		
	    COLLECTION_CHANGE_ADD (1),
		COLLECTION_CHANGE_REMOVE (2);
		
		private final int index;
		
		CollectionChangeType(int index) {
			this.index = index;
		}
		
		public static CollectionChangeType fromInteger(Integer value) {
	        
			switch(value) {
	        case 1:
	            return COLLECTION_CHANGE_ADD;
	        case 2:
	            return COLLECTION_CHANGE_REMOVE;
	        }
	        
	        return null;
	    }
		
		public int index() {
			return index;
		}
	};
	
	public enum AccountExclusionFlags {
		
	    ACCOUNT_EXCLUSION_FLAGS_TRANSACTION_LIST        (1 << 0),
	    ACCOUNT_EXCLUSION_FLAGS_REPORTS               	(1 << 1),
	    ACCOUNT_EXCLUSION_FLAGS_ACCOUNT_LIST            (1 << 2),
	    ACCOUNT_EXCLUSION_FLAGS_BUDGETS                	(1 << 3),
	    ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_INCOME	(1 << 4),
	    ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_EXPENSES	(1 << 5),
	    ACCOUNT_EXCLUSION_FLAGS_ALL						(1 << 0
											    		|1 << 1
											    		|1 << 2
											    		|1 << 3
											    		|1 << 4
											    		|1 << 5);
	    
	    private final int index;
	    
	    AccountExclusionFlags(int index) {
	    	this.index = index;
	    }
	    
	    public int index() {
	    	return index;
	    }
	};
}
