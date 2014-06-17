package cs455.overlay.wireformats;

public interface Protocol {
	static public final int REGISTER           = 1;
	static public final int DEREGISTER         = 2;
	static public final int LINK_WEIGHTS       = 3;
	static public final int TASK_COMPLETE      = 4;
	static public final int TASK_INITIATE      = 5;
	static public final int TASK_SUMMARY_REQ   = 6;
	static public final int TASK_SUMMARY_RES   = 7;
	static public final int REGISTER_RES       = 8;
	static public final int MESSAGE_NODES_LIST = 9;
	static public final int DEREGISTER_RES    = 10;
	static public final int EXCHANGE_REQ      = 11;
	static public final int EXCHANGE_RES      = 12;
}
