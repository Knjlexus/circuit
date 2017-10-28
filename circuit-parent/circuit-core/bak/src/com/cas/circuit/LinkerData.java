package com.cas.circuit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.cas.circuit.msg.LinkerMsg;
import com.cas.circuit.vo.Cable;
import com.cas.circuit.vo.Jack;
import com.cas.circuit.vo.Wire;
import com.cas.gas.vo.Pipe;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;

/**
 * 用于存档接线信息
 * @功能 LinkerData.java
 * @作者 CWJ
 * @创建日期 2016年6月11日
 * @修改人 CWJ
 */
public class LinkerData implements Savable {

	// 存档前的数据Map,KEY:hashCode
	private Map<String, LinkerMsg> linkerMapForSave = new LinkedHashMap<String, LinkerMsg>();

	// 读档后的数据Map,KEY:hashCode
	private Map<Integer, List<LinkerMsg>> linkerMapForRead = new HashMap<Integer, List<LinkerMsg>>();
	
	// 连线验证数据Map,KEY:elecCompKey+TargetKey
	private Map<String, List<LinkerMsg>> linkerMapForCheck = new HashMap<String, List<LinkerMsg>>();
	
	private ArrayList<LinkerMsg> linkerDatas = new ArrayList<LinkerMsg>();

	
	/**
	 * 存档拥有者编号
	 */
	private String userid;
//	/**
//	 * 所负责工作站
//	 */
//	private byte stationInCharge;

	public LinkerMsg addLinker(ILinker linker, ILinkTarget target, String wireMark) {
		// 封装LinkerInfo
		LinkerMsg linkInfo = new LinkerMsg();
		if (linker instanceof Wire) {
			linkInfo.setLinkType(LinkerMsg.WIRE);
		} else if (linker instanceof Pipe) {
			linkInfo.setLinkType(LinkerMsg.PIPE);
		} else if (linker instanceof Cable) {
			if (target instanceof Jack) {
				linkInfo.setLinkType(LinkerMsg.CABLE_JACK);
				linkInfo.setElecComp(((Cable) linker).isElecComp());
			} else {
				linkInfo.setLinkType(LinkerMsg.CABLE_TERM);
				linkInfo.setWireMark(wireMark); // A、B、C
			}
			linkInfo.setCableKey(((Cable) linker).getPO().getId());
		}
		linkInfo.setKey(linker.hashCode());
		linkInfo.setColor(linker.getColor());
		linkInfo.setElecCompKey(target.getElecCompKey());
		linkInfo.setTargetKey(target.getTargetKey());
		linkInfo.setNumMark(linker.getWireNum()); // 线号
		linkerMapForSave.put(linker.hashCode() + "_" + target.hashCode(), linkInfo);
		return linkInfo;
	}

	public List<LinkerMsg> modifyWireNum(ILinker linker, String wireNum) {
		int hashCode = linker.hashCode();
		// 因为连线时的判断所以此处线缆必然是特殊线缆
		if (linker instanceof Cable) {
			Cable cable = (Cable) linker;
			List<Wire> bindWires = cable.getBindWires();
			// 最近Bind的导线
			linker = bindWires.get(bindWires.size() - 1);
		}
		List<LinkerMsg> msgs = new ArrayList<LinkerMsg>();
		ILinkTarget target1 = linker.getLinkTarget1();
		modifyTargetWireNum(msgs, hashCode, target1, wireNum);
		ILinkTarget target2 = linker.getLinkTarget2();
		modifyTargetWireNum(msgs, hashCode, target2, wireNum);
		return msgs;
	}

	private void modifyTargetWireNum(List<LinkerMsg> msgs, int hashCode, ILinkTarget target, String wireNum) {
		if (target == null) {
			return;
		}
		LinkerMsg linkerInfo = linkerMapForSave.get(hashCode + "_" + target.hashCode());
		if (linkerInfo == null) {
			return;
		}
		linkerInfo.setNumMark(wireNum);
		msgs.add(linkerInfo);
	}

	public void removeLinker(ILinker linker) {
		int hashCode = linker.hashCode();
		ILinkTarget target1 = linker.getLinkTarget1();
		removeTarget(hashCode, target1);
		ILinkTarget target2 = linker.getLinkTarget2();
		removeTarget(hashCode, target2);
		if (linker instanceof Cable && ((Cable) linker).isSinglePlug()) {
			for (Wire wire : ((Cable) linker).getMark_wire().values()) {
				target1 = wire.getLinkTarget1();
				removeTarget(hashCode, target1);
				target2 = wire.getLinkTarget2();
				removeTarget(hashCode, target2);
			}
		}
	}

	public void removeTarget(int hashCode, ILinkTarget target) {
		if (target == null) {
			return;
		}
		linkerMapForSave.remove(hashCode + "_" + target.hashCode());
	}
	
	
	public void clear(){
		linkerDatas.clear();
		linkerMapForSave.clear();
		linkerMapForRead.clear();
		linkerMapForCheck.clear();
	}

	public Map<String, LinkerMsg> getLinkerMapForSave() {
		return linkerMapForSave;
	}
	
	public Map<Integer, List<LinkerMsg>> getLinkerMapForRead() {
		return linkerMapForRead;
	}

	public Map<String, List<LinkerMsg>> getLinkerMapForCheck() {
		return linkerMapForCheck;
	}
	
	public ArrayList<LinkerMsg> getLinkerDatas() {
		return linkerDatas;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

//	public byte getStationInCharge() {
//		return stationInCharge;
//	}
//
//	public void setStationInCharge(byte stationInCharge) {
//		this.stationInCharge = stationInCharge;
//	}

	@Override
	public void write(JmeExporter ex) throws IOException {
		OutputCapsule out = ex.getCapsule(this);
		linkerDatas.clear();
		linkerDatas.addAll(linkerMapForSave.values());
		out.writeSavableArrayList(linkerDatas, "LinkerDatas", new ArrayList<LinkerMsg>());
//		out.write(stationInCharge, "StationInCharge", StationConsts.NONE_STATION);
		out.write(userid, "Userid", null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(JmeImporter im) throws IOException {
		InputCapsule in = im.getCapsule(this);
		linkerDatas = in.readSavableArrayList("LinkerDatas", new ArrayList<LinkerMsg>());
//		stationInCharge = in.readByte("StationInCharge", StationConsts.NONE_STATION);
		userid = in.readString("Userid", null);
		
		for (LinkerMsg linker : linkerDatas) {
			int key1 = linker.getKey();
			if (linkerMapForRead.containsKey(key1)) {
				linkerMapForRead.get(key1).add(linker);
			} else {
				List<LinkerMsg> list = new ArrayList<LinkerMsg>();
				list.add(linker);
				linkerMapForRead.put(key1, list);
			}
			String key2 = linker.getElecCompKey()+linker.getTargetKey();
			if (linkerMapForCheck.containsKey(key2)) {
				linkerMapForCheck.get(key2).add(linker);
			} else {
				List<LinkerMsg> list = new ArrayList<LinkerMsg>();
				list.add(linker);
				linkerMapForCheck.put(key2, list);
			}
		}
	}
}
