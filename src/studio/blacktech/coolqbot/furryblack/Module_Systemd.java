package studio.blacktech.coolqbot.furryblack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

import com.sobte.cqp.jcq.entity.IMsg;
import com.sobte.cqp.jcq.event.JcqApp;

import studio.blacktech.coolqbot.furryblack.common.LoggerX;
import studio.blacktech.coolqbot.furryblack.common.message.Message;
import studio.blacktech.coolqbot.furryblack.common.message.MessageDisz;
import studio.blacktech.coolqbot.furryblack.common.message.MessageGrop;
import studio.blacktech.coolqbot.furryblack.common.message.MessageUser;
import studio.blacktech.coolqbot.furryblack.common.module.Module;
import studio.blacktech.coolqbot.furryblack.common.module.ModuleExecutor;
import studio.blacktech.coolqbot.furryblack.common.module.ModuleListener;
import studio.blacktech.coolqbot.furryblack.common.module.ModuleTrigger;
import studio.blacktech.coolqbot.furryblack.modules.Executor_acon;
import studio.blacktech.coolqbot.furryblack.modules.Executor_admin;
import studio.blacktech.coolqbot.furryblack.modules.Executor_chou;
import studio.blacktech.coolqbot.furryblack.modules.Executor_dice;
import studio.blacktech.coolqbot.furryblack.modules.Executor_echo;
import studio.blacktech.coolqbot.furryblack.modules.Executor_jrjp;
import studio.blacktech.coolqbot.furryblack.modules.Executor_jrrp;
import studio.blacktech.coolqbot.furryblack.modules.Executor_kong;
import studio.blacktech.coolqbot.furryblack.modules.Executor_mine;
import studio.blacktech.coolqbot.furryblack.modules.Executor_roll;
import studio.blacktech.coolqbot.furryblack.modules.Executor_roulette;
import studio.blacktech.coolqbot.furryblack.modules.Executor_zhan;
import studio.blacktech.coolqbot.furryblack.modules.Listener_TopSpeak;
import studio.blacktech.coolqbot.furryblack.modules.Trigger_UserDeny;
import studio.blacktech.coolqbot.furryblack.modules.Trigger_WordDeny;

public class Module_Systemd extends Module {

	// ==========================================================================================================================================================
	//
	// ģ���������
	//
	// ==========================================================================================================================================================

	private static String MODULE_PACKAGENAME = "core_systemd";
	private static String MODULE_COMMANDNAME = "system";
	private static String MODULE_DISPLAYNAME = "����ģ��";
	private static String MODULE_DESCRIPTION = "�������й���ģ�鲢·��������Ϣ";
	private static String MODULE_VERSION = "22.0";
	private static String[] MODULE_USAGE = new String[] {};
	private static String[] MODULE_PRIVACY_TRIGER = new String[] {};
	private static String[] MODULE_PRIVACY_LISTEN = new String[] {};
	private static String[] MODULE_PRIVACY_STORED = new String[] {};
	private static String[] MODULE_PRIVACY_CACHED = new String[] {};
	private static String[] MODULE_PRIVACY_OBTAIN = new String[] {};

	// ==========================================================================================================================================================
	//
	// ��Ա����
	//
	// ==========================================================================================================================================================

	private SystemdDelegate delegate = new SystemdDelegate();

	private int COUNT_USER_MESSAGE = 0;
	private int COUNT_DISZ_MESSAGE = 0;
	private int COUNT_GROP_MESSAGE = 0;

	private boolean ENABLE_TRIGGER_USER = false;
	private boolean ENABLE_TRIGGER_DISZ = false;
	private boolean ENABLE_TRIGGER_GROP = false;
	private boolean ENABLE_LISENTER_USER = false;
	private boolean ENABLE_LISENTER_DISZ = false;
	private boolean ENABLE_LISENTER_GROP = false;
	private boolean ENABLE_EXECUTOR_USER = false;
	private boolean ENABLE_EXECUTOR_DISZ = false;
	private boolean ENABLE_EXECUTOR_GROP = false;

	private String CONFIG_TRIGGER_USER;
	private String CONFIG_TRIGGER_DISZ;
	private String CONFIG_TRIGGER_GROP;
	private String CONFIG_LISENTER_USER;
	private String CONFIG_LISENTER_DISZ;
	private String CONFIG_LISENTER_GROP;
	private String CONFIG_EXECUTOR_USER;
	private String CONFIG_EXECUTOR_DISZ;
	private String CONFIG_EXECUTOR_GROP;

	private String[] LIST_TRIGGER_USER = {};
	private String[] LIST_TRIGGER_DISZ = {};
	private String[] LIST_TRIGGER_GROP = {};
	private String[] LIST_LISENTER_USER = {};
	private String[] LIST_LISENTER_DISZ = {};
	private String[] LIST_LISENTER_GROP = {};
	private String[] LIST_EXECUTOR_USER = {};
	private String[] LIST_EXECUTOR_DISZ = {};
	private String[] LIST_EXECUTOR_GROP = {};

	private TreeMap<String, ModuleTrigger> TRIGGER_INSTANCE = new TreeMap<>();
	private ArrayList<ModuleTrigger> TRIGGER_USER = new ArrayList<>(100);
	private ArrayList<ModuleTrigger> TRIGGER_DISZ = new ArrayList<>(100);
	private ArrayList<ModuleTrigger> TRIGGER_GROP = new ArrayList<>(100);

