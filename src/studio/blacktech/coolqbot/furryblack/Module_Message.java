package studio.blacktech.coolqbot.furryblack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import com.sobte.cqp.jcq.entity.Group;
import com.sobte.cqp.jcq.event.JcqApp;

import studio.blacktech.coolqbot.furryblack.common.LoggerX;
import studio.blacktech.coolqbot.furryblack.common.message.Message;
import studio.blacktech.coolqbot.furryblack.common.module.Module;
import studio.blacktech.coolqbot.furryblack.common.module.ModuleExecutor;
import studio.blacktech.coolqbot.furryblack.common.module.ModuleListener;
import studio.blacktech.coolqbot.furryblack.common.module.ModuleTrigger;

public class Module_Message extends Module {

	private static final long serialVersionUID = 1L;

	// ==========================================================================================================================================================
	//
	// 模块基本配置
	//
	// ==========================================================================================================================================================

	private static String MODULE_PACKAGENAME = "core_message";
	private static String MODULE_COMMANDNAME = "message";
	private static String MODULE_DISPLAYNAME = "消息广播";
	private static String MODULE_DESCRIPTION = "负责发送所有消息";
	private static String MODULE_VERSION = "1.0";
	private static String[] MODULE_USAGE = new String[] {};
	private static String[] MODULE_PRIVACY_TRIGER = new String[] {};
	private static String[] MODULE_PRIVACY_LISTEN = new String[] {};
	private static String[] MODULE_PRIVACY_STORED = new String[] {};
	private static String[] MODULE_PRIVACY_CACHED = new String[] {};
	private static String[] MODULE_PRIVACY_OBTAIN = new String[] {};

	// ==========================================================================================================================================================
	//
	// 成员变量
	//
	// ==========================================================================================================================================================

	private File FILE_MESSAGE_HELP;
	private File FILE_MESSAGE_INFO;
	private File FILE_MESSAGE_EULA;

	private String MESSAGE_HELP = "";
	private String MESSAGE_INFO = "";
	private String MESSAGE_EULA = "";

	private String MESSAGE_LIST_USER = "";
	private String MESSAGE_LIST_DISZ = "";
	private String MESSAGE_LIST_GROP = "";

	private TreeMap<Long, LinkedList<Integer>> MESSAGE_HISTORY_GROP;
	private MessageDelegate delegate = new MessageDelegate();

	private long USERID_CQBOT = 0;
	private long USERID_ADMIN = 0;

	private boolean GEN_LOCK = false;

	// ==========================================================================================================================================================
	//
	// 生命周期函数
	//
	// ==========================================================================================================================================================

	public Module_Message() throws Exception {
		super(MODULE_PACKAGENAME, MODULE_COMMANDNAME, MODULE_DISPLAYNAME, MODULE_DESCRIPTION, MODULE_VERSION, MODULE_USAGE, MODULE_PRIVACY_TRIGER, MODULE_PRIVACY_LISTEN, MODULE_PRIVACY_STORED, MODULE_PRIVACY_CACHED, MODULE_PRIVACY_OBTAIN);
	}

