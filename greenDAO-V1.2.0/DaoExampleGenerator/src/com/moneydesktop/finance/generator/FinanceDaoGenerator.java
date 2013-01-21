/*
 * Copyright (C) 2011 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.moneydesktop.finance.generator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

/**
 * Generates entities and DAOs for the project MoneyMobile.
 * 
 * Run it as a Java application (not Android).
 * 
 * @author Saul Howard
 */
public class FinanceDaoGenerator {

	private static Schema schema;
	
	// Entities
	private static Entity accountType;
	private static Entity accountTypeGroup;
	private static Entity bank;
	private static Entity bankAccountBalance;
	private static Entity bankAccount;
	private static Entity budgetItem;
	private static Entity businessObject;
	private static Entity category;
	private static Entity categoryType;
	private static Entity institution;
	private static Entity location;
	private static Entity tag;
	private static Entity tagInstance;
	private static Entity transactions;
	
    public static void main(String[] args) throws Exception {
    	
        schema = new Schema(1, "com.moneydesktop.finance.database");
        schema.enableKeepSectionsByDefault();
        
        addBusinessObject();
        addTag();
        addTagInstance();
        addBankAccountBalance();
        addInstitution();
        addBank();
        addBankAccount();
        addAccountType();
        addAccountTypeGroup();
        addCategoryType();
        addCategory();
        addTransaction();
        addBudgetItem();
        addLocation();
        
        new DaoGenerator().generateAll(schema, "/Users/saulhoward/Developer/Android/Chocopologie/chocopologie/MoneyMobile/src");
    }
    
    private static void addBusinessObjectBase(Entity entity) {

    	Property businessObjectId = entity.addLongProperty("businessObjectId").notNull().getProperty();
    	entity.addToOne(businessObject, businessObjectId);
    	
    	entity.setSuperclass("BusinessObject");
    }
    
    private static void addBusinessObject() {
    	
    	businessObject = schema.addEntity("BusinessObjectBase");
    	businessObject.addIdProperty();
    	businessObject.addIntProperty("dataState");
    	businessObject.addDateProperty("dateModified");
    	businessObject.addStringProperty("errorCode");
    	businessObject.addStringProperty("externalId");
    	businessObject.addIntProperty("flags");
    	businessObject.addStringProperty("primaryKey");
    	businessObject.addStringProperty("toString");
    	businessObject.addIntProperty("version");
    	
    	businessObject.setSuperclass("BusinessObject");
    }
    
    // Dependency on BusinessObject and Tag
    private static void addTagInstance() {
    	
    	tagInstance = schema.addEntity("TagInstance");
    	tagInstance.addIdProperty();
    	Property tagInstance_tagId = tagInstance.addLongProperty("tagId").notNull().getProperty();
    	Property tagInstance_businessObjectId = tagInstance.addLongProperty("baseObjectId").notNull().getProperty();
    	
    	businessObject.addToMany(tagInstance, tagInstance_businessObjectId).setName("tagInstances");
    	
    	tag.addToMany(tagInstance, tagInstance_tagId).setName("tagInstances");
    	
    	tagInstance.addToOne(tag, tagInstance_tagId).setName("tag");
    	tagInstance.addToOne(businessObject, tagInstance_businessObjectId).setName("businessObject");
    	
    	addBusinessObjectBase(tagInstance);
    }
    
    private static void addTag() {
    	
    	tag = schema.addEntity("Tag");
    	tag.addIdProperty();
    	tag.addStringProperty("tagId");
    	tag.addStringProperty("tagName");
    	
    	addBusinessObjectBase(tag);
    }
    
    private static void addBankAccountBalance() {
    	
    	bankAccountBalance = schema.addEntity("BankAccountBalance");
    	bankAccountBalance.addIdProperty();
    	bankAccountBalance.addDoubleProperty("balance");
    	bankAccountBalance.addDateProperty("date");
    	
    	addBusinessObjectBase(bankAccountBalance);
    }
    
