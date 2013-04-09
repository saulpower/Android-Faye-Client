package main.java.com.moneydesktop.finance.database;

import de.greenrobot.dao.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryProperty {

    public static String EQUALS = "= ?";
    public static String NOT_EQUALS = "!= ?";
    public static String LIKE = "LIKE ?";
    public static String NOT_NULL = "NOT NULL";

    private String mTablename;
    private Property mField;
    private Property mForeignKey;
    private String mComparator;
    private String mConnector;
    private boolean mGroup = false;

    private boolean mDescending = false;
    private List<String> mArgs = new ArrayList<String>();

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

    public QueryProperty(String tablename, Property field, boolean isDescending) {
        mTablename = tablename;
        mField = field;
        mDescending = isDescending;
    }

    public boolean isDescending() {
        return mDescending;
    }

    public void setDescending(boolean mDescending) {
        this.mDescending = mDescending;
    }

    public void setSelectionArg(String... args) {
        mArgs.clear();
        Collections.addAll(mArgs, args);
    }

    public List<String> getSelectionArgs() {
        return mArgs;
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
