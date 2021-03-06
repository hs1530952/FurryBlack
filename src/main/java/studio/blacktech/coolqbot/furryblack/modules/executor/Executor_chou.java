package studio.blacktech.coolqbot.furryblack.modules.executor;


import org.meowy.cqp.jcq.entity.Member;
import studio.blacktech.common.security.RandomTool;
import studio.blacktech.coolqbot.furryblack.common.annotation.ModuleExecutorComponent;
import studio.blacktech.coolqbot.furryblack.common.message.Message;
import studio.blacktech.coolqbot.furryblack.common.message.MessageDisz;
import studio.blacktech.coolqbot.furryblack.common.message.MessageGrop;
import studio.blacktech.coolqbot.furryblack.common.message.MessageUser;
import studio.blacktech.coolqbot.furryblack.common.module.ModuleExecutor;
import studio.blacktech.coolqbot.furryblack.entry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * CoolQ在Enable阶段无法获取成员列表
 * 非常麻烦
 *
 * @author netuser
 */
@ModuleExecutorComponent
public class Executor_chou extends ModuleExecutor {

	private static final long serialVersionUID = 1L;

	// ==========================================================================================================================================================
	//
	// 模块基本配置
	//
	// ==========================================================================================================================================================

	private static final String MODULE_PACKAGENAME = "Executor_Chou";
	private static final String MODULE_COMMANDNAME = "chou";
	private static final String MODULE_DISPLAYNAME = "随机抽人";
	private static final String MODULE_DESCRIPTION = "从当前群随机选择一个成员";
	private static final String MODULE_VERSION = "1.2.1";
	private static final String[] MODULE_USAGE = new String[] {
			"/chou - 随机抽一个人", "/chou 理由 - 以某个理由抽一个人"
	};
	private static final String[] MODULE_PRIVACY_STORED = new String[] {};
	private static final String[] MODULE_PRIVACY_CACHED = new String[] {};
	private static final String[] MODULE_PRIVACY_OBTAIN = new String[] {
			"获取命令发送人", "获取群成员列表"
	};

	// ==========================================================================================================================================================
	//
	// 成员变量
	//
	// ==========================================================================================================================================================


	private File FILE_IGNORE_USER;

	private Map<Long, List<Long>> IGNORES;


	// ==========================================================================================================================================================
	//
	// 生命周期函数
	//
	// ==========================================================================================================================================================

	public Executor_chou() throws Exception {

		super(MODULE_PACKAGENAME, MODULE_COMMANDNAME, MODULE_DISPLAYNAME, MODULE_DESCRIPTION, MODULE_VERSION, MODULE_USAGE, MODULE_PRIVACY_STORED, MODULE_PRIVACY_CACHED, MODULE_PRIVACY_OBTAIN);

	}

	@Override
	public boolean init() throws Exception {

		initAppFolder();
		initConfFolder();

		IGNORES = new HashMap<>();


		FILE_IGNORE_USER = Paths.get(FOLDER_CONF.getAbsolutePath(), "ignore_user.txt").toFile();

		if (!FILE_IGNORE_USER.exists()) FILE_IGNORE_USER.createNewFile();


		long gropid;
		long userid;

		String line;
		String[] temp;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(FILE_IGNORE_USER), StandardCharsets.UTF_8))) {


			while ((line = reader.readLine()) != null) {

				if (line.startsWith("#")) continue;
				if (!line.contains(":")) continue;
				if (line.contains("#")) line = line.substring(0, line.indexOf("#")).trim();

				temp = line.split(":");

				if (temp.length != 2) {
					logger.warn("配置无效", line);
					continue;
				}

				gropid = Long.parseLong(temp[0]);
				userid = Long.parseLong(temp[1]);

				if (!IGNORES.containsKey(gropid)) IGNORES.put(gropid, new ArrayList<>());

				IGNORES.get(gropid).add(userid);

				logger.seek("排除用户", gropid + " > " + userid);

			}

		} catch (Exception exception) {
			return false;
		}


		ENABLE_USER = false;
		ENABLE_DISZ = false;
		ENABLE_GROP = true;

		return true;

	}


	@Override
	public boolean boot() throws Exception {

		return true;

	}

	@Override
	public boolean save() throws Exception {

		return true;

	}

	@Override
	public boolean shut() throws Exception {

		return true;

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

	// ==========================================================================================================================================================
	//
	// 工作函数
	//
	// ==========================================================================================================================================================

	@Override
	public boolean doUserMessage(MessageUser message) {

		return true;

	}

	@Override
	public boolean doDiszMessage(MessageDisz message) {

		return true;

	}

	@Override
	public boolean doGropMessage(MessageGrop message) {

		long gropid = message.getGropID();
		long userid = message.getUserID();

		List<Member> members = entry.listMembers(gropid);

		int size = members.size();

		if (size < 3) {
			entry.gropInfo(gropid, userid, "至少需要三个成员");

		} else {

			long chouid;

			do {
				chouid = members.get(RandomTool.nextInt(size)).getQQId();
			} while (chouid == userid || entry.isMyself(chouid) || IGNORES.containsKey(gropid) && IGNORES.get(gropid).contains(chouid));


			if (message.getParameterSection() == 1) {
				entry.gropInfo(gropid, userid, "随机抽到 " + entry.getNickname(gropid, chouid) + "(" + chouid + ")");
			} else {
				entry.gropInfo(gropid, userid, "随机抽到 " + entry.getNickname(gropid, chouid) + "(" + chouid + ")： " + message.getCommandBody());
			}
		}

		return true;

	}
	// ==========================================================================================================================================================
	//
	// 工具函数
	//
	// ==========================================================================================================================================================

	@Override
	public String[] generateReport(Message message) {
		return new String[0];
	}

}
