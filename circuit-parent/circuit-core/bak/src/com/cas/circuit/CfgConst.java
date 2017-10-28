package com.cas.circuit;

public final class CfgConst {
	private CfgConst() {
	}

	public static final String DEFAULT_SCENE_ID = "workshop0";

	public static final String SAVE_GAME_PREFIX = "com/cas/me/";
	public static final String SAVE_GAME_FILE_EXT = ".me";
	public static final String SAVE_GAME_CONNECT_EXT = ".conn";
	public static final String SAVE_GAME_ELEC_EXT = ".elec";
	public static final String SAVE_GAME_POS = ".pos";
	public static final String SAVE_GAME_CAREER = "careers.xml";

	public static final String NAME_OPERATE = "operator";
	public static final String NAME_IO_0 = "IO";
	public static final String NAME_IO_1 = "IO_1";
	public static final String NAME_IO_2 = "IO_2";
	public static final String NAME_IO_3 = "IO_3";
	public static final String NAME_NC = "NC";
	public static final String NAME_PLC = "PLC";
	public static final String NAME_FXOR = "FX";
	public static final String NAME_RELAY_BOARD = "RelayBoard";

	public static final String NPC_NAME = "npc_name";
	public static final String NPC_PRACTICE = "npc_practice";
	public static final String NPC_EXAM = "npc_exam";

	public static final String PRACTICE_ID = "practice_id";
	public static final String QUEST_ACCEPT = "接受任务";
	public static final String QUEST_GIVE_UP = "放弃任务";

	public static final String IMG_PATH = "Interface/nifty/texture/";

	public static final String STATE_NORMAL = "state_normal";
	public static final String STATE_NORMAL_CURSER = "state_normal_curser";
	public static final String STATE_DIALOGUE = "state_dialogue";
	public static final String STATE_SYSMENU = "state_sysmenu";

	// 磁能转换控制输入输出
	public static final String SWITCH_CTRL_INPUT = "input";
	public static final String SWITCH_CTRL_OUTPUT = "output";
//	按下切换,抬起切换(已移动至ButtonIOMouseEvent)
//	public static final String BUTTON_INTERACT_PRESS = "press";
//	public static final String BUTTON_INTERACT_UNIDIR = "unidir";
//	public static final String BUTTON_INTERACT_CLICK = "click";
//	public static final String BUTTON_INTERACT_ROTATE = "rotate";
//	交互元件运动方式
	public static final String BUTTON_MOTION_MOVE = "move";
	public static final String BUTTON_MOTION_ROTATE = "rotate";
	public static final String BUTTON_MOTION_CIRCLE = "circle";

	public static final String BROKEN_STATE_DEFAULT = "完好";
	public static final String BROKEN_STATE_BREAK = "断路";
	public static final String BROKEN_STATE_SHORT = "短路";

	public static final String NOW_CONDITION_STUDY = "now_condition_study";
	public static final String NOW_CONDITION_TRAIN = "now_condition_train";
	public static final String NOW_CONDITION_CAREER = "now_condition_career";
	public static final String NOW_CONDITION_TASK = "now_condition_task";

	public static final String SELECT_STUDY = "教程";
	public static final String SELECT_TRAIN = "训练";
	public static final String SELECT_EXAM = "考核";
	public static final String SELECT_TASK = "任务";

	public static final String GOTO_MENU_DISABLE = "goto_menu_disable";

	// 电压类型
//	public static final String VOLTAGE_DC = "voltage_dc";
//	public static final String VOLTAGE_AC = "voltage_ac";

//	public static final String MACH_TYPE = "mach_type";

	public static final String SELECT_STUDY_IMG_PATH = "me/Interface/nifty/ui/resources/image/guide";
	public static final String SELECT_TRAIN_IMG_PATH = "me/Interface/nifty/ui/resources/image/guide";
	public static final String SELECT_EXAM_IMG_PATH = "me/Interface/nifty/ui/resources/image/guide";

	public static final String SEARCH_BOOK_IMG_PATH = "me/Interface/nifty/texture/books/";
	public static final String SEARCH_BOOK_IMG_PATH_SMALL = "me/Interface/nifty/texture/books/small/";

//  选择界面类型
	public static final String SELECT_TYPE_BROKEN_CASE = "brokenCase";

}