    // Dependency on BankAccountBalance and Bank
    private static void addBankAccount() {
    	
    	bankAccount = schema.addEntity("BankAccount");
    	bankAccount.addIdProperty();
    	bankAccount.addStringProperty("accountId");
    	bankAccount.addStringProperty("accountName");
    	bankAccount.addStringProperty("accountNumber");
    	bankAccount.addDoubleProperty("balance");
    	bankAccount.addStringProperty("bankName");
    	bankAccount.addDoubleProperty("beginningBalance");
    	bankAccount.addDoubleProperty("creditLimit");
    	bankAccount.addStringProperty("defaultClassId");
    	bankAccount.addIntProperty("dueDay");
    	bankAccount.addIntProperty("exclusionFlags");
    	bankAccount.addStringProperty("institutionId");
    	bankAccount.addDoubleProperty("interestRate");
    	bankAccount.addBooleanProperty("isExcluded");
    	bankAccount.addBooleanProperty("isHolding");
    	bankAccount.addBooleanProperty("isLinked");
    	bankAccount.addDoubleProperty("localBalance");
    	bankAccount.addDoubleProperty("minimumPayment");
    	bankAccount.addIntProperty("mortgageTotal");
    	bankAccount.addStringProperty("notes");
    	bankAccount.addStringProperty("originalName");
    	bankAccount.addIntProperty("propertyType");
    	bankAccount.addIntProperty("transactionCount");

    	// BankAccount to BankAccountBalance Relationship
    	Property bankAccountBalance_bankAccountId = bankAccountBalance.addLongProperty("bankAccountId").getProperty();
    	bankAccount.addToMany(bankAccountBalance, bankAccountBalance_bankAccountId).setName("bankAccountBalances");
    	bankAccountBalance.addToOne(bankAccount, bankAccountBalance_bankAccountId).setName("bankAccount");
    	
    	// BankAccount to Bank Relationship
    	Property bankAccount_BankId = bankAccount.addLongProperty("bankAccountId").getProperty();
    	bank.addToMany(bankAccount, bankAccount_BankId).setName("bankAccounts");
    	bankAccount.addToOne(bank, bankAccount_BankId).setName("bank");
    	
    	addBusinessObjectBase(bankAccount);
    }
    
    private static void addAccountType() {
    	
    	accountType = schema.addEntity("AccountType");
    	accountType.addIdProperty();
    	accountType.addStringProperty("accountTypeId");
    	accountType.addStringProperty("accountTypeName");
    	accountType.addIntProperty("aggregationType");
    	accountType.addIntProperty("financialAccountType");
    	accountType.addStringProperty("groupKey");
    	
    	addBusinessObjectBase(accountType);
    }
    
    // Dependency on  AccountType and BankAccount
    private static void addAccountTypeGroup() {
    	
    	accountTypeGroup = schema.addEntity("AccountTypeGroup");
    	accountTypeGroup.addIdProperty();
    	accountTypeGroup.addStringProperty("groupId");
    	accountTypeGroup.addStringProperty("groupName");
    	accountTypeGroup.addStringProperty("imageName");
    	accountTypeGroup.addIntProperty("sortOrder");
    	
    	// AccountType to AccountTypeGroup Relationship
    	Property accountType_AccountTypeGroupId = accountType.addLongProperty("accountTypeGroupId").getProperty();
    	accountTypeGroup.addToMany(accountType, accountType_AccountTypeGroupId).setName("accountTypes");
    	accountType.addToOne(accountTypeGroup, accountType_AccountTypeGroupId).setName("accountTypeGroup");
    	
    	// AccountType to BankAccount Relationship
    	Property bankAccount_accountTypeId = bankAccount.addLongProperty("accountTypeId").getProperty();
    	Property bankAccount_subAccountTypeId = bankAccount.addLongProperty("subAccountTypeId").getProperty();
    	accountType.addToMany(bankAccount, bankAccount_accountTypeId).setName("bankAccounts");
    	bankAccount.addToOne(accountType, bankAccount_accountTypeId).setName("accountType");
    	bankAccount.addToOne(accountType, bankAccount_subAccountTypeId).setName("subAccountType");
    	
    	// AccountType Recursive Relationship
    	Property accountType_parentAccountTypeId = accountType.addLongProperty("parentAccountTypeId").getProperty();
    	accountType.addToOne(accountType, accountType_parentAccountTypeId).setName("parent");
    	accountType.addToMany(accountType, accountType_parentAccountTypeId).setName("children");
    	
    	addBusinessObjectBase(accountTypeGroup);
    }

