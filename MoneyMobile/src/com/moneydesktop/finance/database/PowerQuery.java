package com.moneydesktop.finance.database;

import de.greenrobot.dao.AbstractDao;

import java.util.*;

public class PowerQuery {

    public final String TAG = this.getClass().getSimpleName();
    
	private AbstractDao<?, Long> mDao;
	
    private int mCount = 0;
    private Map<String, String> mTableMap = new HashMap<String, String>();
    
    private List<String> mSelectionArgs = new ArrayList<String>();
    private String[] mArgs = new String[2];

    private List<QueryProperty> mJoins = new ArrayList<QueryProperty>();
    private List<QueryProperty> mWhereQueries = new ArrayList<QueryProperty>();
    private List<PowerQuery> mQueries = new ArrayList<PowerQuery>();
    private String mConnector = "WHERE ";
    private List<QueryProperty> mOrders = new ArrayList<QueryProperty>();
    private String mOffset;
    private String mLimit;
    
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
    	mDao = dao;
        mTableMap.put(dao.getTablename(), "T");
    }

    public void addTables(Map<String, String> tables) {
        mTableMap.putAll(tables);
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
        
        mJoins.add(foreignKey);
        
        return this;
    }
    
    public PowerQuery where(QueryProperty field, String value) {

    	if (field.getComparator() == null) {
	        field.setComparator("= ?");
    	}
	        
        return addWhere(field, value);
    }
    
    public PowerQuery whereLike(QueryProperty field, String value) {

        if (field.getComparator() == null) {
            field.setComparator("LIKE ?");
        }
        
        return addWhere(field, value);
    }

    private PowerQuery addWhere(QueryProperty field, String value) {

        field.setSelectionArg(value);

        return where(field);
    }
    
    public PowerQuery between(QueryProperty field, String value1, String value2) {
        
    	field.setSelectionArg(value1, value2);
        
        field.setComparator("BETWEEN ? AND ?");
        
        return where(field);
    }
    
    public PowerQuery where(PowerQuery where) {
        
        if (where == null) {
            return this;
        }

        mQueries.add(where);
        
        return this;
    }
    
    public PowerQuery where(QueryProperty field) {

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

        field.setDescending(isDescending);
        mOrders.add(field);

        return this;
    }
    
    public PowerQuery orderBy(QueryProperty field) {

        mOrders.add(field);
        
        return this;
    }
    
    public PowerQuery limit(int limit) {
        
    	mArgs[0] = Integer.toString(limit);
        mLimit = " LIMIT ?";
        
        return this;
    }
    
    public PowerQuery offset(int offset) {

    	mArgs[1] = Integer.toString(offset);
        mOffset = " OFFSET ?";
        
        return this;
    }
    
    public String toString() {
        return buildQuery();
    }

    public void addQueryString(StringBuilder builder) {

        mSelectionArgs.clear();

        builder.append(mGroup ? " (" : "");

        for (int i = 0; i < mWhereQueries.size(); i++) {

            QueryProperty field = mWhereQueries.get(i);

            builder.append(field.getConnector(i) + " " + getTableRef(field.getTablename()) + "." + field.getColumnName() + " " + field.getComparator());

            mSelectionArgs.addAll(field.getSelectionArgs());
        }

        builder.append(mGroup ? ")" : "");
    }
    
    private String buildQuery() {

        StringBuilder builder = new StringBuilder();

        // Process all of the JOIN statements
        for (QueryProperty prop : mJoins) {
            String tableRef = addTable(prop.getTablename());
            builder.append(" LEFT JOIN " + prop.getTablename() + " " + tableRef + " ON T." + prop.getColumnName() + " = " + tableRef + "." + prop.getForeignKey());
        }

        // Add the where statement
        builder.append(" WHERE");
        
        addQueryString(builder);

        // Add any grouped WHERE statements
        for (PowerQuery powerQuery : mQueries) {
            builder.append(" AND");
            powerQuery.addTables(mTableMap);
            powerQuery.addQueryString(builder);

            mSelectionArgs.addAll(powerQuery.getSelectionArgsList());
        }
        
        builder.append(" GROUP BY T._ID");
        
        if (mOrders.size() > 0) {

            builder.append(" ORDER BY ");

            for (QueryProperty queryProperty : mOrders) {
                builder.append(getTableRef(queryProperty.getTablename()) + "." + queryProperty.getColumnName() + (queryProperty.isDescending() ? " DESC" : "") + ", ");
            }

            builder.delete(builder.length() - 2, builder.length());
        }
        
        if (mLimit != null) {
            builder.append(mLimit);
            mSelectionArgs.add(mArgs[0]);
        }
        if (mOffset != null) {
            builder.append(mOffset);
            mSelectionArgs.add(mArgs[1]);
        }
        
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
    
    public Object unique() {
    	
    	List<?> results = mDao.queryRaw(toString(), getSelectionArgs());
    	
    	if (results.size() == 1) return results.get(0);
    	
    	return null;
    }
    
    public List<?> list() {
    	return mDao.queryRaw(toString(), getSelectionArgs());
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
