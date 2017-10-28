package com.cas.circuit.msg;

import java.io.IOException;

import com.cas.circuit.handler.LinkMsgHandler;
import com.cas.network.msg.BaseMsg;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.ColorRGBA;
import com.jme3.network.serializing.Serializable;

@Serializable
public class LinkerMsg extends BaseMsg implements Savable {
	public static final byte NONE_MSG = 0;
	/**
	 * 添加接线信息
	 */
	public static final byte ADD_LINKER = 1;
	/**
	 * 删除接线信息
	 */
	public static final byte DELETE_LINKER = 2;
	/**
	 * 删除临时接线
	 */
	public static final byte DELETE_SINGLE_TARGET = 3;
	/**
	 * 给连接线标记线号
	 */
	public static final byte MARK_LINKER = 4;
	/**
	 * 导线
	 */
	public static final byte WIRE = 0;
	/**
	 * 气管
	 */
	public static final byte PIPE = 1;
	/**
	 * 线缆-插座
	 */
	public static final byte CABLE_JACK = 2;
	/**
	 * 线缆-端子
	 */
	public static final byte CABLE_TERM = 3;
	/**
	 * 接线对象的HashCode
	 */
	private int key;
	/**
	 * 接线类型（导线、气管、线缆）
	 */
	private byte linkType;
	/**
	 * 接线颜色
	 */
	private ColorRGBA color;
	/**
	 * 接线元器件模型名称
	 */
	private String elecCompKey;
	/**
	 * 接线目标（端子、气口）模型名称
	 */
	private String targetKey;
	/**
	 * 线缆种类
	 */
	private String cableKey;
	/**
	 * 线缆上导线标签
	 */
	private String wireMark;
	/**
	 * 线缆是否为元器件上延伸线缆
	 */
	private boolean elecComp;
	/**
	 * 线号
	 */
	private String numMark;

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public byte getLinkType() {
		return linkType;
	}

	public void setLinkType(byte linkType) {
		this.linkType = linkType;
	}

	public ColorRGBA getColor() {
		return color;
	}

	public void setColor(ColorRGBA color) {
		this.color = color;
	}

	public String getElecCompKey() {
		return elecCompKey;
	}

	public void setElecCompKey(String elecCompKey) {
		this.elecCompKey = elecCompKey;
	}

	public String getTargetKey() {
		return targetKey;
	}

	public void setTargetKey(String targetKey) {
		this.targetKey = targetKey;
	}

	public String getCableKey() {
		return cableKey;
	}

	public void setCableKey(String cableKey) {
		this.cableKey = cableKey;
	}

	public String getWireMark() {
		return wireMark;
	}

	public void setWireMark(String wireMark) {
		this.wireMark = wireMark;
	}

	public boolean isElecComp() {
		return elecComp;
	}

	public void setElecComp(boolean elecComp) {
		this.elecComp = elecComp;
	}

	public String getNumMark() {
		return numMark;
	}

	public void setNumMark(String numMark) {
		this.numMark = numMark;
	}

	@Override
	public void write(JmeExporter ex) throws IOException {
		OutputCapsule out = ex.getCapsule(this);
		out.write(key, "Key", 0);
		out.write(linkType, "LinkType", (byte) 0);
		out.write(color, "Color", null);
		out.write(elecCompKey, "ElecCompKey", null);
		out.write(targetKey, "TargetKey", null);
		out.write(cableKey, "CableKey", null);
		out.write(wireMark, "WireMark", null);
		out.write(elecComp, "ElecComp", false);
		out.write(numMark, "NumMark", null);
	}

	@Override
	public void read(JmeImporter im) throws IOException {
		InputCapsule in = im.getCapsule(this);
		key = in.readInt("Key", 0);
		linkType = in.readByte("LinkType", (byte) 0);
		color = (ColorRGBA) in.readSavable("Color", null);
		elecCompKey = in.readString("ElecCompKey", null);
		targetKey = in.readString("TargetKey", null);
		cableKey = in.readString("CableKey", null);
		wireMark = in.readString("WireMark", null);
		elecComp = in.readBoolean("ElecComp", false);
		numMark = in.readString("NumMark", null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<LinkMsgHandler> getHandlerClass() {
		return LinkMsgHandler.class;
	}
}
