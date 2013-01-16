package com.moneydesktop.finance.database;

import de.greenrobot.dao.AbstractDao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PowerQuery {

    private int mCount = 0;
    private Map<String, String> mTableMap = new HashMap<String, String>();
    
    private List<String> mSelectionArgs = new ArrayList<String>();
    
    private String mJoin = "";
    private List<QueryProperty> mWhereQueries = new ArrayList<QueryProperty>();
    private String mConnector = "WHERE ";
    private String mOrder = "";
    private String mOffset = "";
    private String mLimit = "";
    
    private boolean mWhereAdded = false;
    private boolean mConnectorCheck = false;
    private boolean mGroup = false;
    
    public PowerQuery(boolean group) {
        mGroup = group;
        mWhereAdded = true;
        mConnectorCheck = true;
        mConnector = "";
    }
    
    public PowerQuery(AbstractDao<?, Long> dao) {
        mTableMap.put(dao.getTablename(), "T");
    }
    
    private int getCount() {
        int count = mCount;
        mCount++;
        
        return count;
    }
    
    private String addTable(String tablename) {
        String tableRef = "T" + getCount();
        mTableMap.put(tablename.toUpperCase(), tableRef);
        
        return tableRef;
    }
    
    private String getTableRef(String tablename) {
        return mTableMap.get(tablename.toUpperCase());
    }
    
    public PowerQuery join(QueryProperty foreignKey) {
        
        String tableRef = addTable(foreignKey.getTablename());
        mJoin = " JOIN " + foreignKey.getTablename() + " " + tableRef + " ON T." + foreignKey.getColumnName() + " = " + tableRef + "._ID";
        
        return this;
    }
    
    public PowerQuery where(QueryProperty field, String value) {

        field.setComparator("= ?");
        
        return addWhere(field, value);
    }
    
    public PowerQuery whereLike(QueryProperty field, String value) {

        field.setComparator("LIKE ?");
        
        return addWhere(field, value);
    }
    
    public PowerQuery between(QueryProperty field, String value1, String value2) {
        
        mSelectionArgs.add(value1);
        mSelectionArgs.add(value2);
        
        field.setComparator("BETWEEN ? AND ?");
        
        return addWhere(field);
    }
    
    public PowerQuery where(PowerQuery where) {
        
        if (where == null) {
            return this;
        }
        
        whereCheck();
        
        mSelectionArgs.addAll(where.getSelectionArgsList());
        mWhereQueries.addAll(where.getQueryProperties());
        
        return this;
    }
    
    private PowerQuery addWhere(QueryProperty field, String value) {
        
        mSelectionArgs.add(value);
        
        return addWhere(field);
    }
    
    private PowerQuery addWhere(QueryProperty field) {

        String connector = whereCheck();
        
        field.setGroup(mGroup);
        field.setConnector(connector);
        mWhereQueries.add(field);
        
        return this;
    }
    
    public PowerQuery between(QueryProperty field, Date date1, Date date2) {
        return between(field, Long.toString(date1.getTime()), Long.toString(date2.getTime()));
    }
    
    private String whereCheck() {

        String connector = mConnector;
        
        if (!mWhereAdded) {
            mWhereAdded = true;
            return "";
        }
        
        if (!mConnectorCheck) {
            and();
        }
        
        mConnectorCheck = false;
        
        return connector;
    }
    
    public PowerQuery and() {
        
        mConnector = " AND";
        mConnectorCheck = true;
        
        return this;
    }
    
    public PowerQuery or() {
        
        mConnector = " OR";
        mConnectorCheck = true;
        
        return this;
    }
    
    public PowerQuery orderBy(QueryProperty field, boolean isDescending) {
        
        mOrder = " ORDER BY " + getTableRef(field.getTablename()) + "." + field.getColumnName() + (isDescending ? " DESC" : "");
        
        return this;
    }
    
    public PowerQuery limit(int limit) {
        
        mSelectionArgs.add(Integer.toString(limit));
        mLimit = " LIMIT ?";
        
        return this;
    }
    
    public PowerQuery offset(int offset) {
        
        mSelectionArgs.add(Integer.toString(offset));
        mOffset = " OFFSET ?";
        
        return this;
    }
    
    public String toString() {
        return buildQuery();
    }
    
    private String buildQuery() {
        
        StringBuilder builder = new StringBuilder();
        builder.append(mJoin);
        builder.append(" WHERE");
        
        boolean isGroup = false;
        boolean isEnd = false;
        
        for (int i = 0; i < mWhereQueries.size(); i++) {
            
            QueryProperty field = mWhereQueries.get(i);
            
            String grouper = "";
            
            if (!isGroup && field.isGroup()) {
                grouper = "(";
                isEnd = false;
            } else if (isGroup && !field.isGroup()) {
                grouper = ")";
                isEnd = true;
            }
            
            isGroup = field.isGroup();
            
            builder.append((isEnd ? grouper : "") + field.getConnector(i) + " " + (isEnd ? "" : grouper) + getTableRef(field.getTablename()) + "." + field.getColumnName() + " " + field.getComparator());
        }
        
        builder.append(mOrder);
        builder.append(mLimit);
        builder.append(mOffset);
        
        return builder.toString();
    }
    
    public String[] getSelectionArgs() {
        return mSelectionArgs.toArray(new String[0]);
    }
    
    public List<String> getSelectionArgsList() {
        return mSelectionArgs;
    }
    
    public List<QueryProperty> getQueryProperties() {
        return mWhereQueries;
    }
    
    public boolean hasQueryProperty(QueryProperty property) {
        
        for (QueryProperty field : mWhereQueries) {
            if (field.equals(property)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static PowerQuery where(boolean group, QueryProperty field, String value) {
        
        PowerQuery pq = new PowerQuery(group);
        
        pq.where(field, value);
        
        return pq;
    }
    
    public static PowerQuery whereLike(boolean group, QueryProperty field, String value) {
        
        PowerQuery pq = new PowerQuery(group);
        
        pq.whereLike(field, value);
        
        return pq;
    }
}
