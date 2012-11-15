package com.moneydesktop.finance.database;

public class DatabaseObjectFactory {

	public static BusinessObject createInstance(Class<?> key, String id) {
		
		Long guid = Long.valueOf(id.hashCode());
		
		if (key.equals(AccountType.class))
			return new AccountType(guid);
		
		if (key.equals(AccountTypeGroup.class))
			return new AccountTypeGroup(guid);

		if (key.equals(Bank.class))
			return new Bank(guid);
		
		if (key.equals(BankAccount.class))
			return new BankAccount(guid);
		
		if (key.equals(BankAccountBalance.class))
			return new BankAccountBalance(guid);
		
		if (key.equals(BudgetItem.class))
			return new BudgetItem(guid);
		
		if (key.equals(BusinessObjectBase.class))
			return new BusinessObjectBase(guid);
		
		if (key.equals(Category.class))
			return new Category(guid);
		
		if (key.equals(CategoryType.class))
			return new CategoryType(guid);
		
		if (key.equals(Institution.class))
			return new Institution(guid);
		
		if (key.equals(Location.class))
			return new Location(guid);
		
		if (key.equals(Tag.class))
			return new Tag(guid);
		
		if (key.equals(TagInstance.class))
			return new TagInstance(guid);
		
		if (key.equals(Transactions.class))
			return new Transactions(guid);
		return null;
	}
}