	private TreeMap<String, ModuleListener> LISTENER_INSTANCE = new TreeMap<>();
	private ArrayList<ModuleListener> LISTENER_USER = new ArrayList<>(100);
	private ArrayList<ModuleListener> LISTENER_DISZ = new ArrayList<>(100);
	private ArrayList<ModuleListener> LISTENER_GROP = new ArrayList<>(100);

	private TreeMap<String, ModuleExecutor> EXECUTOR_INSTANCE = new TreeMap<>();
	private TreeMap<String, ModuleExecutor> EXECUTOR_USER = new TreeMap<>();
	private TreeMap<String, ModuleExecutor> EXECUTOR_DISZ = new TreeMap<>();
	private TreeMap<String, ModuleExecutor> EXECUTOR_GROP = new TreeMap<>();

	// ==========================================================================================================================================================
	//
	// �������ں���
	//
	// ==========================================================================================================================================================

	public Module_Systemd() throws Exception {
		super(MODULE_PACKAGENAME, MODULE_COMMANDNAME, MODULE_DISPLAYNAME, MODULE_DESCRIPTION, MODULE_VERSION, MODULE_USAGE, MODULE_PRIVACY_TRIGER, MODULE_PRIVACY_LISTEN, MODULE_PRIVACY_STORED, MODULE_PRIVACY_CACHED, MODULE_PRIVACY_OBTAIN);
	}

