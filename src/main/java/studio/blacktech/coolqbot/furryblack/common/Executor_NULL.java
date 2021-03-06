package studio.blacktech.coolqbot.furryblack.common;


import studio.blacktech.coolqbot.furryblack.common.message.Message;
import studio.blacktech.coolqbot.furryblack.common.message.MessageDisz;
import studio.blacktech.coolqbot.furryblack.common.message.MessageGrop;
import studio.blacktech.coolqbot.furryblack.common.message.MessageUser;
import studio.blacktech.coolqbot.furryblack.common.module.ModuleExecutor;


public class Executor_NULL extends ModuleExecutor {


	private static final long serialVersionUID = 1L;


	// ==========================================================================================================================================================
	//
	// 模块基本配置
	//
	// ==========================================================================================================================================================


	private static final String MODULE_PACKAGENAME = "Executor_NULL";
	private static final String MODULE_COMMANDNAME = "null";
	private static final String MODULE_DISPLAYNAME = "模板模块";
	private static final String MODULE_DESCRIPTION = "模板模块";
	private static final String MODULE_VERSION = "1.0";
	private static final String[] MODULE_USAGE = new String[] {};
	public static String[] MODULE_PRIVACY_STORED = new String[] {};
	public static String[] MODULE_PRIVACY_CACHED = new String[] {};
	public static String[] MODULE_PRIVACY_OBTAIN = new String[] {};


	// ==========================================================================================================================================================
	//
	// 成员变量
	//
	// ==========================================================================================================================================================


	// ==========================================================================================================================================================
	//
	// 生命周期函数
	//
	// ==========================================================================================================================================================


	public Executor_NULL() throws Exception {
		super(MODULE_PACKAGENAME, MODULE_COMMANDNAME, MODULE_DISPLAYNAME, MODULE_DESCRIPTION, MODULE_VERSION, MODULE_USAGE, MODULE_PRIVACY_STORED, MODULE_PRIVACY_CACHED, MODULE_PRIVACY_OBTAIN);
	}


	@Override
	public boolean init() throws Exception {

		if (NEW_CONFIG) {
			CONFIG.setProperty("", "");
			saveConfig();
		} else {
			loadConfig();
		}
		ENABLE_USER = false;
		ENABLE_DISZ = false;
		ENABLE_GROP = false;
		return false;

	}

	@Override
	public boolean boot() throws Exception {
		return false;
	}

	@Override
	public boolean save() throws Exception {
		return false;
	}

	@Override
	public boolean shut() throws Exception {
		return false;
	}

	@Override
	public String[] exec(Message message) throws Exception {
		return new String[] {
				"此模块无可用命令"
		};
	}

	@Override
	public void groupMemberIncrease(int typeid, int sendtime, long gropid, long operid, long userid) {

	}

	@Override
	public void groupMemberDecrease(int typeid, int sendtime, long gropid, long operid, long userid) {
	}

	@Override
	public boolean doUserMessage(MessageUser message) throws Exception {
		return true;
	}

	@Override
	public boolean doDiszMessage(MessageDisz message) throws Exception {
		return true;
	}

	@Override
	public boolean doGropMessage(MessageGrop message) throws Exception {
		return true;
	}

	// ==========================================================================================================================================================
	//
	// 工具函数
	//
	// ==========================================================================================================================================================

	@Override
	public String[] generateReport(Message message) {
		return null;
	}
}
