package main_package;

public interface MasterToPrimaryInterface {
	public void commitTansaction(long txnID);
	public void abortTransaction(long txnID);
}