	@Override
	public void init(LoggerX logger) throws Exception {
		if (this.NEW_CONFIG) {
			logger.seek("[Systemd] �����ļ������� - ����Ĭ������");
			this.CONFIG.setProperty("trigger_user", "none");
			this.CONFIG.setProperty("trigger_disz", "none");
			this.CONFIG.setProperty("trigger_grop", "none");
			this.CONFIG.setProperty("listener_user", "none");
			this.CONFIG.setProperty("listener_disz", "none");
			this.CONFIG.setProperty("listener_grop", "none");
			this.CONFIG.setProperty("executor_user", "none");
			this.CONFIG.setProperty("executor_disz", "none");
			this.CONFIG.setProperty("executor_grop", "none");
			this.saveConfig();
		} else {
			this.loadConfig();
		}

		// =======================================================================================================================
		//
		//
		// =======================================================================================================================
		// ʵ����������

		this.instantiationTrigger(new Trigger_UserDeny());
		this.instantiationTrigger(new Trigger_WordDeny());

		// =======================================================================================================================
		// ʵ����������

		this.instantiationListener(new Listener_TopSpeak());

		// =======================================================================================================================
		// ʵ����ִ����

		this.instantiationExecutor(new Executor_admin());
		this.instantiationExecutor(new Executor_acon());
		this.instantiationExecutor(new Executor_chou());
		this.instantiationExecutor(new Executor_dice());
		this.instantiationExecutor(new Executor_echo());
		this.instantiationExecutor(new Executor_jrjp());
		this.instantiationExecutor(new Executor_jrrp());
		this.instantiationExecutor(new Executor_mine());
		this.instantiationExecutor(new Executor_kong());
		this.instantiationExecutor(new Executor_roll());
		this.instantiationExecutor(new Executor_roulette());
		this.instantiationExecutor(new Executor_zhan());

		// =======================================================================================================================
		//
		//
		// =======================================================================================================================
		// ��ʼ��������

		for (String name : this.TRIGGER_INSTANCE.keySet()) {
			logger.full("[Systemd] ��ʼ��������", name);
			this.TRIGGER_INSTANCE.get(name).init(logger);
		}

		// =======================================================================================================================
		// ��ʼ��������

		for (String name : this.LISTENER_INSTANCE.keySet()) {
			logger.full("[Systemd] ��ʼ��������", name);
			this.LISTENER_INSTANCE.get(name).init(logger);
		}

		// =======================================================================================================================
		// ��ʼ��ִ����

		for (String name : this.EXECUTOR_INSTANCE.keySet()) {
			logger.full("[Systemd] ��ʼ��ִ����", name);
			this.EXECUTOR_INSTANCE.get(name).init(logger);
		}

		// =======================================================================================================================
		//
		//
		// =======================================================================================================================
		// ��ȡ����������

		this.CONFIG_TRIGGER_USER = this.CONFIG.getProperty("trigger_user", "none");
		this.CONFIG_TRIGGER_DISZ = this.CONFIG.getProperty("trigger_disz", "none");
		this.CONFIG_TRIGGER_GROP = this.CONFIG.getProperty("trigger_grop", "none");

		logger.seek("[Systemd] ��������˽��" + this.CONFIG_TRIGGER_USER);
		logger.seek("[Systemd] ������������" + this.CONFIG_TRIGGER_DISZ);
		logger.seek("[Systemd] ��������Ⱥ��" + this.CONFIG_TRIGGER_GROP);

		this.LIST_TRIGGER_USER = this.CONFIG_TRIGGER_USER.equals("none") ? new String[0] : this.CONFIG_TRIGGER_USER.split(",");
		this.LIST_TRIGGER_DISZ = this.CONFIG_TRIGGER_DISZ.equals("none") ? new String[0] : this.CONFIG_TRIGGER_DISZ.split(",");
		this.LIST_TRIGGER_GROP = this.CONFIG_TRIGGER_GROP.equals("none") ? new String[0] : this.CONFIG_TRIGGER_GROP.split(",");

		// =======================================================================================================================
		// ��ȡ����������

		this.CONFIG_LISENTER_USER = this.CONFIG.getProperty("listener_user", "none");
		this.CONFIG_LISENTER_DISZ = this.CONFIG.getProperty("listener_disz", "none");
		this.CONFIG_LISENTER_GROP = this.CONFIG.getProperty("listener_grop", "none");

		logger.seek("[Systemd] ��������˽��" + this.CONFIG_LISENTER_USER);
		logger.seek("[Systemd] ������������" + this.CONFIG_LISENTER_DISZ);
		logger.seek("[Systemd] ��������Ⱥ��" + this.CONFIG_LISENTER_GROP);

		this.LIST_LISENTER_USER = this.CONFIG_LISENTER_USER.equals("none") ? new String[0] : this.CONFIG_LISENTER_USER.split(",");
		this.LIST_LISENTER_DISZ = this.CONFIG_LISENTER_DISZ.equals("none") ? new String[0] : this.CONFIG_LISENTER_DISZ.split(",");
		this.LIST_LISENTER_GROP = this.CONFIG_LISENTER_GROP.equals("none") ? new String[0] : this.CONFIG_LISENTER_GROP.split(",");

		// =======================================================================================================================
		// ��ȡִ��������

		this.CONFIG_EXECUTOR_USER = this.CONFIG.getProperty("executor_user", "none");
		this.CONFIG_EXECUTOR_DISZ = this.CONFIG.getProperty("executor_disz", "none");
		this.CONFIG_EXECUTOR_GROP = this.CONFIG.getProperty("executor_grop", "none");

		logger.seek("[Systemd] ִ������˽��" + this.CONFIG_EXECUTOR_USER);
		logger.seek("[Systemd] ִ����������" + this.CONFIG_EXECUTOR_DISZ);
		logger.seek("[Systemd] ִ������Ⱥ��" + this.CONFIG_EXECUTOR_GROP);

		this.LIST_EXECUTOR_USER = this.CONFIG_EXECUTOR_USER.equals("none") ? new String[0] : this.CONFIG_EXECUTOR_USER.split(",");
		this.LIST_EXECUTOR_DISZ = this.CONFIG_EXECUTOR_DISZ.equals("none") ? new String[0] : this.CONFIG_EXECUTOR_DISZ.split(",");
		this.LIST_EXECUTOR_GROP = this.CONFIG_EXECUTOR_GROP.equals("none") ? new String[0] : this.CONFIG_EXECUTOR_GROP.split(",");

		// =======================================================================================================================
		//
		//
		// =======================================================================================================================
		// ע�ᴥ����

		for (String name : this.LIST_TRIGGER_USER) {
			if (this.TRIGGER_INSTANCE.containsKey(name)) {
				ModuleTrigger instance = this.TRIGGER_INSTANCE.get(name);
				if (instance.ENABLE_USER) { this.TRIGGER_USER.add(instance); }
			} else {
				logger.mini("[Systemd] ���ô���", "˽�Ĵ�����������" + name);
			}
		}

		for (String name : this.LIST_TRIGGER_DISZ) {
			if (this.TRIGGER_INSTANCE.containsKey(name)) {
				ModuleTrigger instance = this.TRIGGER_INSTANCE.get(name);
				if (instance.ENABLE_DISZ) { this.TRIGGER_DISZ.add(instance); }
			} else {
				logger.mini("[Systemd] ���ô���", "���Ĵ�����������" + name);
			}
		}

		for (String name : this.LIST_TRIGGER_GROP) {
			if (this.TRIGGER_INSTANCE.containsKey(name)) {
				ModuleTrigger instance = this.TRIGGER_INSTANCE.get(name);
				if (instance.ENABLE_GROP) { this.TRIGGER_GROP.add(instance); }
			} else {
				logger.mini("[Systemd] ���ô���", "Ⱥ�Ĵ�����������" + name);
			}
		}

		this.ENABLE_TRIGGER_USER = this.TRIGGER_USER.size() > 0;
		this.ENABLE_TRIGGER_DISZ = this.TRIGGER_DISZ.size() > 0;
		this.ENABLE_TRIGGER_GROP = this.TRIGGER_GROP.size() > 0;

		// =======================================================================================================================
		// ע�������

		for (String name : this.LIST_LISENTER_USER) {
			if (this.LISTENER_INSTANCE.containsKey(name)) {
				ModuleListener instance = this.LISTENER_INSTANCE.get(name);
				if (instance.ENABLE_USER) { this.LISTENER_USER.add(instance); }
			} else {
				logger.mini("[Systemd] ���ô���", "˽�ļ�����������" + name);
			}
		}

		for (String name : this.LIST_LISENTER_DISZ) {
			if (this.LISTENER_INSTANCE.containsKey(name)) {
				ModuleListener instance = this.LISTENER_INSTANCE.get(name);
				if (instance.ENABLE_DISZ) { this.LISTENER_DISZ.add(instance); }
			} else {
				logger.mini("[Systemd] ���ô���", "���ļ�����������" + name);
			}
		}

		for (String name : this.LIST_LISENTER_GROP) {
			if (this.LISTENER_INSTANCE.containsKey(name)) {
				ModuleListener instance = this.LISTENER_INSTANCE.get(name);
				if (instance.ENABLE_GROP) { this.LISTENER_GROP.add(instance); }
			} else {
				logger.mini("[Systemd] ���ô���", "Ⱥ�ļ�����������" + name);
			}
		}

		this.ENABLE_LISENTER_USER = this.LISTENER_USER.size() > 0;
		this.ENABLE_LISENTER_DISZ = this.LISTENER_DISZ.size() > 0;
		this.ENABLE_LISENTER_GROP = this.LISTENER_GROP.size() > 0;

		// =======================================================================================================================
		// ע��ִ����

		for (String name : this.LIST_EXECUTOR_USER) {
			if (this.EXECUTOR_INSTANCE.containsKey(name)) {
				ModuleExecutor instance = this.EXECUTOR_INSTANCE.get(name);
				if (instance.ENABLE_USER) { this.EXECUTOR_USER.put(instance.MODULE_COMMANDNAME(), instance); }
			} else {
				logger.mini("[Systemd] ���ô���", "����ִ����������" + name);
			}
		}

		for (String name : this.LIST_EXECUTOR_DISZ) {
			if (this.EXECUTOR_INSTANCE.containsKey(name)) {
				ModuleExecutor instance = this.EXECUTOR_INSTANCE.get(name);
				if (instance.ENABLE_DISZ) { this.EXECUTOR_DISZ.put(instance.MODULE_COMMANDNAME(), instance); }
			} else {
				logger.mini("[Systemd] ���ô���", "����ִ����������" + name);
			}
		}

		for (String name : this.LIST_EXECUTOR_GROP) {
			if (this.EXECUTOR_INSTANCE.containsKey(name)) {
				ModuleExecutor instance = this.EXECUTOR_INSTANCE.get(name);
				if (instance.ENABLE_GROP) { this.EXECUTOR_GROP.put(instance.MODULE_COMMANDNAME(), instance); }
			} else {
				logger.mini("[Systemd] ���ô���", "����ִ����������" + name);
			}
		}

		this.ENABLE_EXECUTOR_USER = this.EXECUTOR_USER.size() > 0;
		this.ENABLE_EXECUTOR_DISZ = this.EXECUTOR_DISZ.size() > 0;
		this.ENABLE_EXECUTOR_GROP = this.EXECUTOR_GROP.size() > 0;

		// =======================================================================================================================

		logger.info("[Systemd] ������");
		logger.info("  ˽�ģ�", this.ENABLE_TRIGGER_USER ? "���� - " + this.TRIGGER_USER.size() + "��" : "����");
		logger.info("  ���ģ�", this.ENABLE_TRIGGER_DISZ ? "���� - " + this.TRIGGER_DISZ.size() + "��" : "����");
		logger.info("  Ⱥ�ģ�", this.ENABLE_TRIGGER_GROP ? "���� - " + this.TRIGGER_GROP.size() + "��" : "����");

		logger.seek("[Systemd] ������");
		logger.seek("  ˽�ģ�", this.ENABLE_LISENTER_USER ? "���� - " + this.LISTENER_DISZ.size() + "��" : "����");
		logger.seek("  ���ģ�", this.ENABLE_LISENTER_DISZ ? "���� - " + this.LISTENER_DISZ.size() + "��" : "����");
		logger.seek("  Ⱥ�ģ�", this.ENABLE_LISENTER_GROP ? "���� - " + this.LISTENER_GROP.size() + "��" : "����");

		logger.seek("[Systemd] ִ����");
		logger.seek("  ˽�ģ�", this.ENABLE_EXECUTOR_USER ? "���� - " + this.EXECUTOR_USER.size() + "��" : "����");
		logger.seek("  ���ģ�", this.ENABLE_EXECUTOR_DISZ ? "���� - " + this.EXECUTOR_DISZ.size() + "��" : "����");
		logger.seek("  Ⱥ�ģ�", this.ENABLE_EXECUTOR_GROP ? "���� - " + this.EXECUTOR_GROP.size() + "��" : "����");

		// =======================================================================================================================
		//
		//
		// =======================================================================================================================
		// Ԥ���� /list ����Ϣ

		entry.getMessage().genetateList(this.TRIGGER_USER, this.TRIGGER_DISZ, this.TRIGGER_GROP, this.LISTENER_USER, this.LISTENER_DISZ, this.LISTENER_GROP, this.EXECUTOR_USER, this.EXECUTOR_DISZ, this.EXECUTOR_GROP);
	}