    // Dependency on BankAccount and Category
    private static void addTransaction() {
    	
    	transactions = schema.addEntity("Transactions");
    	transactions.addIdProperty();
    	transactions.addDoubleProperty("amount");
    	transactions.addDoubleProperty("amountReimbursable");
    	transactions.addDateProperty("date");
    	transactions.addDateProperty("datePosted");
    	transactions.addIntProperty("dayNumber");
    	transactions.addIntProperty("exclusionFlags");
    	transactions.addBooleanProperty("hasReceipt");
    	transactions.addBooleanProperty("hasSplit");
    	transactions.addBooleanProperty("isBusiness");
    	transactions.addBooleanProperty("isCleared");
    	transactions.addBooleanProperty("isExcluded");
    	transactions.addBooleanProperty("isFlagged");
    	transactions.addBooleanProperty("isManual");
    	transactions.addBooleanProperty("isMatched");
    	transactions.addBooleanProperty("isProcessed");
    	transactions.addBooleanProperty("isReimbursable");
    	transactions.addBooleanProperty("isReported");
    	transactions.addBooleanProperty("isReportedAndPaid");
    	transactions.addBooleanProperty("isReportedAndSubmitted");
    	transactions.addBooleanProperty("isSplit");
    	transactions.addBooleanProperty("isVoid");
    	transactions.addStringProperty("memo");
    	transactions.addIntProperty("monthNumber");
    	transactions.addDoubleProperty("normalizedAmount");
    	transactions.addStringProperty("originalCategory");
    	transactions.addStringProperty("originalTitle");
    	transactions.addIntProperty("quarterNumber");
    	transactions.addDoubleProperty("rawAmount");
    	transactions.addStringProperty("reference");
    	transactions.addStringProperty("tagString");
    	transactions.addStringProperty("title");
    	transactions.addStringProperty("transactionId");
    	transactions.addIntProperty("transactionType");
    	transactions.addIntProperty("weekNumber");
    	transactions.addIntProperty("yearNumber");
    	
    	// Transaction to BankAccount Relationship
    	Property transaction_bankAccountId = transactions.addLongProperty("bankAccountId").getProperty();
    	bankAccount.addToMany(transactions, transaction_bankAccountId).setName("transactions");
    	transactions.addToOne(bankAccount, transaction_bankAccountId).setName("bankAccount");
    	
    	// Transaction to Category Relationship
    	Property transaction_categoryId = transactions.addLongProperty("categoryId").getProperty();
    	category.addToMany(transactions, transaction_categoryId).setName("transactions");
    	transactions.addToOne(category, transaction_categoryId).setName("category");
    	
    	// Transaction Recursive Relationship
    	Property transaction_parentTransactionId = transactions.addLongProperty("parentTransactionId").getProperty();
    	transactions.addToOne(transactions, transaction_parentTransactionId).setName("parent");
    	transactions.addToMany(transactions, transaction_parentTransactionId).setName("children");
    	
    	addBusinessObjectBase(transactions);
    }
    
    private static void addCategoryType() {
    	
    	categoryType = schema.addEntity("CategoryType");
    	categoryType.addIdProperty();
    	categoryType.addStringProperty("categoryTypeId");
    	categoryType.addStringProperty("categoryTypeName");
    	
    	addBusinessObjectBase(categoryType);
    }
    
