package com.moneydesktop.finance.database;

import de.greenrobot.dao.Property;

public class QueryProperty {

    public static String EQUALS = "= ?";
    public static String NOT_EQUALS = "!= ?";
    public static String LIKE = "LIKE ?";
    
    private String mTablename;
    private Property mField;
    private Property mForeignKey;
    private String mComparator;
    private String mConnector;
    private boolean mGroup = false;
    
    public QueryProperty(String tablename, Property field, Property foreignKey) {
        mTablename = tablename;
        mField = field;
        mForeignKey = foreignKey;
    }
    
    public QueryProperty(String tablename, Property field, String comparator) {
        mTablename = tablename;
        mField = field;
        mComparator = comparator;
    }
    
    public QueryProperty(String tablename, Property field) {
        mTablename = tablename;
        mField = field;
    }
    
    public QueryProperty(String tablename, Property field, String comparisonMethod) {
        mTablename = tablename;
        mField = field;
        mComparator = comparisonMethod;
    }
    

    public String getTablename() {
        return mTablename;
    }

    public void setTablename(String mTablename) {
        this.mTablename = mTablename;
    }

    public Property getField() {
        return mField;
    }

    public void setField(Property mField) {
        this.mField = mField;
    }
    
    public String getColumnName() {
        return mField.columnName;
    }
    
    public void setForeignKey(Property foreignKey) {
        mForeignKey = foreignKey;
    }
    
    public String getForeignKey() {
        return mForeignKey.columnName;
    }

    public String getComparator() {
        return mComparator;
    }

    public void setComparator(String mComparator) {
        this.mComparator = mComparator;
    }

    public String getConnector(int position) {
        
        if (position == 0) return "";
        
        if (mConnector == null || mConnector.equals("")) {
            mConnector = " AND";
        }
        
        return mConnector;
    }

    public void setConnector(String mConnector) {
        this.mConnector = mConnector;
    }

    public boolean isGroup() {
        return mGroup;
    }

    public void setGroup(boolean mGroup) {
        this.mGroup = mGroup;
    }
    
    @Override
    public boolean equals(Object object) {
        
        if (object != null && object instanceof QueryProperty) {
            QueryProperty prop = (QueryProperty) object;
            
            if (prop.getTablename() != null && prop.getColumnName() != null
                    && getTablename() != null && getColumnName() != null) {
                
                return prop.getTablename().equals(getTablename()) && prop.getColumnName().equals(getColumnName());
            }
        }
        
        return false;
    }
}