	@Override
	public void boot(LoggerX logger) throws Exception {

		// =======================================================================================================================
		// ����������

		for (String name : this.TRIGGER_INSTANCE.keySet()) {
			logger.full("[Systemd] ����������", name);
			this.TRIGGER_INSTANCE.get(name).boot(logger);
		}

		// =======================================================================================================================
		// ���� ������

		for (String name : this.LISTENER_INSTANCE.keySet()) {
			logger.full("[Systemd] ����������", name);
			this.LISTENER_INSTANCE.get(name).boot(logger);
		}

		// =======================================================================================================================
		// ����ִ����

		for (String name : this.EXECUTOR_INSTANCE.keySet()) {
			logger.full("[Systemd] ����ִ����", name);
			this.EXECUTOR_INSTANCE.get(name).boot(logger);
		}

	}

	@Override
	public void shut(LoggerX logger) throws Exception {
		for (String name : this.TRIGGER_INSTANCE.keySet()) {
			this.TRIGGER_INSTANCE.get(name).shut(logger);
		}
		for (String name : this.LISTENER_INSTANCE.keySet()) {
			this.LISTENER_INSTANCE.get(name).shut(logger);
		}
		for (String name : this.EXECUTOR_INSTANCE.keySet()) {
			this.EXECUTOR_INSTANCE.get(name).shut(logger);
		}
	}

