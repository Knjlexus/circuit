package com.cas.circuit.util;

import java.util.HashMap;
import java.util.Map;

import com.cas.circuit.logic.Encoder;
import com.cas.circuit.logic.FR_D720SInverter;
import com.cas.circuit.logic.Inverter;
import com.cas.circuit.logic.MMT_4QLogic;
import com.cas.circuit.logic.ServoDrive;
import com.cas.circuit.logic.VS10N051CLogic;
import com.cas.circuit.logic.cylinder.CJ2B16CylinderLogic;
import com.cas.circuit.logic.cylinder.CJ2B16ShortCylinderLogic;
import com.cas.circuit.logic.cylinder.MHC2_20DCylinderLogic;
import com.cas.circuit.logic.cylinder.MHZ2_20DCylinderLogic;
import com.cas.circuit.logic.cylinder.MHZ2_20DCylinderLogicEx;
import com.cas.circuit.logic.cylinder.SwingCylinderLogic;
import com.cas.circuit.logic.cylinder.SwingCylinderLogicEx;
import com.cas.circuit.logic.motor.DCMotor;
import com.cas.circuit.logic.motor.ElectroMotor;
import com.cas.circuit.logic.motor.ServoMotor;
import com.cas.circuit.logic.plc.FX3UPLCLogic;
import com.cas.circuit.logic.sensor.DiffuseSensorLogic;
import com.cas.circuit.logic.sensor.FiberSensorLogic;
import com.cas.circuit.logic.sensor.InductanceLogic;
import com.cas.circuit.logic.sensor.MagnetismSensorLogic;
import com.cas.circuit.logic.valve.ValveLogic;
import com.cas.circuit.logic.valve.ValveLogicEx;
import com.cas.circuit.vo.Cable;
import com.cas.circuit.vo.ControlIO;
import com.cas.circuit.vo.ElecComp;
import com.cas.circuit.vo.ElecCompDef;
import com.cas.circuit.vo.Format;
import com.cas.circuit.vo.Jack;
import com.cas.circuit.vo.LightIO;
import com.cas.circuit.vo.Magnetism;
import com.cas.circuit.vo.ResisRelation;
import com.cas.circuit.vo.ResisState;
import com.cas.circuit.vo.SensorIO;
import com.cas.circuit.vo.Terminal;
import com.cas.circuit.vo.VoltageIO;
import com.cas.circuit.vo.Wire;
import com.cas.gas.vo.BlockRelation;
import com.cas.gas.vo.BlockState;
import com.cas.gas.vo.GasPort;
import com.cas.gas.vo.Pipe;
import com.cas.util.parser.ClsMap;
import com.cas.util.parser.ParserFactory;

/**
 * @功能 ElecCompUtil.java
 * @作者 Administrator
 * @创建日期 2016年5月20日
 * @修改人 Administrator
 */
public final class ElecCompUtil {
//	key：型号
	private static Map<Object, ElecCompDef> elecCompdefMap;
	private static ParserFactory parserFactory;

	static {
//		按首字母顺序， 防止漏或重复
		ClsMap.registClazz("BlockState", BlockState.class);
		ClsMap.registClazz("BlockRelation", BlockRelation.class);
		ClsMap.registClazz("ControlIO", ControlIO.class);
		ClsMap.registClazz("Cable", Cable.class);
		ClsMap.registClazz("ElecComp", ElecComp.class);
		ClsMap.registClazz("ElecCompDef", ElecCompDef.class);
		ClsMap.registClazz("Format", Format.class);
		ClsMap.registClazz("GasPort", GasPort.class);
		ClsMap.registClazz("Jack", Jack.class);
		ClsMap.registClazz("Pipe", Pipe.class);
		ClsMap.registClazz("LightIO", LightIO.class);
		ClsMap.registClazz("Magnetism", Magnetism.class);
		ClsMap.registClazz("ResisState", ResisState.class);
		ClsMap.registClazz("ResisRelation", ResisRelation.class);
		ClsMap.registClazz("SensorIO", SensorIO.class);
		ClsMap.registClazz("Terminal", Terminal.class);
		ClsMap.registClazz("VoltageIO", VoltageIO.class);
		ClsMap.registClazz("Wire", Wire.class);

//		=============logic=============
//		传感器逻辑类
		ClsMap.registClazz("DiffuseSensorLogic", DiffuseSensorLogic.class);
		ClsMap.registClazz("FiberSensorLogic", FiberSensorLogic.class);
		ClsMap.registClazz("InductanceLogic", InductanceLogic.class);
		ClsMap.registClazz("MagnetismSensorLogic", MagnetismSensorLogic.class);

//		PLC逻辑类
		ClsMap.registClazz("FX3UPLCLogic", FX3UPLCLogic.class);

//		气缸逻辑类
		ClsMap.registClazz("CJ2B16CylinderLogic", CJ2B16CylinderLogic.class);
		ClsMap.registClazz("CJ2B16ShortCylinderLogic", CJ2B16ShortCylinderLogic.class);
		ClsMap.registClazz("MHZ2_20DCylinderLogic", MHZ2_20DCylinderLogic.class);
		ClsMap.registClazz("MHZ2_20DCylinderLogicEx", MHZ2_20DCylinderLogicEx.class);
		ClsMap.registClazz("MHC2_20DCylinderLogic", MHC2_20DCylinderLogic.class);
		ClsMap.registClazz("SwingCylinderLogic", SwingCylinderLogic.class);
		ClsMap.registClazz("SwingCylinderLogicEx", SwingCylinderLogicEx.class);

//		电机逻辑类
		ClsMap.registClazz("ElectroMotor", ElectroMotor.class);
		ClsMap.registClazz("ServoMotor", ServoMotor.class);
		ClsMap.registClazz("DCMotor", DCMotor.class);
//		编码器逻辑类
		ClsMap.registClazz("Encoder", Encoder.class);
//		变频器逻辑类
		ClsMap.registClazz("Inverter", Inverter.class);
		ClsMap.registClazz("FR_D720SInverter", FR_D720SInverter.class);
//		直流调速器
		ClsMap.registClazz("MMT_4QLogic", MMT_4QLogic.class);
//		伺服逻辑类
		ClsMap.registClazz("ServoDrive", ServoDrive.class);

//		电磁阀逻辑类
		ClsMap.registClazz("ValveLogic", ValveLogic.class);
		ClsMap.registClazz("ValveLogicEx", ValveLogicEx.class);
//		行程开关
		ClsMap.registClazz("VS10N051CLogic", VS10N051CLogic.class);

		parserFactory = ParserFactory.createFactory("CIRCUIT_CFG_PARSER_FACTORY");

		elecCompdefMap = new HashMap<Object, ElecCompDef>();
		attachElecCompDef("com/cas/circuit/config/components.xml");
	}

	/**
	 * 
	 */
	private ElecCompUtil() {

	}

	/**
	 * @return
	 */
	public static Map<Object, ElecCompDef> getElecCompDefMap() {
		return elecCompdefMap;
	}

	/**
	 * @param filename
	 */
	public static void attachElecCompDef(String filename) {
		elecCompdefMap.putAll(parserFactory.getParser(ElecCompDef.class, filename).getDataMap());
	}

}