	@Override
	public void init(LoggerX logger) throws Exception {

		this.initConfFolder();
		this.initCofigurtion();

		this.MESSAGE_HISTORY_GROP = new TreeMap<>();

		if (this.NEW_CONFIG) {
			logger.seek("[Message] 配置文件不存在 - 生成默认配置");
			this.CONFIG.setProperty("logger_level", "0");
			this.CONFIG.setProperty("userid_admin", "0");
			this.saveConfig();
		} else {
			this.loadConfig();
		}

		this.FILE_MESSAGE_HELP = Paths.get(this.FOLDER_CONF.getAbsolutePath(), "message_help.txt").toFile();
		this.FILE_MESSAGE_INFO = Paths.get(this.FOLDER_CONF.getAbsolutePath(), "message_info.txt").toFile();
		this.FILE_MESSAGE_EULA = Paths.get(this.FOLDER_CONF.getAbsolutePath(), "message_eula.txt").toFile();

		if (!this.FILE_MESSAGE_HELP.exists()) { this.FILE_MESSAGE_HELP.createNewFile(); }
		if (!this.FILE_MESSAGE_INFO.exists()) { this.FILE_MESSAGE_INFO.createNewFile(); }
		if (!this.FILE_MESSAGE_EULA.exists()) { this.FILE_MESSAGE_EULA.createNewFile(); }

		BufferedReader readerHelp = new BufferedReader(new InputStreamReader(new FileInputStream(this.FILE_MESSAGE_HELP), "UTF-8"));
		BufferedReader readerInfo = new BufferedReader(new InputStreamReader(new FileInputStream(this.FILE_MESSAGE_INFO), "UTF-8"));
		BufferedReader readerEula = new BufferedReader(new InputStreamReader(new FileInputStream(this.FILE_MESSAGE_EULA), "UTF-8"));

		String line;

		while ((line = readerHelp.readLine()) != null) {
			this.MESSAGE_HELP = this.MESSAGE_HELP + line + "\r\n";
		}

		while ((line = readerInfo.readLine()) != null) {
			this.MESSAGE_INFO = this.MESSAGE_INFO + line + "\r\n";
		}

		while ((line = readerEula.readLine()) != null) {
			this.MESSAGE_EULA = this.MESSAGE_EULA + line + "\r\n";
		}

		readerHelp.close();
		readerInfo.close();
		readerEula.close();

		this.MESSAGE_HELP = this.MESSAGE_HELP.replaceAll("REPLACE_VERSION", entry.VerID);
		this.MESSAGE_INFO = this.MESSAGE_INFO.replaceAll("REPLACE_VERSION", entry.VerID);
		this.MESSAGE_EULA = this.MESSAGE_EULA.replaceAll("REPLACE_VERSION", entry.VerID);

		this.USERID_CQBOT = JcqApp.CQ.getLoginQQ();
		this.USERID_ADMIN = Long.parseLong(this.CONFIG.getProperty("userid_admin", "0"));

		if (this.USERID_ADMIN == 0) { throw new Exception("管理员账号配置错误"); }

		logger.seek("[Message] 机器人账号", this.USERID_CQBOT);
		logger.seek("[Message] 管理员账号", this.USERID_ADMIN);

		List<Group> groups = JcqApp.CQ.getGroupList();
		for (Group group : groups) {
			this.MESSAGE_HISTORY_GROP.put(group.getId(), new LinkedList<>());
		}

	}

	@Override
	public void boot(LoggerX logger) throws Exception {

	}

	@Override
	public void shut(LoggerX logger) throws Exception {
	}

	@Override
	public void save(LoggerX logger) throws Exception {
	}

	@Override
	public void reload(LoggerX logger) throws Exception {
	}

	@Override
	public void groupMemberIncrease(int typeid, int sendtime, long gropid, long operid, long userid) throws Exception {
	}

	@Override
	public void groupMemberDecrease(int typeid, int sendtime, long gropid, long operid, long userid) throws Exception {
	}

	// ==========================================================================================================================================================
	//
	// 工具函数
	//
	// ==========================================================================================================================================================

	@Override
	public String[] generateReport(int mode, Message message, Object... parameters) {
		return null;
	}

	private void doAdminInfo(String message) {
		JcqApp.CQ.sendPrivateMsg(this.USERID_ADMIN, message);
	}

	private void doAdminInfo(String[] message) {
		for (String temp : message) {
			JcqApp.CQ.sendPrivateMsg(this.USERID_ADMIN, temp);
		}
	}

	private void doUserInfo(long userid, String message) {
		JcqApp.CQ.sendPrivateMsg(userid, message);
	}

	private void doUserInfo(long userid, String[] message) {
		for (String temp : message) {
			JcqApp.CQ.sendPrivateMsg(userid, temp);
		}
	}

	private void doDiszInfo(long diszid, String message) {
		JcqApp.CQ.sendDiscussMsg(diszid, message);
	}

	private void doDiszInfo(long diszid, String[] message) {
		for (String temp : message) {
			JcqApp.CQ.sendDiscussMsg(diszid, temp);
		}
	}

	private void doDiszInfo(long diszid, long userid, String message) {
		JcqApp.CQ.sendDiscussMsg(diszid, "[CQ:at,qq=" + userid + "] " + message);
	}

	private void doGropInfo(long gropid, String message) {
		this.MESSAGE_HISTORY_GROP.get(gropid).add(JcqApp.CQ.sendGroupMsg(gropid, message));
	}

	private void doGropInfo(long gropid, String[] message) {
		for (String temp : message) {
			this.MESSAGE_HISTORY_GROP.get(gropid).add(JcqApp.CQ.sendGroupMsg(gropid, temp));
		}
	}