	@Override
	public void reload(LoggerX logger) throws Exception {
		for (String name : this.TRIGGER_INSTANCE.keySet()) {
			this.TRIGGER_INSTANCE.get(name).reload(logger);
		}
		for (String name : this.LISTENER_INSTANCE.keySet()) {
			this.LISTENER_INSTANCE.get(name).reload(logger);
		}
		for (String name : this.EXECUTOR_INSTANCE.keySet()) {
			this.EXECUTOR_INSTANCE.get(name).reload(logger);
		}
	}

	@Override
	public void groupMemberIncrease(int typeid, int sendtime, long gropid, long operid, long userid) throws Exception {
		LoggerX logger = new LoggerX();
		logger.info("[��Ⱥ]", typeid == 1 ? "��������" : "�����Ⱥ");
		logger.info("Ⱥ��", gropid);
		logger.info("����", JcqApp.CQ.getStrangerInfo(operid).getNick() + "(" + operid + ")");
		logger.info("��Ա", JcqApp.CQ.getStrangerInfo(userid).getNick() + "(" + userid + ")");
		entry.getMessage().adminInfo(logger.make(1));

		for (String name : this.TRIGGER_INSTANCE.keySet()) {
			this.TRIGGER_INSTANCE.get(name).groupMemberIncrease(typeid, sendtime, gropid, operid, userid);
		}
		for (String name : this.LISTENER_INSTANCE.keySet()) {
			this.LISTENER_INSTANCE.get(name).groupMemberIncrease(typeid, sendtime, gropid, operid, userid);
		}
		for (String name : this.EXECUTOR_INSTANCE.keySet()) {
			this.EXECUTOR_INSTANCE.get(name).groupMemberIncrease(typeid, sendtime, gropid, operid, userid);
		}
	}

	@Override
	public void groupMemberDecrease(int typeid, int sendtime, long gropid, long operid, long userid) throws Exception {
		LoggerX logger = new LoggerX();
		logger.info("[��Ⱥ]", typeid == 1 ? "������Ⱥ" : "�����߳�");
		logger.info("Ⱥ��", gropid);
		logger.info("����", JcqApp.CQ.getStrangerInfo(operid).getNick() + "(" + operid + ")");
		logger.info("��Ա", JcqApp.CQ.getStrangerInfo(userid).getNick() + "(" + userid + ")");
		entry.getMessage().adminInfo(logger.make(1));

		for (String name : this.TRIGGER_INSTANCE.keySet()) {
			this.TRIGGER_INSTANCE.get(name).groupMemberDecrease(typeid, sendtime, gropid, operid, userid);
		}
		for (String name : this.LISTENER_INSTANCE.keySet()) {
			this.LISTENER_INSTANCE.get(name).groupMemberDecrease(typeid, sendtime, gropid, operid, userid);
		}
		for (String name : this.EXECUTOR_INSTANCE.keySet()) {
			this.EXECUTOR_INSTANCE.get(name).groupMemberDecrease(typeid, sendtime, gropid, operid, userid);
		}
	}
	// ==========================================================================================================================================================
	//
	//
	//
	// ==========================================================================================================================================================

	private void instantiationTrigger(ModuleTrigger instance) {
		this.TRIGGER_INSTANCE.put(instance.MODULE_COMMANDNAME(), instance);
	}

	public void instantiationListener(ModuleListener instance) {
		this.LISTENER_INSTANCE.put(instance.MODULE_COMMANDNAME(), instance);
	}

	private void instantiationExecutor(ModuleExecutor instance) {
		this.EXECUTOR_INSTANCE.put(instance.MODULE_COMMANDNAME(), instance);
	}

	public ModuleTrigger getTrigger(String name) {
		return this.TRIGGER_INSTANCE.get(name);
	}

	public ModuleListener getListener(String name) {
		return this.LISTENER_INSTANCE.get(name);
	}

	public ModuleExecutor getExecutor(String name) {
		return this.EXECUTOR_INSTANCE.get(name);
	}

	// ==========================================================================================================================================================
	//
	//
	//
	// ==========================================================================================================================================================

