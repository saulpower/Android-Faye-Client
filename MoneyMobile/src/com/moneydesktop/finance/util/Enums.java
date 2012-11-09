package com.moneydesktop.finance.util;

public class Enums {

	public enum TxType {
		INSERT, UPDATE, DELETE
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