	private void doGropInfo(long gropid, long userid, String message) {
		this.MESSAGE_HISTORY_GROP.get(gropid).add(JcqApp.CQ.sendGroupMsg(gropid, "[CQ:at,qq=" + userid + "] " + message));
	}

	private void doSendInfo(long userid) {
		JcqApp.CQ.sendPrivateMsg(userid, this.MESSAGE_INFO);
	}

	private void doSendEula(long userid) {
		JcqApp.CQ.sendPrivateMsg(userid, this.MESSAGE_EULA);
	}

	private void doSendHelp(long userid) {
		JcqApp.CQ.sendPrivateMsg(userid, this.MESSAGE_HELP);
	}

	private void doSendHelp(long userid, ModuleTrigger module) {
		JcqApp.CQ.sendPrivateMsg(userid, module.MODULE_FULLHELP());
	}

	private void doSendHelp(long userid, ModuleListener module) {
		JcqApp.CQ.sendPrivateMsg(userid, module.MODULE_FULLHELP());
	}

	private void doSendHelp(long userid, ModuleExecutor module) {
		JcqApp.CQ.sendPrivateMsg(userid, module.MODULE_FULLHELP());
	}

	private void doSendListUser(long userid) {
		JcqApp.CQ.sendPrivateMsg(userid, this.MESSAGE_LIST_USER);
	}

	private void doSendListDisz(long userid) {
		JcqApp.CQ.sendPrivateMsg(userid, this.MESSAGE_LIST_DISZ);
	}

	private void doSendListGrop(long userid) {
		JcqApp.CQ.sendPrivateMsg(userid, this.MESSAGE_LIST_GROP);
	}

	private boolean doIsMyself(long userid) {
		return this.USERID_CQBOT == userid;
	}

	private boolean doIsAdmin(long userid) {
		return this.USERID_ADMIN == userid;
	}

	private void doRevokeMessage(long gropid) {
		JcqApp.CQ.deleteMsg(this.MESSAGE_HISTORY_GROP.get(gropid).pollLast());
	}