	protected int doUserMessage(int typeid, long userid, MessageUser message, int messageid, int messagefont) throws Exception {

		this.COUNT_USER_MESSAGE++;

		// ===============================================================================================================================

		if (this.ENABLE_TRIGGER_USER) {
			for (ModuleTrigger temp : this.TRIGGER_USER) {
				if (temp.executeUserMessage(typeid, userid, message, messageid, messagefont)) { return IMsg.MSG_IGNORE; }
			}
		}

		// ===============================================================================================================================

		if (this.ENABLE_LISENTER_USER) {
			for (ModuleListener temp : this.LISTENER_USER) {
				temp.executeUserMessage(typeid, userid, message, messageid, messagefont);
			}
		}

		// ===============================================================================================================================

		if (message.anaylysIsCommand().isCommand()) {

			switch (message.parseOption().getCommand()) {

			// ===============================================================================================================================
			case "info":
				entry.getMessage().sendInfo(userid);
				break;
			// ===============================================================================================================================
			case "eula":
				entry.getMessage().sendEula(userid);
				break;
			// ===============================================================================================================================
			case "list":
				entry.getMessage().sendListUser(userid);
				break;
			// ===============================================================================================================================
			case "help":
				if (message.getSection() == 0) {
					entry.getMessage().sendHelp(userid);
				} else {
					if (this.EXECUTOR_INSTANCE.containsKey(message.getSegment()[0])) {
						entry.getMessage().sendHelp(userid, this.EXECUTOR_INSTANCE.get(message.getSegment()[0]));
					} else {
						entry.getMessage().sendHelp(userid);
					}
				}
				break;
			// ===============================================================================================================================
			default:
				if (this.ENABLE_EXECUTOR_USER && this.EXECUTOR_USER.containsKey(message.getCommand())) {
					this.EXECUTOR_USER.get(message.getCommand()).executeUserMessage(typeid, userid, message, messageid, messagefont);
				} else {
					entry.getMessage().userInfo(userid, "û�д˲��");
				}
				break;
			}
			// ===============================================================================================================================
		} else {
			entry.getMessage().userInfo(userid, "δʶ������ݣ���BOTû�����칦�ܣ���ʹ��/help�鿴������");
		}

		return IMsg.MSG_IGNORE;
	}

	// ==========================================================================================================================================================
	//
	//
	//
	// ==========================================================================================================================================================

	protected int doDiszMessage(long diszid, long userid, MessageDisz message, int messageid, int messagefont) throws Exception {

		this.COUNT_DISZ_MESSAGE++;

		// ===============================================================================================================================

		if (this.ENABLE_TRIGGER_DISZ) {
			for (ModuleTrigger temp : this.TRIGGER_DISZ) {
				if (temp.executeDiszMessage(diszid, userid, message, messageid, messagefont)) { return IMsg.MSG_IGNORE; }
			}
		}

		// ===============================================================================================================================

		if (this.ENABLE_LISENTER_DISZ) {
			for (ModuleListener temp : this.LISTENER_DISZ) {
				temp.executeDiszMessage(diszid, userid, message, messageid, messagefont);
			}
		}

		// ===============================================================================================================================

		if (message.anaylysIsCommand().isCommand()) {

			switch (message.parseOption().getCommand()) {
			// ===============================================================================================================================
			case "info":
				entry.getMessage().sendInfo(userid);
				break;
			// ===============================================================================================================================
			case "eula":
				entry.getMessage().sendEula(userid);
				break;
			// ===============================================================================================================================
			case "list":
				entry.getMessage().sendListUser(userid);
				break;
			// ===============================================================================================================================
			case "help":
				if (message.getSection() == 0) {
					entry.getMessage().sendHelp(userid);
				} else {
					if (this.EXECUTOR_INSTANCE.containsKey(message.getSegment()[0])) {
						entry.getMessage().sendHelp(userid, this.EXECUTOR_INSTANCE.get(message.getSegment()[0]));
					} else {
						entry.getMessage().sendHelp(userid);
					}
				}
				break;
			// ===============================================================================================================================
			default:
				if (this.ENABLE_EXECUTOR_DISZ && this.EXECUTOR_DISZ.containsKey(message.getCommand())) {
					this.EXECUTOR_DISZ.get(message.getCommand()).executeDiszMessage(diszid, userid, message, messageid, messagefont);
				} else {
					entry.getMessage().userInfo(userid, "û�д˲��");
				}
				break;
			}
			// ===============================================================================================================================
		}
		return IMsg.MSG_IGNORE;
	}

	// ==========================================================================================================================================================
	//
	//
	//
	// ==========================================================================================================================================================

	protected int doGropMessage(long gropid, long userid, MessageGrop message, int messageid, int messagefont) throws Exception {

		this.COUNT_GROP_MESSAGE++;

		// ===============================================================================================================================

		if (this.ENABLE_TRIGGER_GROP) {
			for (ModuleTrigger temp : this.TRIGGER_GROP) {
				if (temp.executeGropMessage(gropid, userid, message, messageid, messagefont)) { return IMsg.MSG_IGNORE; }
			}
		}

		// ===============================================================================================================================

		if (this.ENABLE_LISENTER_GROP) {
			for (ModuleListener temp : this.LISTENER_GROP) {
				temp.executeGropMessage(gropid, userid, message, messageid, messagefont);
			}
		}

		// ===============================================================================================================================

		if (message.anaylysIsCommand().isCommand()) {

			switch (message.parseOption().getCommand()) {
			// ===============================================================================================================================
			case "info":
				entry.getMessage().sendInfo(userid);
				break;
			// ===============================================================================================================================
			case "eula":
				entry.getMessage().sendEula(userid);
				break;
			// ===============================================================================================================================
			case "list":
				entry.getMessage().sendListUser(userid);
				break;
			// ===============================================================================================================================
			case "help":
				if (message.getSection() == 0) {
					entry.getMessage().sendHelp(userid);
				} else {
					if (this.EXECUTOR_INSTANCE.containsKey(message.getSegment()[0])) {
						entry.getMessage().sendHelp(userid, this.EXECUTOR_INSTANCE.get(message.getSegment()[0]));
					} else {
						entry.getMessage().userInfo(userid, "û�д˲��");
					}
				}
				break;
			// ===============================================================================================================================
			default:
				if (this.ENABLE_EXECUTOR_GROP && this.EXECUTOR_GROP.containsKey(message.getCommand())) {
					this.EXECUTOR_GROP.get(message.getCommand()).executeGropMessage(gropid, userid, message, messageid, messagefont);
				} else {
					entry.getMessage().sendHelp(userid);
				}
				break;
			}
			// ===============================================================================================================================
		}
		return IMsg.MSG_IGNORE;
	}

