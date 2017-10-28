package com.cas.circuit.consts;

import java.util.HashMap;
import java.util.Map;

public final class MonitorConst {
	private MonitorConst() {
	}

	public static final String MONITOR_IMG_PATH = "me/Interface/nifty/texture/monitor/";
	public static final String TEXT_NORM_FONT = "me/Interface/nifty/fnt/monitor/cnc_font.fnt";
	public static final String TEXT_PMC_FONT = "me/Interface/nifty/fnt/monitor/cnc_pmc_font.fnt";
	public static final String TEXT_LCD_FONT = "me/Interface/nifty/fnt/monitor/cnc_lcd_font.fnt";
	public static final String TEXT_COLOR_STR_BLACK = "#000f";
	public static final String TEXT_COLOR_STR_GREEN = "#077f";
	public static final String TRANS_COLOR_STR = "#0000";
	public static final String VALUE_SELED_COLOR_STR = "#ff0f";// 金黄色
	public static final String TITLE_SELED_COLOR_STR = "#4b9f";// 浅蓝色
	public static final String DARK_GRAY_COLOR_STR = "#444f";// 深灰色
//	public static final String RAISED_PANEL_IMG = "me/Interface/nifty/texture/monitor/content_panel.png";
//	public static final String RAISED_PANEL_IMGMODE = "resize:1,8,1,2,1,8,1,7,1,8,1,1";

	public static final String TRUE = "1";
	public static final String FALSE = "0";
	public static final String PARAM_ON = "1";
	public static final String PARAM_OFF = "0";

	public static final String VALUE_ZERO = "0";

	public enum WorkMode {
		EDIT("编辑"), MEM("MEM"), MDI("MDI"), HND("HND"), JOG("JOG"), REF("REF"), DNC("DNC");
		private String text;

		WorkMode(String text) {
			this.text = text;
		}

		/**
		 * @return the text
		 */
		public String getText() {
			return text;
		}
	}

	// 工作方式
//	public static final String WORK_MODE_EDIT = "0";
	public static final String WORK_MODE_EDIT_TEXT = "编辑";
//	public static final String WORK_MODE_MEM = "1";
	public static final String WORK_MODE_MEM_TEXT = "MEM";
//	public static final String WORK_MODE_MDI = "2";
	public static final String WORK_MODE_MDI_TEXT = "MDI";
//	public static final String WORK_MODE_HND = "3";
	public static final String WORK_MODE_HND_TEXT = "HND";
//	public static final String WORK_MODE_JOG = "4";
	public static final String WORK_MODE_JOG_TEXT = "JOG";
//	public static final String WORK_MODE_REF = "5";
	public static final String WORK_MODE_REF_TEXT = "REF";
//	public static final String WORK_MODE_DNC = "6";
	public static final String WORK_MODE_DNC_TEXT = "DNC";

	public static Map<String, String> firstFunctionMap = new HashMap<String, String>();
	public static Map<String, String> secondFunctionMap = new HashMap<String, String>();

	public static final String MONITOR_AXIS_S1 = "S1";
	public static final String MONITOR_AXIS_X = "X";
	public static final String MONITOR_AXIS_Z = "Z";
	public static final String MONITOR_AXIS_Y = "Y";
	public static final String MONITOR_AXIS_U = "U";
	public static final String MONITOR_AXIS_V = "V";
	public static final String MONITOR_AXIS_W = "W";
	public static final String PARAMITEM_VALUE_88 = "88";
	public static final String PARAMITEM_VALUE_89 = "89";
	public static final String PARAMITEM_VALUE_90 = "90";

	public enum MenuButton {
		POS("POS"), PROG("PROG"), OFS_SET("OFS/SET"), SYSTEM("SYSTEM"), MSG("MSG"), CSTM_GR("CSTM/GR"), OPERATION("(操作)");
		private String text;

		MenuButton(String text) {
			this.text = text;
		}

		/**
		 * @return the text
		 */
		public String getText() {
			return text;
		}
	};

	// 菜单切换按钮名称
	public static final String MENU_POS = "POS";
	public static final String MENU_PROG = "PROG";
	public static final String MENU_OFS_SET = "OFS/SET";
	public static final String MENU_SYSTEM = "SYSTEM";
	public static final String MENU_MSG = "MSG";
	public static final String MENU_CSTM_GR = "CSTM/GR";
	public static final String MENU_OPERATION = "(操作)";

	// 系统
	public static final String SYSTEM_PARAMETER = "参数";
	public static final String SYSTEM_PRM_SETTING = "PRM设";
	public static final String SYSTEM_NO_SEARCH = "号搜索";
	public static final String SYSTEM_ON = "ON:1";
	public static final String SYSTEM_OFF = "OFF:0";
	public static final String SYSTEM_PMC = "PMC";
	public static final String SYSTEM_INPUT = "输入";
	public static final String SYSTEM_INPUT_PLUS = "+输入";
	public static final String SYSTEM_CANCEL = "取消";
	public static final String SYSTEM_EXEC = "执行";
	public static final String SYSTEM_SELECT = "选择";
	public static final String SYSTEM_MENU = "菜单";
	public static final String SYSTEM_AXIS_X = "X  轴";
	public static final String SYSTEM_AXIS_Z = "Z  轴";
	public static final String SYSTEM_AXIS_SETTING = "轴设定";
	public static final String SYSTEM_AXIS_SWITCH = "轴改变";
	public static final String SYSTEM_SWITCH = "切换";
	public static final String SYSTEM_PMC_SEARCH = "搜索";
	public static final String SYSTEM_CODE = "代码";
	public static final String SYSTEM_BACK = "返回";

	// 坐标
//	public static final String POSITION_ABSOLUTE = "绝对";
//	public static final String POSITION_RELATIVE = "相对";
//	public static final String POSITION_COMPREHENSIVE = "综合";
//	public static final String POSITION_HANDLE = "手轮";
	public static final String POSITION_PART_COUNT = "件数:0";
	public static final String POSITION_RUNTIME = "时间:0";
	public static final String POSITION_W_PRESET = "W预置";
	public static final String POSITION_PRESET = "预置";
	public static final String POSITION_RETURN_ZERO = "归零";
	public static final String POSITION_ALL_AXLS = "所有轴";
	public static final String POSITION_EXEC = "执行";
	public static final String INPUT_ANGLE_BRACKET = ">";
	public static final String INPUT_UNDER_LINE = "_";
	public static final String INPUT_SHIFT_TAG = "^";

	public static final String TIP_OUT_OF_RANGE = "数据超限";
	public static final String TIP_WRONG_FORMAT = "格式错误";
	public static final String TIP_WRONG_WORK_MODE = "错误方式";
	public static final String TIP_WRITE_LOCK = "写保护";
	public static final String TIP_AXLS_NAME_NOT_EXIST = "指定的轴名称不存在。";
	public static final String TIP_INPUT_OK = "输入OK？";
	public static final String TIP_INPUT_NOT_FIND = "地址没有发现";
}