	public void doGenetateList(
	// @formatter:off
		ArrayList<ModuleTrigger> TRIGGER_USER,
		ArrayList<ModuleTrigger> TRIGGER_DISZ,
		ArrayList<ModuleTrigger> TRIGGER_GROP,
		ArrayList<ModuleListener> LISTENER_USER,
		ArrayList<ModuleListener> LISTENER_DISZ,
		ArrayList<ModuleListener> LISTENER_GROP,
		TreeMap<String, ModuleExecutor> EXECUTOR_USER,
		TreeMap<String, ModuleExecutor> EXECUTOR_DISZ,
		TreeMap<String, ModuleExecutor> EXECUTOR_GROP
	// @formatter:on
	) {
		if (this.GEN_LOCK) { return; }

		StringBuilder preBuilder;

		// =========================================================
		// =========================================================
		// =========================================================

		preBuilder = new StringBuilder();

		preBuilder.append("=================");
		preBuilder.append("\r\n启用的触发器： ");

		if (TRIGGER_USER.size() == 0) {
			preBuilder.append("无");
		} else {
			preBuilder.append(TRIGGER_USER.size());
			for (ModuleTrigger temp : TRIGGER_USER) {
				preBuilder.append("\r\n");
				preBuilder.append(temp.MODULE_COMMANDNAME());
				preBuilder.append(" > ");
				preBuilder.append(temp.MODULE_DISPLAYNAME());
				preBuilder.append("：");
				preBuilder.append(temp.MODULE_DESCRIPTION());
			}
		}

		preBuilder.append("\r\n=================");
		preBuilder.append("\r\n启用的监听器： ");

		if (LISTENER_USER.size() == 0) {
			preBuilder.append("无");
		} else {
			preBuilder.append(LISTENER_USER.size());
			for (ModuleListener temp : LISTENER_USER) {
				preBuilder.append("\r\n");
				preBuilder.append(temp.MODULE_COMMANDNAME());
				preBuilder.append(" > ");
				preBuilder.append(temp.MODULE_DISPLAYNAME());
				preBuilder.append("：");
				preBuilder.append(temp.MODULE_DESCRIPTION());
			}
		}

		preBuilder.append("\r\n=================");
		preBuilder.append("\r\n可用的执行器： ");

		if (EXECUTOR_USER.size() == 0) {
			preBuilder.append("无");
		} else {
			preBuilder.append(EXECUTOR_USER.size());
			for (String temp : EXECUTOR_USER.keySet()) {
				ModuleExecutor module = EXECUTOR_USER.get(temp);
				module.genFullHelp();
				preBuilder.append("\r\n");
				preBuilder.append(module.MODULE_COMMANDNAME());
				preBuilder.append(" > ");
				preBuilder.append(module.MODULE_DISPLAYNAME());
				preBuilder.append("：");
				preBuilder.append(module.MODULE_DESCRIPTION());
			}
		}

		preBuilder.append("\r\n=================");

		this.MESSAGE_LIST_USER = preBuilder.toString();

		// =========================================================
		// =========================================================
		// =========================================================

		preBuilder = new StringBuilder();

		preBuilder.append("=================");
		preBuilder.append("\r\n启用的触发器： ");

		if (TRIGGER_DISZ.size() == 0) {
			preBuilder.append("无");
		} else {
			preBuilder.append(TRIGGER_DISZ.size());
			for (ModuleTrigger temp : TRIGGER_DISZ) {
				preBuilder.append("\r\n");
				preBuilder.append(temp.MODULE_COMMANDNAME());
				preBuilder.append(" > ");
				preBuilder.append(temp.MODULE_DISPLAYNAME());
				preBuilder.append("：");
				preBuilder.append(temp.MODULE_DESCRIPTION());
			}
		}

		preBuilder.append("\r\n=================");
		preBuilder.append("\r\n启用的监听器： ");

		if (LISTENER_DISZ.size() == 0) {
			preBuilder.append("无");
		} else {
			preBuilder.append(LISTENER_DISZ.size());
			for (ModuleListener temp : LISTENER_DISZ) {
				preBuilder.append("\r\n");
				preBuilder.append(temp.MODULE_COMMANDNAME());
				preBuilder.append(" > ");
				preBuilder.append(temp.MODULE_DISPLAYNAME());
				preBuilder.append("：");
				preBuilder.append(temp.MODULE_DESCRIPTION());
			}
		}

		preBuilder.append("\r\n=================");
		preBuilder.append("\r\n可用的执行器： ");

		if (EXECUTOR_DISZ.size() == 0) {
			preBuilder.append("无");
		} else {
			preBuilder.append(EXECUTOR_DISZ.size());
			for (String temp : EXECUTOR_DISZ.keySet()) {
				ModuleExecutor module = EXECUTOR_DISZ.get(temp);
				module.genFullHelp();
				preBuilder.append("\r\n");
				preBuilder.append(module.MODULE_COMMANDNAME());
				preBuilder.append(" > ");
				preBuilder.append(module.MODULE_DISPLAYNAME());
				preBuilder.append("：");
				preBuilder.append(module.MODULE_DESCRIPTION());
			}
		}

		preBuilder.append("\r\n=================");

		this.MESSAGE_LIST_USER = preBuilder.toString();

		// =========================================================
		// =========================================================
		// =========================================================

		preBuilder = new StringBuilder();

		preBuilder.append("=================");
		preBuilder.append("\r\n启用的触发器： ");

		if (TRIGGER_GROP.size() == 0) {
			preBuilder.append("无");
		} else {
			preBuilder.append(TRIGGER_GROP.size());
			for (ModuleTrigger temp : TRIGGER_GROP) {
				preBuilder.append("\r\n");
				preBuilder.append(temp.MODULE_COMMANDNAME());
				preBuilder.append(" > ");
				preBuilder.append(temp.MODULE_DISPLAYNAME());
				preBuilder.append("：");
				preBuilder.append(temp.MODULE_DESCRIPTION());
			}
		}

		preBuilder.append("\r\n=================");
		preBuilder.append("\r\n启用的监听器： ");

		if (LISTENER_GROP.size() == 0) {
			preBuilder.append("无");
		} else {
			preBuilder.append(LISTENER_GROP.size());
			for (ModuleListener temp : LISTENER_USER) {
				preBuilder.append("\r\n");
				preBuilder.append(temp.MODULE_COMMANDNAME());
				preBuilder.append(" > ");
				preBuilder.append(temp.MODULE_DISPLAYNAME());
				preBuilder.append("：");
				preBuilder.append(temp.MODULE_DESCRIPTION());
			}
		}

		preBuilder.append("\r\n=================");
		preBuilder.append("\r\n可用的执行器： ");

		if (EXECUTOR_GROP.size() == 0) {
			preBuilder.append("无");
		} else {
			preBuilder.append(EXECUTOR_GROP.size());
			for (String temp : EXECUTOR_GROP.keySet()) {
				ModuleExecutor module = EXECUTOR_GROP.get(temp);
				module.genFullHelp();
				preBuilder.append("\r\n");
				preBuilder.append(module.MODULE_COMMANDNAME());
				preBuilder.append(" > ");
				preBuilder.append(module.MODULE_DISPLAYNAME());
				preBuilder.append("：");
				preBuilder.append(module.MODULE_DESCRIPTION());
			}
		}

		preBuilder.append("\r\n=================");

		this.MESSAGE_LIST_USER = preBuilder.toString();

	}