	// ==========================================================================================================================

	@Override
	public String[] generateReport(int mode, Message message, Object... parameters) {
		return null;
	}

	// ========================================================================================================================
	// ========================================================================================================================

	public SystemdDelegate getDelegate() {
		return this.delegate;
	}

	// ========================================================================================================================
	// ========================================================================================================================

	private String doGenerateSystemsReport() {

		StringBuilder builder = new StringBuilder();

		// ===============================================================================

		long uptime = System.currentTimeMillis() - entry.BOOTTIME;

		long uptimedd = uptime / 86400000;
		uptime = uptime % 86400000;
		long uptimehh = uptime / 3600000;
		uptime = uptime % 3600000;
		long uptimemm = uptime / 60000;
		uptime = uptime % 60000;
		long uptimess = uptime / 1000;

		long totalMemory = Runtime.getRuntime().totalMemory() / 1024;
		long freeMemory = Runtime.getRuntime().freeMemory() / 1024;

		// ========================================================================================================================

		builder.append(LoggerX.time());
		builder.append(" - ״̬��\r\n\r\n����ʱ��: ");
		builder.append(uptimedd);
		builder.append(" - ");
		builder.append(uptimehh);
		builder.append(":");
		builder.append(uptimemm);
		builder.append(":");
		builder.append(uptimess);

		// ===============================================================================

		builder.append("\r\nϵͳ�ڴ�: ");
		builder.append(totalMemory - freeMemory);
		builder.append("KB /");
		builder.append(totalMemory);
		builder.append("KB");

		// ===============================================================================

		builder.append("\r\n\r\n����-˽�ģ� ");
		builder.append(this.COUNT_USER_MESSAGE);
		builder.append("��\r\n����-���ģ� ");
		builder.append(this.COUNT_DISZ_MESSAGE);
		builder.append("��\r\n����-Ⱥ�ģ� ");
		builder.append(this.COUNT_GROP_MESSAGE);
		builder.append("��");

		// ===============================================================================

		builder.append("\r\n\r\n������: ");
		builder.append(this.TRIGGER_INSTANCE.size());
		builder.append("��");

		for (String name : this.TRIGGER_INSTANCE.keySet()) {
			ModuleTrigger temp = this.TRIGGER_INSTANCE.get(name);
			builder.append("\r\nģ�� ");
			builder.append(temp.MODULE_PACKAGENAME());
			builder.append(": ");
			builder.append(temp.COUNT_USER);
			builder.append("/");
			builder.append(temp.COUNT_DISZ);
			builder.append("/");
			builder.append(temp.COUNT_GROP);
			builder.append("��");
		}

		// ===============================================================================

		builder.append("\r\n\r\n������: ");
		builder.append(this.TRIGGER_INSTANCE.size());
		builder.append("��");

		for (String name : this.LISTENER_INSTANCE.keySet()) {
			ModuleListener temp = this.LISTENER_INSTANCE.get(name);
			builder.append("\r\nģ�� ");
			builder.append(temp.MODULE_PACKAGENAME());
			builder.append(": ");
			builder.append(temp.COUNT_USER);
			builder.append("/");
			builder.append(temp.COUNT_DISZ);
			builder.append("/");
			builder.append(temp.COUNT_GROP);
			builder.append("��");
		}

		// ===============================================================================

		builder.append("\r\n\r\nִ����: ");
		builder.append(this.TRIGGER_INSTANCE.size());
		builder.append("��");

		for (String name : this.EXECUTOR_INSTANCE.keySet()) {
			ModuleExecutor temp = this.EXECUTOR_INSTANCE.get(name);
			builder.append("\r\nģ�� ");
			builder.append(temp.MODULE_PACKAGENAME());
			builder.append(": ");
			builder.append(temp.COUNT_USER);
			builder.append("/");
			builder.append(temp.COUNT_DISZ);
			builder.append("/");
			builder.append(temp.COUNT_GROP);
			builder.append("��");
		}

		// ===============================================================================

		return builder.toString();
	}

	// ========================================================================================================================

