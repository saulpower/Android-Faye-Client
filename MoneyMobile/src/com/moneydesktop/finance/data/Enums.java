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

    
   
    public enum AddBankFragments {
        
        SELECT_METHOD (0),
        SELECT_INSTITUTION (1),
        LOGIN (2);
        
        private int index;
        
        AddBankFragments(int index) {
            this.index = index;
        }
        
        public static AddBankFragments fromInteger(Integer value) {
            
            switch(value) {
            case 1:
                return SELECT_METHOD;
            case 2:
                return SELECT_INSTITUTION;
            case 3:
                return LOGIN;
            }
            return null;
        }
        
        public int index() {
            return index;
        }        
        public static int size() {
			return 3;
        }

    };
    
    
   
    
    public enum BankRefreshStatus {
        
        STATUS_PENDING (1),
        STATUS_PROCESSING (2),
        STATUS_SUCCEEDED (3),
        STATUS_EXCEPTION (4),
        STATUS_LOGIN_FAILED (5),
        STATUS_MFA (6),
        STATUS_UPDATE_REQUIRED (7);
                
        
        private int index;
        
        BankRefreshStatus(int index) {
            this.index = index;
        }
        
        public static BankRefreshStatus fromInteger(Integer value) {
            
            switch(value) {
            case 1:
                return STATUS_PENDING;
            case 2:
                return STATUS_PROCESSING;
            case 3:
                return STATUS_SUCCEEDED;
            case 4:
                return STATUS_EXCEPTION;
            case 5:
                return STATUS_LOGIN_FAILED;
            case 6:
                return STATUS_MFA;
            case 7:
                return STATUS_UPDATE_REQUIRED;
            }
            
            
            return null;
        }
        
        public int index() {
            return index;
        }
        
    };
    
    
  public enum PropertyTypes {
	  	
	  	REAL_ESTATE(0),
	  	VEHICLE(1),
        ART (2),
        JEWELRY (3),
        FURNITURE (4),
        APPLIANCES (5),
        COMPUTER (6),        
        ELECTRONICS (7),
        SPORTS_EQUIPMENT (8),
        MISCELLANEOUS (9);
                     
        private int index;
        
        PropertyTypes(int index) {
            this.index = index;
        }
        
        public static PropertyTypes fromInteger(Integer value) {
            
            switch(value) {
            case 0:
                return REAL_ESTATE;
            case 1:
                return VEHICLE;
            case 2:
                return ART;
            case 3:
                return JEWELRY;
            case 4:
                return FURNITURE;
            case 5:
                return APPLIANCES;
            case 6:
                return COMPUTER;
            case 7:
                return ELECTRONICS;
            case 8:
                return SPORTS_EQUIPMENT;
            case 9:
                return MISCELLANEOUS;
            }
            
            return null;
        }
        
        public int index() {
            return index;
        }
        
        public static int size() {
			return 10;
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
        SPENDING(4),
        POPUP_TAGS(10),
        POPUP_CATEGORIES(11),
        LOCK_SCREEN(12),
        TRANSACTIONS_PAGE(13),
        ACCOUNT_SUMMARY(14),
        SPENDING_SUMMARY(15),
        BUDGET_SUMMARY(16),
        TRANSACTION_SUMMARY(17),
        ACCOUNT_SETTINGS(18),
        FEEDBACK(19),
        ADD_BANK(20),
        FIX_BANK(21),
        SHOW_HIDE_DATA(22),
        UPDATE_USERNAME_PASSWORD(23),
        MANUAL_BANK_LIST(24);
        
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
            case 4:
            	return SPENDING;
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
            case 18:
                return ACCOUNT_SETTINGS;
            case 19:
                return FEEDBACK;
            case 20:
            	return ADD_BANK;
	        case 21:
	        	return FIX_BANK;
	        case 22:
	        	return SHOW_HIDE_DATA;
	        case 23:
	        	return UPDATE_USERNAME_PASSWORD;
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
		
	    ACCOUNT_EXCLUSION_FLAGS_TRANSACTION_LIST        (1 << 0), //1
	    ACCOUNT_EXCLUSION_FLAGS_REPORTS               	(1 << 1), //2
	    ACCOUNT_EXCLUSION_FLAGS_ACCOUNT_LIST            (1 << 2), //4
	    ACCOUNT_EXCLUSION_FLAGS_BUDGETS                	(1 << 3), //8
	    ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_INCOME	(1 << 4), //16
	    ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_EXPENSES	(1 << 5), //32
	    ACCOUNT_EXCLUSION_FLAGS_DEBT					(1 << 6), //64
	    ACCOUNT_EXCLUSION_FLAGS_ALL						(1 << 0
											    		|1 << 1
											    		|1 << 2
											    		|1 << 3
											    		|1 << 4
											    		|1 << 5
											    		|1 << 6); //127
	    	    
	    private final int index;
	    
	    AccountExclusionFlags(int index) {
	    	this.index = index;
	    }
	    
	    public int index() {
	    	return index;
	    }
	    public static int size() {
	    	return 7;
	    }
	};
	
	public enum SlideFrom {
			RIGHT,
			LEFT,
			TOP,
			BOTTOM
	}
	
	public enum AccountTypesEnum {
	    CASH("CASH"),
	    CHECKING("CHECKING"),
	    CREDIT_CARD("CREDIT CARD"),
	    INVESTMENTS("INVESTMENTS"),
	    LINE_OF_CREDIT("LINE OF CREDIT"),
	    LOANS("LOANS"),
	    MORTGAGE("MORTGAGE"),
	    PROPERTY("PROPERTY"),
	    SAVINGS("SAVINGS");

	    
	    private AccountTypesEnum(final String text) {
	        this.text = text;
	    }

	    private final String text;

	    @Override
	    public String toString() {
	        return text;
	    }
	    
	    public static AccountTypesEnum fromString(String value) {
	    	AccountTypesEnum typesEnum = null;
			if (value.equals("CASH")) {
				typesEnum = CASH;
			} else if (value.equals("CHECKING")) {
				typesEnum = CHECKING;
			} else if (value.equals("CREDIT CARD")) {
				typesEnum = CREDIT_CARD;
			} else if (value.equals("INVESTMENTS")) {
				typesEnum = INVESTMENTS;
			} else if (value.equals("LINE OF CREDIT")) {
				typesEnum = LINE_OF_CREDIT;
			} else if (value.equals("LOANS")) {
				typesEnum = LOANS;
			} else if (value.equals("MORTGAGE")) {
				typesEnum = MORTGAGE;
			} else if (value.equals("PROPERTY")) {
				typesEnum = PROPERTY;
			} else if (value.equals("SAVINGS")) {
				typesEnum = SAVINGS;
			}
			return typesEnum;
	    }
	}
	
	
	public enum PropertyTypesEnum {
	    REAL_ESTATE("REAL ESTATE"),
	    VEHICLE("VEHICLE"),
	    ART("ART"),
	    JEWELRY("JEWELRY"),
	    FURNITURE("FURNITURE"),
	    APPLIANCES("APPLIANCES"),
	    COMPUTER("COMPUTER"),
	    ELECTRONICS("ELECTRONICS"),
	    SPORTS_EQUIPMENT("SPORTS EQUIPMENT"),
	    MISCELLANEOUS("MISCELLANEOUS");

	    
	    private PropertyTypesEnum(final String text) {
	        this.text = text;
	    }

	    private final String text;

	    @Override
	    public String toString() {
	        return text;
	    }
	    
	    public static PropertyTypesEnum fromString(String value) {
	    	PropertyTypesEnum typesEnum = null;
			if (value.equals("REAL ESTATE")) {
				typesEnum = REAL_ESTATE;
			} else if (value.equals("VEHICLE")) {
				typesEnum = VEHICLE;
			} else if (value.equals("ART")) {
				typesEnum = ART;
			} else if (value.equals("JEWELRY")) {
				typesEnum = JEWELRY;
			} else if (value.equals("FURNITURE")) {
				typesEnum = FURNITURE;
			} else if (value.equals("APPLIANCES")) {
				typesEnum = APPLIANCES;
			} else if (value.equals("COMPUTER")) {
				typesEnum = COMPUTER;
			} else if (value.equals("ELECTRONICS")) {
				typesEnum = ELECTRONICS;
			} else if (value.equals("SPORTS EQUIPMENT")) {
				typesEnum = SPORTS_EQUIPMENT;
			} else if (value.equals("MISCELLANEOUS")) {
				typesEnum = MISCELLANEOUS;
			}
			return typesEnum;
	    }
	}
}