	public MessageDelegate getDelegate() {
		return this.delegate;
	}

	public class MessageDelegate {

		public void adminInfo(String message) {
			Module_Message.this.doAdminInfo(message);
		}

		public void adminInfo(String[] message) {
			Module_Message.this.doAdminInfo(message);
		}

		public void userInfo(long userid, String message) {
			Module_Message.this.doUserInfo(userid, message);
		}

		public void userInfo(long userid, String[] message) {
			Module_Message.this.doUserInfo(userid, message);
		}

		public void diszInfo(long diszid, String message) {
			Module_Message.this.doDiszInfo(diszid, message);
		}

		public void diszInfo(long diszid, String[] message) {
			Module_Message.this.doDiszInfo(diszid, message);
		}

		public void diszInfo(long diszid, long userid, String message) {
			Module_Message.this.doDiszInfo(diszid, userid, message);
		}

		public void gropInfo(long gropid, String message) {
			Module_Message.this.doGropInfo(gropid, message);
		}

		public void gropInfo(long gropid, String[] message) {
			Module_Message.this.doGropInfo(gropid, message);
		}

		public void gropInfo(long gropid, long userid, String message) {
			Module_Message.this.doGropInfo(gropid, userid, message);
		}

		public void sendInfo(long userid) {
			Module_Message.this.doSendInfo(userid);
		}

		public void sendEula(long userid) {
			Module_Message.this.doSendEula(userid);
		}

		public void sendHelp(long userid) {
			Module_Message.this.doSendHelp(userid);
		}

		public void sendHelp(long userid, ModuleTrigger module) {
			Module_Message.this.doSendHelp(userid, module);
		}

		public void sendHelp(long userid, ModuleListener module) {
			Module_Message.this.doSendHelp(userid, module);
		}

		public void sendHelp(long userid, ModuleExecutor module) {
			Module_Message.this.doSendHelp(userid, module);
		}

		public void sendListUser(long userid) {
			Module_Message.this.doSendListUser(userid);
		}

		public void sendListDisz(long userid) {
			Module_Message.this.doSendListDisz(userid);
		}

		public void sendListGrop(long userid) {
			Module_Message.this.doSendListGrop(userid);
		}

		public boolean isMyself(long userid) {
			return Module_Message.this.doIsMyself(userid);
		}

		public boolean isAdmin(long userid) {
			return Module_Message.this.doIsAdmin(userid);
		}

		public void revokeMessage(long gropid) {
			Module_Message.this.doRevokeMessage(gropid);
		}

		public void genetateList(
		// 	@formatter:off
			ArrayList<ModuleTrigger> TRIGGER_USER,
			ArrayList<ModuleTrigger> TRIGGER_DISZ,
			ArrayList<ModuleTrigger> TRIGGER_GROP,
			ArrayList<ModuleListener> LISTENER_USER,
			ArrayList<ModuleListener> LISTENER_DISZ,
			ArrayList<ModuleListener> LISTENER_GROP,
			TreeMap<String, ModuleExecutor> EXECUTOR_USER,
			TreeMap<String, ModuleExecutor> EXECUTOR_DISZ,
			TreeMap<String, ModuleExecutor> EXECUTOR_GROP
			// @formatter:on
		) {
			Module_Message.this.doGenetateList(TRIGGER_USER, TRIGGER_DISZ, TRIGGER_GROP, LISTENER_USER, LISTENER_DISZ, LISTENER_GROP, EXECUTOR_USER, EXECUTOR_DISZ, EXECUTOR_GROP);
		}

	}
}