    // Dependency on CategoryType
    private static void addCategory() {
    	
    	category = schema.addEntity("Category");
    	category.addIdProperty();
    	category.addStringProperty("categoryId");
    	category.addStringProperty("categoryName");
    	category.addStringProperty("categoryNumber");
    	category.addStringProperty("imageName");
    	category.addBooleanProperty("isSystem");
    	category.addBooleanProperty("isTaxRelated");
    	category.addStringProperty("notes");
    	category.addIntProperty("sortOrder");
    	category.addStringProperty("taxReference");
    	
    	// Category to CategoryType Relationship
    	Property category_categoryTypeId = category.addLongProperty("categoryTypeId").getProperty();
    	categoryType.addToMany(category, category_categoryTypeId).setName("categories");
    	category.addToOne(categoryType, category_categoryTypeId).setName("categoryType");
    	
    	// Category Recursive Relationship
    	Property category_parentCategoryId = category.addLongProperty("parentCategoryId").getProperty();
    	category.addToOne(category, category_parentCategoryId).setName("parent");
    	category.addToMany(category, category_parentCategoryId).setName("children");

    	addBusinessObjectBase(category);
    }
    
    // Dependency on Category
    private static void addBudgetItem() {
    	
    	budgetItem = schema.addEntity("BudgetItem");
    	budgetItem.addIdProperty();
    	budgetItem.addDoubleProperty("amount");
    	budgetItem.addStringProperty("budgetItemId");
    	budgetItem.addBooleanProperty("isActive");
    	budgetItem.addBooleanProperty("isDefault");
    	
    	// BudgetItem to Category Relationship
    	Property budgetItem_categoryId = budgetItem.addLongProperty("categoryId").getProperty();
    	category.addToMany(budgetItem, budgetItem_categoryId).setName("budgetItems");
    	budgetItem.addToOne(category, budgetItem_categoryId).setName("category");
    	
    	addBusinessObjectBase(budgetItem);
    }

    // Dependency on Institution
    private static void addBank() {
    	
    	bank = schema.addEntity("Bank");
    	bank.addIdProperty();
    	bank.addStringProperty("bankId");
    	bank.addStringProperty("bankName");
    	bank.addDateProperty("dateCreated");
    	bank.addStringProperty("defaultClassId");
    	bank.addStringProperty("deleteAction");
    	bank.addBooleanProperty("isLinked");
    	bank.addDateProperty("lastRefreshDate");
    	bank.addStringProperty("logoId");
    	bank.addIntProperty("processStatus");
    	bank.addIntProperty("status");
    	bank.addStringProperty("statusDescription");
    	bank.addIntProperty("statusFlags");
    	bank.addStringProperty("statusInstructions");
    	
    	// Bank to Institution Relationship
    	Property bank_InstitutionId = bank.addLongProperty("institutionId").getProperty();
    	institution.addToMany(bank, bank_InstitutionId).setName("banks");
    	bank.addToOne(institution, bank_InstitutionId).setName("institution");

    	addBusinessObjectBase(bank);
    }
    
    private static void addInstitution() {
    	
    	institution = schema.addEntity("Institution");
    	institution.addIdProperty();
    	institution.addDateProperty("createdOn");
    	institution.addStringProperty("institutionId");
    	institution.addStringProperty("instructions");
    	institution.addStringProperty("name");
    	institution.addStringProperty("phone");
    	institution.addIntProperty("popularity");
    	institution.addIntProperty("status");
    	institution.addDateProperty("updatedOn");
    	institution.addStringProperty("url");
    	
    	addBusinessObjectBase(institution);
    }
    
    // Dependency on Bank
    private static void addLocation() {
    	
    	location = schema.addEntity("Location");
    	location.addIdProperty();
    	location.addStringProperty("city");
    	location.addBooleanProperty("isClient");
    	location.addDoubleProperty("latitude");
    	location.addStringProperty("locationId");
    	location.addStringProperty("locationType");
    	location.addDoubleProperty("longitude");
    	location.addStringProperty("name");
    	location.addStringProperty("postalCode");
    	location.addStringProperty("state");
    	location.addStringProperty("streetAddress");
    	
    	// Location to Bank Relationship
    	Property location_BankId = location.addLongProperty("bankId").getProperty();
    	bank.addToMany(location, location_BankId).setName("locations");
    	location.addToOne(bank, location_BankId).setName("bank");
    	
    	addBusinessObjectBase(location);
    }
}