	private String[] doGenerateModulesReport() {

		LinkedList<String> report = new LinkedList<>();

		StringBuilder builder = new StringBuilder();

		for (String name : this.TRIGGER_INSTANCE.keySet()) {
			ModuleTrigger temp = this.TRIGGER_INSTANCE.get(name);
			builder.setLength(0);
			builder.append("[������] ");
			builder.append(temp.MODULE_PACKAGENAME());
			builder.append(" - ");
			builder.append(temp.COUNT_USER);
			builder.append("/");
			builder.append(temp.COUNT_DISZ);
			builder.append("/");
			builder.append(temp.COUNT_GROP);
			builder.append("\r\n");
			builder.append(temp.generateReport(0, null, null, null));
			report.add(builder.toString());
		}

		// ===============================================================================

		for (String name : this.LISTENER_INSTANCE.keySet()) {
			ModuleListener temp = this.LISTENER_INSTANCE.get(name);
			builder.setLength(0);
			builder.append("[������]");
			builder.append(temp.MODULE_PACKAGENAME());
			builder.append(" - ");
			builder.append(temp.COUNT_USER);
			builder.append("/");
			builder.append(temp.COUNT_DISZ);
			builder.append("/");
			builder.append(temp.COUNT_GROP);
			builder.append("\r\n");
			builder.append(temp.generateReport(0, null, null, null));
			report.add(builder.toString());
		}

		// ===============================================================================

		for (String name : this.EXECUTOR_INSTANCE.keySet()) {
			ModuleExecutor temp = this.EXECUTOR_INSTANCE.get(name);
			builder.setLength(0);
			builder.append("[ִ����]");
			builder.append(temp.MODULE_PACKAGENAME());
			builder.append(" - ");
			builder.append(temp.COUNT_USER);
			builder.append("/");
			builder.append(temp.COUNT_DISZ);
			builder.append("/");
			builder.append(temp.COUNT_GROP);
			builder.append("\r\n");
			builder.append(temp.generateReport(0, null, null, null));
			report.add(builder.toString());
		}

		// ===============================================================================

		return report.toArray(new String[report.size()]);
	}

	// ===============================================================================

	private String doGenerateModuleFullHelp(String name) {
		if (this.EXECUTOR_INSTANCE.containsKey(name)) {
			return this.EXECUTOR_INSTANCE.get(name).MODULE_FULLHELP;
		} else {
			return null;
		}
	}

	// ===============================================================================

	private String[] doGenerateModuleReport(String name, int mode, Message message) {
		if (this.TRIGGER_INSTANCE.containsKey(name)) {
			return this.TRIGGER_INSTANCE.get(name).generateReport(mode, message, null, null);
		} else if (this.LISTENER_INSTANCE.containsKey(name)) {
			return this.LISTENER_INSTANCE.get(name).generateReport(mode, message, null, null);
		} else if (this.EXECUTOR_INSTANCE.containsKey(name)) {
			return this.EXECUTOR_INSTANCE.get(name).generateReport(mode, message, null, null);
		} else {
			return new String[] {
					"ģ�鲻����"
			};
		}
	}

	// ========================================================================================================================
	// ========================================================================================================================
	// ========================================================================================================================

	public void doSendModuleFullHelp(int logdest, long destid, String name) {
		switch (logdest) {
		case 0:
			entry.getMessage().adminInfo(this.doGenerateModuleFullHelp(name));
			break;
		case 1:
			entry.getMessage().userInfo(destid, this.doGenerateModuleFullHelp(name));
			break;
		case 2:
			entry.getMessage().diszInfo(destid, this.doGenerateModuleFullHelp(name));
			break;
		case 3:
			entry.getMessage().gropInfo(destid, this.doGenerateModuleFullHelp(name));
		}
	}

	public void doSendSystemsReport(int logdest, long destid) {
		switch (logdest) {
		case 0:
			entry.getMessage().adminInfo(this.doGenerateSystemsReport());
			break;
		case 1:
			entry.getMessage().userInfo(destid, this.doGenerateSystemsReport());
			break;
		case 2:
			entry.getMessage().diszInfo(destid, this.doGenerateSystemsReport());
			break;
		case 3:
			entry.getMessage().gropInfo(destid, this.doGenerateSystemsReport());
		}
	}

	public void doSendModulesReport(int logdest, long destid) {
		switch (logdest) {
		case 0:
			entry.getMessage().adminInfo(this.doGenerateModulesReport());
			break;
		case 1:
			entry.getMessage().userInfo(destid, this.doGenerateModulesReport());
			break;
		case 2:
			entry.getMessage().diszInfo(destid, this.doGenerateModulesReport());
			break;
		case 3:
			entry.getMessage().gropInfo(destid, this.doGenerateModulesReport());
		}
	}

	public void doSendModuleReport(Message message) {
		String[] target = message.getFlag("target").split(":");
		String[] module = message.getFlag("module").split(":");
		if (target == null || module == null) { return; }
		switch (target[0]) {
		case "1":
			entry.getMessage().userInfo(Long.parseLong(target[1]), this.doGenerateModuleReport(module[0], Integer.parseInt(module[1]), message));
			break;
		case "2":
			entry.getMessage().diszInfo(Long.parseLong(target[1]), this.doGenerateModuleReport(module[0], Integer.parseInt(module[1]), message));
			break;
		case "3":
			entry.getMessage().gropInfo(Long.parseLong(target[1]), this.doGenerateModuleReport(module[0], Integer.parseInt(module[1]), message));
			break;
		}

	}

	// ========================================================================================================================
	// ========================================================================================================================
	// ========================================================================================================================

	public class SystemdDelegate {

		public void sendModuleFullHelp(int logdest, long destid, String name) {
			Module_Systemd.this.doSendModuleFullHelp(logdest, destid, name);
		}

		public void sendSystemsReport(int logdest, long destid) {
			Module_Systemd.this.doSendSystemsReport(logdest, destid);
		}

		public void sendModulesReport(int logdest, long destid) {
			Module_Systemd.this.doSendModulesReport(logdest, destid);
		}

		public void sendModuleReport(Message message) {
			Module_Systemd.this.doSendModuleReport(message);
		}

	}

}